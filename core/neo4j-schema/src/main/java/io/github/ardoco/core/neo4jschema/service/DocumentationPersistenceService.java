/* Licensed under MIT 2023-2026. */
package io.github.ardoco.core.neo4jschema.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import io.github.ardoco.core.neo4jschema.repository.documentation.SentenceNodeRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.neo4j.core.Neo4jClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import edu.kit.kastel.mcse.ardoco.core.api.text.DependencyTag;
import edu.kit.kastel.mcse.ardoco.core.api.text.Text;
import io.github.ardoco.core.neo4jschema.adapter.Neo4jSentence;
import io.github.ardoco.core.neo4jschema.adapter.Neo4jText;
import io.github.ardoco.core.neo4jschema.adapter.Neo4jWord;
import io.github.ardoco.core.neo4jschema.entities.documentation.SentenceNode;
import io.github.ardoco.core.neo4jschema.entities.documentation.TextNode;
import io.github.ardoco.core.neo4jschema.mapper.DocumentationMapper;
import io.github.ardoco.core.neo4jschema.repository.documentation.TextNodeRepository;

@Service
public class DocumentationPersistenceService {

    private final TextNodeRepository textRepository;
    private final DocumentationMapper documentationMapper;
    private static final Logger logger = LoggerFactory.getLogger(DocumentationPersistenceService.class);
    private final Neo4jClient neo4jClient;
    private final SentenceNodeRepository sentenceRepository;

    public DocumentationPersistenceService(TextNodeRepository textRepository, DocumentationMapper mapper, Neo4jClient neo4jClient, SentenceNodeRepository sentenceNodeRepository) {
        this.sentenceRepository = sentenceNodeRepository;
        this.neo4jClient = neo4jClient;
        this.textRepository = textRepository;
        this.documentationMapper = mapper;
    }

    /**
     * Checks if a preprocessed TextNode exists in the Neo4j database for the given identifier. This method performs a simple existence check using the
     * TextNodeRepository, which is optimized for this purpose.
     *
     * @param identifier The identifier to check for the existence of a corresponding TextNode in the database.
     * @return true if a TextNode with the given identifier exists, false otherwise.
     */
    @Transactional(readOnly = true)
    public boolean hasPreprocessedText(String identifier) {
        return textRepository.existsByArdocoId(identifier);
    }

    /**
     * Loads a preprocessed Text from the Neo4j database based on the provided identifier. This method performs a deep fetch of the TextNode and its related
     * entities to reconstruct the domain Text object. If no TextNode is found for the given identifier, it returns null.
     *
     * @param identifier The identifier used to locate the TextNode in the database
     * @return The domain Text object reconstructed from the TextNode, or null if no matching TextNode is found.
     */
    @Transactional(readOnly = true)
    public Optional<Text> loadPreprocessedText(String identifier) {
        TextNode meta = textRepository.findTextById(identifier).orElse(null);
        if (meta == null)
            return Optional.empty();

        Map<Integer, Neo4jWord> globalWordMap = new HashMap<>();
        List<Neo4jSentence> allDomainSentences = new ArrayList<>();

        // Use manual pagination for sentences to avoid many individual queries when using the repositorys findByArdocoId() method and then mapping.
        int pageSize = 100;
        int currentSkip = 0;
        boolean hasMore = true;

        while (hasMore) {
            String pagedCypher = """
                    MATCH (t:Text {ardocoId: $id})-[:HAS_SENTENCE]->(s:Sentence)
                    WHERE s.sentenceNumber >= $skip AND s.sentenceNumber < ($skip + $limit)
                    
                    OPTIONAL MATCH (s)-[:CONTAINS_WORD]->(w:Word)
                    WITH s, collect(DISTINCT w) AS words
                    
                    OPTIONAL MATCH (s)-[:HAS_ROOT_PHRASE]->(root:Phrase)
                    OPTIONAL MATCH (root)-[:HAS_CHILD_PHRASE*0..]->(p:Phrase)
                    
                    WITH s, words, 
                         collect(DISTINCT elementId(root)) AS rootIds, 
                         collect(DISTINCT p) AS allPhrases
                    
                    RETURN s {.*, 
                              words: [word IN words | word {.*}], 
                              rootPhraseIds: rootIds,
                              phrases: [phrase IN allPhrases | phrase { 
                                  .*, 
                                  id: elementId(phrase),
                                  childIds: [(phrase)-[:HAS_CHILD_PHRASE]->(child) | elementId(child)],
                                  containedWords: [(phrase)-[:CONTAINS_WORD]->(cw:Word) | cw.position]
                              }]
                             } AS sentenceData
                    ORDER BY s.sentenceNumber ASC
                    """;

            var rows = neo4jClient.query(pagedCypher).bind(identifier).to("id").bind(currentSkip).to("skip").bind(pageSize).to("limit").fetch().all();

            if (rows.isEmpty()) {
                hasMore = false;
            } else {
                for (Map<String, Object> row : rows) {
                    Map<String, Object> data = (Map<String, Object>) row.get("sentenceData");
                    SentenceNode sNode = documentationMapper.convertMapToSentenceNode(data);
                    allDomainSentences.add(documentationMapper.mapSentenceToDomain(sNode, globalWordMap));
                }
                currentSkip += pageSize;
            }
        }

        // Fetch dependencies between words
        String cypher = """
                MATCH (t:Text {ardocoId: $id})-[:HAS_SENTENCE]->(:Sentence)-[:CONTAINS_WORD]->(source:Word)
                MATCH (source)-[rel:DEPENDENCY]->(target:Word)
                RETURN source.position AS sourcePos, target.position AS targetPos, rel.dependencyType AS type
                """;

        neo4jClient.query(cypher).bind(identifier).to("id").fetch().all().forEach(row -> {
            Integer sPos = ((Long) row.get("sourcePos")).intValue();
            Integer tPos = ((Long) row.get("targetPos")).intValue();
            String type = (String) row.get("type");

            Neo4jWord sourceWord = globalWordMap.get(sPos);
            Neo4jWord targetWord = globalWordMap.get(tPos);

            if (sourceWord != null && targetWord != null) {
                try {
                    DependencyTag tag = DependencyTag.valueOf(type);
                    sourceWord.addOutgoingDependency(tag, targetWord);
                    targetWord.addIncomingDependency(tag, sourceWord);
                } catch (IllegalArgumentException e) {
                    logger.warn("Unknown Dependency Tag encountered: {}", type);
                }
            }
        });

        return Optional.of(new Neo4jText(identifier, allDomainSentences));
    }

    private List<Neo4jSentence> fetchAllSentences(String id, Map<Integer, Neo4jWord> wordMap) {
        List<Neo4jSentence> allSentences = new ArrayList<>();
        int skip = 0;
        List<Map<String, Object>> rows;

        do {
            rows = sentenceRepository.findPagedSentenceData(id, skip, 100);
            for (var row : rows) {
                var data = (Map<String, Object>) row.get("sentenceData");
                SentenceNode node = documentationMapper.convertMapToSentenceNode(data);
                allSentences.add(documentationMapper.mapSentenceToDomain(node, wordMap));
            }
            skip += 100;
        } while (!rows.isEmpty());

        return allSentences;
    }

    /**
     * Saves the provided domain Text object into the Neo4j database. This method first deletes any existing TextNode associated with the given documentId to
     * ensure that stale data is not retained. Then, it converts the domain Text into a TextNode entity using the DocumentationMapper and persists it using the
     * TextNodeRepository.
     *
     * @param domainText The domain Text object to be saved into the database.
     * @param documentId The identifier to associate with the saved TextNode entity, used for future retrieval and deletion operations.
     */

    @Transactional
    public void savePreprocessedText(Text domainText, String documentId) {
        deletePreprocessedText(documentId);
        TextNode textNode = documentationMapper.toNode(domainText, documentId);
        textRepository.save(textNode);
    }

    /**
     * Deletes a preprocessed TextNode and all its associated child nodes from the Neo4j database based on the provided identifier. Moreover, this method also
     * deletes all TraceLinks and Inconsistencies that point to the sentences of the TextNode.
     *
     * @param identifier The identifier used to locate the TextNode in the database for deletion.
     */
    @Transactional
    public void deletePreprocessedText(String identifier) {
        textRepository.deleteByArdocoId(identifier);
    }

}
