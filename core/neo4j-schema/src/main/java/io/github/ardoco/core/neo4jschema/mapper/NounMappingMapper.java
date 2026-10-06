/* Licensed under MIT 2026. */
package io.github.ardoco.core.neo4jschema.mapper;

import java.util.ArrayList;
import java.util.HashMap;
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
import io.github.ardoco.core.neo4jschema.repository.documentation.DocumentationGraphRepository;

/**
 * Maps between domain {@link NounMapping} and Neo4j {@link NounMappingNode}.
 * Words/phrases are linked to existing documentation nodes (never recreated).
 */
@Component
public class NounMappingMapper {

    private static final Logger logger = LoggerFactory.getLogger(NounMappingMapper.class);

    /** Claimant used only when restoring confidences from stored probability doubles. */
    public static final Claimant RESUME_CLAIMANT = new Claimant() {
    };

    private final DocumentationGraphRepository documentationGraphRepository;

    public NounMappingMapper(DocumentationGraphRepository documentationGraphRepository) {
        this.documentationGraphRepository = documentationGraphRepository;
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
     * Resolves the ids of the existing Phrase nodes (from the preprocessed text graph) that correspond to the phrases of the given noun mapping.
     * Phrases are matched by phrase type and exact word positions; only ids are loaded, never {@code PhraseNode} objects.
     *
     * @param mapping the domain noun mapping
     * @return distinct Phrase ids; phrases without a match are logged and skipped
     */
    public List<String> resolvePhraseIds(NounMapping mapping) {
        Set<String> phraseIds = new LinkedHashSet<>();
        for (Phrase phrase : mapping.getPhrases()) {
            List<Integer> phraseWordPositions = phrase.getContainedWords().collect(Word::getPosition).toList();
            if (phraseWordPositions.isEmpty()) {
                logger.warn("Skipping IN_PHRASE for NounMapping {}: phrase has no contained words", mapping.getArdocoId());
                continue;
            }
            List<String> matches = documentationGraphRepository.findPhraseIdByTypeAndExactWordPositions(phrase.getPhraseType().name(),
                    phraseWordPositions);
            if (matches.isEmpty()) {
                logger.warn("No Phrase matched for NounMapping {} (type={}, positions={})", mapping.getArdocoId(), phrase.getPhraseType(),
                        phraseWordPositions);
            } else {
                phraseIds.add(matches.get(0));
            }
        }
        return new ArrayList<>(phraseIds);
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

        ImmutableList<Word> referenceWords = Lists.immutable.withAll(node.getReferenceWords().stream().map(wn -> wordsByPosition.get(wn.getPosition())).filter(
                w -> w != null).toList());

        MutableSortedMap<MappingKind, Confidence> distribution = SortedMaps.mutable.empty();
        Confidence nameConfidence = new Confidence(AggregationFunctions.AVERAGE);
        nameConfidence.addAgentConfidence(RESUME_CLAIMANT, node.getNameProbability());
        Confidence typeConfidence = new Confidence(AggregationFunctions.AVERAGE);
        typeConfidence.addAgentConfidence(RESUME_CLAIMANT, node.getTypeProbability());
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
