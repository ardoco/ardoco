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

    @Transactional
    public void saveNounMapping(NounMapping mapping) {
        NounMappingNode node = nounMappingMapper.toNode(mapping);
        nounMappingRepository.save(node);
        logger.debug("Saved NounMapping {} ({} words)", mapping.getArdocoId(), mapping.getWords().size());
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
