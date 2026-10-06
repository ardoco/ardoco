/* Licensed under MIT 2026. */
package io.github.ardoco.core.neo4jschema.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import edu.kit.kastel.mcse.ardoco.core.api.stage.textextraction.NounMapping;
import edu.kit.kastel.mcse.ardoco.core.api.text.Sentence;
import edu.kit.kastel.mcse.ardoco.core.api.text.Text;
import edu.kit.kastel.mcse.ardoco.core.api.text.Word;
import edu.kit.kastel.mcse.ardoco.tlr.textextraction.NounMappingImpl;
import io.github.ardoco.core.neo4jschema.entities.textextraction.NounMappingNode;
import io.github.ardoco.core.neo4jschema.mapper.NounMappingMapper;
import io.github.ardoco.core.neo4jschema.repository.textextraction.NounMappingRepository;

/**
 * Persists TextState noun mappings with Spring Data Neo4j.
 * Dual-write today; {@link #loadAllNounMappings(Text)} supports load-on-resume.
 */
@Service
public class TextStatePersistenceService {

    private static final Logger logger = LoggerFactory.getLogger(TextStatePersistenceService.class);

    private final NounMappingRepository nounMappingRepository;
    private final NounMappingMapper nounMappingMapper;

    public TextStatePersistenceService(NounMappingRepository nounMappingRepository, NounMappingMapper nounMappingMapper) {
        this.nounMappingRepository = nounMappingRepository;
        this.nounMappingMapper = nounMappingMapper;
    }

    /**
     * Creates or updates a NounMapping node and replaces its outgoing MAPS_WORD, HAS_REFERENCE_WORD and IN_PHRASE edges.
     * <p>
     * Deliberately does not use {@code nounMappingRepository.save(NounMappingNode)}: that cascades into the attached Word/Phrase objects and
     * synchronises their relationships, which deleted NEXT_WORD/DEPENDENCY edges and created orphan Phrase copies (TeaStore: -255 NEXT_WORD,
     * -358 DEPENDENCY, +3846 Phrase nodes). Here Word and Phrase nodes are only matched, never written.
     *
     * @param mapping the domain noun mapping
     */
    @Transactional
    public void saveNounMapping(NounMapping mapping) {
        String ardocoId = mapping.getArdocoId();
        List<Integer> wordPositions = NounMappingMapper.positionsOf(mapping.getWords());
        List<Integer> referencePositions = NounMappingMapper.positionsOf(mapping.getReferenceWords());
        List<String> phraseIds = nounMappingMapper.resolvePhraseIds(mapping);

        Long missing = nounMappingRepository.saveWithLinks(ardocoId, nounMappingMapper.toProperties(mapping), wordPositions, referencePositions, phraseIds);
        if (missing != null && missing > 0) {
            logger.warn("NounMapping {}: {} link targets not found (requested: {} words, {} reference words, {} phrases; target nodes missing)", ardocoId,
                    missing, wordPositions.size(), referencePositions.size(), phraseIds.size());
        }
        logger.debug("Saved NounMapping {} ({} words, {} phrases)", ardocoId, wordPositions.size(), phraseIds.size());
    }

    @Transactional
    public void deleteNounMapping(String ardocoId) {
        nounMappingRepository.deleteByArdocoId(ardocoId);
        logger.debug("Deleted NounMapping {}", ardocoId);
    }

    public boolean hasNounMappings() {
        return nounMappingRepository.count() > 0;
    }

    /**
     * Loads all NounMappings, resolving words against the annotated {@link Text}.
     * Phrases are derived from those domain words (not from Neo4j Phrase adapters).
     */
    @Transactional(readOnly = true)
    public Collection<NounMapping> loadAllNounMappings(Text annotatedText) {
        Map<Integer, Word> wordsByPosition = indexWordsByPosition(annotatedText);
        List<NounMapping> result = new ArrayList<>();
        for (NounMappingNode node : nounMappingRepository.findAll()) {
            long creationTime = NounMappingImpl.nextEarliestCreationTime();
            result.add(nounMappingMapper.toDomain(node, wordsByPosition, creationTime));
        }
        logger.info("Loaded {} NounMappings from Neo4j", result.size());
        return result;
    }

    private static Map<Integer, Word> indexWordsByPosition(Text annotatedText) {
        Map<Integer, Word> wordsByPosition = new HashMap<>();
        for (Sentence sentence : annotatedText.getSentences()) {
            for (Word word : sentence.getWords()) {
                wordsByPosition.put(word.getPosition(), word);
            }
        }
        return wordsByPosition;
    }
}
