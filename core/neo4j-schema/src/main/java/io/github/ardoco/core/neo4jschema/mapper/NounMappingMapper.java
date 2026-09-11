/* Licensed under MIT 2026. */
package io.github.ardoco.core.neo4jschema.mapper;

import java.util.ArrayList;
import java.util.HashSet;
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
import io.github.ardoco.core.neo4jschema.entities.documentation.PhraseNode;
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

    public NounMappingNode toNode(NounMapping mapping) {
        NounMappingNode node = new NounMappingNode(mapping.getArdocoId());
        node.setReference(mapping.getReference());
        node.setKind(mapping.getKind().name());
        node.setProbability(mapping.getProbability());
        node.setCompound(mapping.isCompound());
        node.setSurfaceForms(new ArrayList<>(mapping.getSurfaceForms().toList()));
        node.setNameProbability(mapping.getProbabilityForKind(MappingKind.NAME));
        node.setTypeProbability(mapping.getProbabilityForKind(MappingKind.TYPE));

        List<Integer> wordPositions = mapping.getWords().collect(Word::getPosition).toList();
        List<Integer> referencePositions = mapping.getReferenceWords().collect(Word::getPosition).toList();
        node.setMappedWords(findWords(wordPositions));
        node.setReferenceWords(findWords(referencePositions));

        List<PhraseNode> phraseNodes = new ArrayList<>();
        for (Phrase phrase : mapping.getPhrases()) {
            List<Integer> phraseWordPositions = phrase.getContainedWords().collect(Word::getPosition).toList();
            if (phraseWordPositions.isEmpty()) {
                logger.warn("Skipping IN_PHRASE for NounMapping {}: phrase has no contained words", mapping.getArdocoId());
                continue;
            }
            documentationGraphRepository.findPhraseByTypeAndExactWordPositions(phrase.getPhraseType().name(), phraseWordPositions).ifPresentOrElse(phraseNodes::add,
                    () -> logger.warn("No Phrase matched for NounMapping {} (type={}, positions={})", mapping.getArdocoId(), phrase.getPhraseType(),
                            phraseWordPositions));
        }
        node.setPhrases(phraseNodes);
        return node;
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

    private List<WordNode> findWords(List<Integer> positions) {
        if (positions == null || positions.isEmpty()) {
            return new ArrayList<>();
        }
        List<WordNode> found = documentationGraphRepository.findWordsByPositions(positions);
        Set<Integer> wanted = new HashSet<>(positions);
        List<WordNode> ordered = new ArrayList<>();
        for (Integer position : positions) {
            found.stream().filter(w -> w.getPosition() == position).findFirst().ifPresentOrElse(ordered::add, () -> logger.warn(
                    "No Word node for position {} while saving NounMapping", position));
            wanted.remove(position);
        }
        return ordered;
    }
}
