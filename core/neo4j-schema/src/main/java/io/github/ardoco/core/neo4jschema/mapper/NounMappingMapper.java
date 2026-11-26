/* Licensed under MIT 2026. */
package io.github.ardoco.core.neo4jschema.mapper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.factory.SortedMaps;
import org.eclipse.collections.api.factory.SortedSets;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.map.sorted.MutableSortedMap;
import org.eclipse.collections.api.set.sorted.MutableSortedSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.neo4j.core.Neo4jClient;
import org.springframework.stereotype.Component;

import edu.kit.kastel.mcse.ardoco.core.api.stage.textextraction.MappingKind;
import edu.kit.kastel.mcse.ardoco.core.api.stage.textextraction.NounMapping;
import edu.kit.kastel.mcse.ardoco.core.api.text.Phrase;
import edu.kit.kastel.mcse.ardoco.core.api.text.Word;
import edu.kit.kastel.mcse.ardoco.core.common.AggregationFunctions;
import edu.kit.kastel.mcse.ardoco.core.data.Confidence;
import edu.kit.kastel.mcse.ardoco.core.pipeline.agent.Claimant;
import edu.kit.kastel.mcse.ardoco.tlr.textextraction.NounMappingImpl;
import io.github.ardoco.core.neo4jschema.entities.documentation.WordNode;
import io.github.ardoco.core.neo4jschema.entities.textextraction.NounMappingNode;

/**
 * Maps between domain {@link NounMapping} and Neo4j {@link NounMappingNode}.
 * Words/phrases are linked to existing documentation nodes (never recreated).
 */
@Component
public class NounMappingMapper {

    private static final Logger logger = LoggerFactory.getLogger(NounMappingMapper.class);

    /** Claimant used only when restoring confidences from stored probability doubles. */
    public static final Claimant RESTORE_CLAIMANT = new Claimant() {
    };

    private static final int LOOKUP_CHUNK_SIZE = 2000;

    private static final String PHRASE_LOOKUP_QUERY = """
            UNWIND $phrases AS phrase
            MATCH (w:Word {position: phrase.positions[0]}) USING INDEX SEEK w:Word(position)
            MATCH (p:Phrase {phraseType: phrase.type})-[:CONTAINS_WORD]->(w)
            MATCH (p)-[:CONTAINS_WORD]->(contained:Word)
            WITH phrase, p, collect(DISTINCT contained.position) AS phrasePositions
            WHERE size(phrasePositions) = size(phrase.positions) AND ALL(position IN phrase.positions WHERE position IN phrasePositions)
            WITH phrase, collect(p.id) AS phraseIds
            RETURN phrase.key AS key, phraseIds[0] AS phraseId
            """;

    private final Neo4jClient neo4jClient;

    public NounMappingMapper(Neo4jClient neo4jClient) {
        this.neo4jClient = neo4jClient;
    }

    /**
     * Scalar properties of the NounMapping node, for {@code NounMappingRepository#upsertAndClearLinks}.
     * <p>
     * The keys must equal the field names of {@link NounMappingNode}: Spring Data maps those fields 1:1 (no {@code @Property} overrides), and
     * the read path ({@code findAll} + {@link #toDomain}) relies on the same names.
     *
     * @param mapping the domain noun mapping
     * @return property map (mutable, may contain {@code null} values)
     */
    public Map<String, Object> toProperties(NounMapping mapping) {
        Map<String, Object> props = new HashMap<>();
        props.put("reference", mapping.getReference());
        props.put("kind", mapping.getKind().name());
        props.put("probability", mapping.getProbability());
        props.put("isCompound", mapping.isCompound());
        props.put("surfaceForms", new ArrayList<>(mapping.getSurfaceForms().toList()));
        props.put("nameProbability", mapping.getProbabilityForKind(MappingKind.NAME));
        props.put("typeProbability", mapping.getProbabilityForKind(MappingKind.TYPE));
        return props;
    }

    /**
     * Distinct word positions in iteration order.
     *
     * @param words the words
     * @return their distinct positions
     */
    public static List<Integer> positionsOf(Iterable<? extends Word> words) {
        Set<Integer> positions = new LinkedHashSet<>();
        for (Word word : words) {
            positions.add(word.getPosition());
        }
        return new ArrayList<>(positions);
    }

    /**
     * Resolves the ids of the existing Phrase nodes (from the preprocessed text graph) that correspond to the phrases of the given noun mappings,
     * with one batched lookup per {@value #LOOKUP_CHUNK_SIZE} phrases. Phrases are matched by phrase type and exact word positions, starting from
     * the indexed position of their first word; only ids are loaded, never {@code PhraseNode} objects.
     *
     * @param mappings   the domain noun mappings
     * @param unresolved receives one description per phrase that has no contained words or no matching Phrase node
     * @return per noun-mapping ardocoId, the distinct Phrase ids of the phrases that were matched
     */
    public Map<String, List<String>> resolvePhraseIds(Collection<NounMapping> mappings, List<String> unresolved) {
        Map<String, List<String>> phraseIdsByMapping = new LinkedHashMap<>();
        List<Map<String, Object>> lookups = new ArrayList<>();
        Map<String, String> mappingByKey = new HashMap<>();
        Map<String, String> descriptionByKey = new HashMap<>();
        for (NounMapping mapping : mappings) {
            phraseIdsByMapping.putIfAbsent(mapping.getArdocoId(), new ArrayList<>());
            int index = 0;
            for (Phrase phrase : mapping.getPhrases()) {
                List<Integer> phraseWordPositions = phrase.getContainedWords().collect(Word::getPosition).toList();
                if (phraseWordPositions.isEmpty()) {
                    unresolved.add(mapping.getArdocoId() + ": phrase without contained words");
                    continue;
                }
                String key = mapping.getArdocoId() + "#" + index++;
                lookups.add(Map.of("key", key, "type", phrase.getPhraseType().name(), "positions", phraseWordPositions));
                mappingByKey.put(key, mapping.getArdocoId());
                descriptionByKey.put(key, mapping.getArdocoId() + ": no Phrase " + phrase.getPhraseType() + " " + phraseWordPositions);
            }
        }

        Map<String, String> phraseIdByKey = new HashMap<>();
        for (int start = 0; start < lookups.size(); start += LOOKUP_CHUNK_SIZE) {
            List<Map<String, Object>> chunk = lookups.subList(start, Math.min(start + LOOKUP_CHUNK_SIZE, lookups.size()));
            for (Map<String, Object> row : neo4jClient.query(PHRASE_LOOKUP_QUERY).bind(chunk).to("phrases").fetch().all()) {
                phraseIdByKey.put((String) row.get("key"), (String) row.get("phraseId"));
            }
        }

        for (Map<String, Object> lookup : lookups) {
            String key = (String) lookup.get("key");
            String phraseId = phraseIdByKey.get(key);
            if (phraseId == null) {
                unresolved.add(descriptionByKey.get(key));
                continue;
            }
            List<String> phraseIds = phraseIdsByMapping.get(mappingByKey.get(key));
            if (!phraseIds.contains(phraseId)) {
                phraseIds.add(phraseId);
            }
        }
        return phraseIdsByMapping;
    }

    /**
     * Restores a domain NounMapping. Domain {@link Word}s must come from the annotated {@link edu.kit.kastel.mcse.ardoco.core.api.text.Text}
     * (via {@code wordsByPosition}); phrases are derived from those words.
     */
    public NounMapping toDomain(NounMappingNode node, Map<Integer, Word> wordsByPosition, long earliestCreationTime) {
        MutableSortedSet<Word> words = SortedSets.mutable.empty();
        for (WordNode wordNode : node.getMappedWords()) {
            Word word = wordsByPosition.get(wordNode.getPosition());
            if (word != null) {
                words.add(word);
            } else {
                logger.warn("No domain Word for position {} while loading NounMapping {}", wordNode.getPosition(), node.getArdocoId());
            }
        }

        ImmutableList<Word> referenceWords = Lists.immutable.withAll(node.getReferenceWords()
                .stream()
                .map(wn -> wordsByPosition.get(wn.getPosition()))
                .filter(w -> w != null)
                .toList());

        MutableSortedMap<MappingKind, Confidence> distribution = SortedMaps.mutable.empty();
        Confidence nameConfidence = new Confidence(AggregationFunctions.AVERAGE);
        nameConfidence.addAgentConfidence(RESTORE_CLAIMANT, node.getNameProbability());
        Confidence typeConfidence = new Confidence(AggregationFunctions.AVERAGE);
        typeConfidence.addAgentConfidence(RESTORE_CLAIMANT, node.getTypeProbability());
        distribution.put(MappingKind.NAME, nameConfidence);
        distribution.put(MappingKind.TYPE, typeConfidence);

        ImmutableList<String> surfaceForms = Lists.immutable.withAll(node.getSurfaceForms() != null ? node.getSurfaceForms() : List.of());
        String reference = node.getReference() != null ? node.getReference() : "";

        NounMappingImpl mapping = new NounMappingImpl(node.getArdocoId(), earliestCreationTime, words.toImmutable(), distribution.toImmutable(), referenceWords,
                surfaceForms, reference);
        mapping.setIsDefinedAsCompound(node.isCompound());
        return mapping;
    }
}
