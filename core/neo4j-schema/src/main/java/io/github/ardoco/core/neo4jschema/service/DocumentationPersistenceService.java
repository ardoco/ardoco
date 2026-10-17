/* Licensed under MIT 2023-2026. */
package io.github.ardoco.core.neo4jschema.service;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

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
import io.github.ardoco.core.neo4jschema.entities.documentation.PhraseNode;
import io.github.ardoco.core.neo4jschema.entities.documentation.SentenceNode;
import io.github.ardoco.core.neo4jschema.entities.documentation.TextNode;
import io.github.ardoco.core.neo4jschema.mapper.DocumentationMapper;
import io.github.ardoco.core.neo4jschema.repository.documentation.TextNodeRepository;

@Service
public class DocumentationPersistenceService {

    private static final Logger logger = LoggerFactory.getLogger(DocumentationPersistenceService.class);

    private static final String PAGED_SENTENCE_QUERY = """
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

    private static final String DEPENDENCY_QUERY = """
            MATCH (t:Text {ardocoId: $id})-[:HAS_SENTENCE]->(:Sentence)-[:CONTAINS_WORD]->(source:Word)
            MATCH (source)-[rel:DEPENDENCY]->(target:Word)
            RETURN source.position AS sourcePos, target.position AS targetPos, rel.dependencyType AS type
            """;

    private final TextNodeRepository textRepository;
    private final DocumentationMapper documentationMapper;
    private final Neo4jClient neo4jClient;

    public DocumentationPersistenceService(TextNodeRepository textRepository, DocumentationMapper mapper, Neo4jClient neo4jClient) {
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
        // Only an existence check is needed here; the sentences/words/phrases are streamed via the paged Cypher queries below.
        // findByArdocoId now fully hydrates the graph, so avoid calling it just to null-check.
        if (!textRepository.existsByArdocoId(identifier)) {
            logger.info("No preprocessed text found for id: " + identifier);
            return Optional.empty();
        }

        Map<Integer, Neo4jWord> globalWordMap = new HashMap<>();
        List<Neo4jSentence> allDomainSentences = new ArrayList<>();

        // Use manual pagination for sentences to avoid many individual queries when using the repositorys findByArdocoId() method and then mapping.
        int pageSize = 100;
        int currentSkip = 0;
        boolean hasMore = true;

        while (hasMore) {
            var rows = neo4jClient.query(PAGED_SENTENCE_QUERY).bind(identifier).to("id").bind(currentSkip).to("skip").bind(pageSize).to("limit").fetch().all();

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
        neo4jClient.query(DEPENDENCY_QUERY).bind(identifier).to("id").fetch().all().forEach(row -> {
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
        persistTextBatched(textNode);
    }

    private static final int BATCH_CHUNK_SIZE = 2000;

    /**
     * Persists the already-built {@link TextNode} graph (see {@link DocumentationMapper#toNode}) via targeted, batched Cypher instead of
     * {@code textRepository.save(TextNode)}: a plain Spring Data cascade walks the whole cyclic Sentence/Word/Phrase/Dependency graph with its own
     * per-node id resolution. {@code toNode} already assigns every id exactly as before (deterministic {@code Sentence.ardocoId}, matching what
     * {@code TraceLinkPersistenceService} independently recomputes for SENTENCE_* trace links) and already de-duplicates shared {@link PhraseNode}s by
     * Java object identity (a phrase reachable from multiple ancestors - {@code getSubphrases()} returns the full transitive descendant set, not just
     * direct children - stays one physical node); this method only replaces the final persistence step.
     */
    private void persistTextBatched(TextNode textNode) {
        neo4jClient.query("CREATE (t:Text {id: $id, ardocoId: $ardocoId})")
                .bind(UUID.randomUUID().toString())
                .to("id")
                .bind(textNode.getArdocoId())
                .to("ardocoId")
                .run();

        List<SentenceNode> sentences = textNode.getSentences();

        createSentenceNodes(sentences);
        createWordNodes(sentences);
        Map<PhraseNode, String> phraseIds = collectPhrases(sentences);
        createPhraseNodes(phraseIds);

        createHasSentenceEdges(textNode.getArdocoId(), sentences);
        createNextSentenceEdges(sentences);
        createContainsWordEdges(sentences);
        createNextWordEdges(sentences);
        createDependencyEdges(sentences);
        createHasRootPhraseEdges(sentences, phraseIds);
        createPhraseContainsWordEdges(phraseIds);
        createHasChildPhraseEdges(phraseIds);
    }

    private void createSentenceNodes(List<SentenceNode> sentences) {
        List<Map<String, Object>> items = new ArrayList<>(sentences.size());
        for (SentenceNode s : sentences) {
            items.add(Map.of("ardocoId", s.getArdocoId(), "sentenceNumber", s.getSentenceNumber(), "text", s.getText()));
        }
        runBatchedCreate("CREATE (n:Sentence:Traceable) SET n += item", items);
    }

    private void createWordNodes(List<SentenceNode> sentences) {
        List<Map<String, Object>> items = new ArrayList<>();
        for (SentenceNode s : sentences) {
            for (var w : s.getWords()) {
                Map<String, Object> props = new HashMap<>();
                props.put("id", UUID.randomUUID().toString());
                props.put("position", w.getPosition());
                putIfNotNull(props, "text", w.getText());
                putIfNotNull(props, "lemma", w.getLemma());
                putIfNotNull(props, "posTag", w.getPosTag());
                items.add(props);
            }
        }
        runBatchedCreate("CREATE (n:Word) SET n += item", items);
    }

    /**
     * Collects every unique {@link PhraseNode} reachable from the sentences' root phrases, de-duplicated by Java object identity (matching
     * {@code toNode}'s {@code phraseCache}), and assigns each a fresh id. {@code getChildPhrases()} already holds the full transitive descendant set
     * (not just direct children), so a plain reachability walk with an identity-based visited set discovers every phrase exactly once regardless of
     * how many ancestors point to it.
     */
    private Map<PhraseNode, String> collectPhrases(List<SentenceNode> sentences) {
        Map<PhraseNode, String> phraseIds = new IdentityHashMap<>();
        Deque<PhraseNode> stack = new ArrayDeque<>();
        for (SentenceNode s : sentences) {
            stack.addAll(s.getRootPhrases());
        }
        while (!stack.isEmpty()) {
            PhraseNode phrase = stack.pop();
            if (phraseIds.containsKey(phrase)) {
                continue;
            }
            phraseIds.put(phrase, UUID.randomUUID().toString());
            stack.addAll(phrase.getChildPhrases());
        }
        return phraseIds;
    }

    private void createPhraseNodes(Map<PhraseNode, String> phraseIds) {
        List<Map<String, Object>> items = new ArrayList<>(phraseIds.size());
        for (Map.Entry<PhraseNode, String> entry : phraseIds.entrySet()) {
            PhraseNode phrase = entry.getKey();
            Map<String, Object> props = new HashMap<>();
            props.put("id", entry.getValue());
            putIfNotNull(props, "text", phrase.getText());
            putIfNotNull(props, "phraseType", phrase.getPhraseType());
            items.add(props);
        }
        runBatchedCreate("CREATE (n:Phrase) SET n += item", items);
    }

    private static void putIfNotNull(Map<String, Object> map, String key, Object value) {
        if (value != null) {
            map.put(key, value);
        }
    }

    private void runBatchedCreate(String createClause, List<Map<String, Object>> items) {
        if (items.isEmpty()) {
            return;
        }
        for (List<Map<String, Object>> chunk : chunk(items, BATCH_CHUNK_SIZE)) {
            neo4jClient.query("UNWIND $items AS item\n" + createClause).bind(chunk).to("items").run();
        }
    }

    private void createHasSentenceEdges(String textArdocoId, List<SentenceNode> sentences) {
        List<String> sentenceIds = sentences.stream().map(SentenceNode::getArdocoId).toList();
        runBatchedMatchedEdges("""
                MATCH (t:Text {ardocoId: $textArdocoId})
                UNWIND $ids AS sId
                MATCH (s:Sentence {ardocoId: sId})
                CREATE (t)-[:HAS_SENTENCE]->(s)
                """, Map.of("textArdocoId", textArdocoId), "ids", sentenceIds);
    }

    private void createNextSentenceEdges(List<SentenceNode> sentences) {
        List<Map<String, Object>> edges = new ArrayList<>();
        for (int i = 0; i < sentences.size() - 1; i++) {
            edges.add(Map.of("fromId", sentences.get(i).getArdocoId(), "toId", sentences.get(i + 1).getArdocoId()));
        }
        createMatchedPairEdges("Sentence", "ardocoId", "Sentence", "ardocoId", "NEXT_SENTENCE", edges);
    }

    private void createContainsWordEdges(List<SentenceNode> sentences) {
        List<Map<String, Object>> edges = new ArrayList<>();
        for (SentenceNode s : sentences) {
            for (var w : s.getWords()) {
                edges.add(Map.of("fromId", s.getArdocoId(), "toId", w.getPosition()));
            }
        }
        createMatchedPairEdges("Sentence", "ardocoId", "Word", "position", "CONTAINS_WORD", edges);
    }

    private void createNextWordEdges(List<SentenceNode> sentences) {
        List<Map<String, Object>> edges = new ArrayList<>();
        for (SentenceNode s : sentences) {
            var words = s.getWords();
            for (int i = 0; i < words.size() - 1; i++) {
                edges.add(Map.of("fromId", words.get(i).getPosition(), "toId", words.get(i + 1).getPosition()));
            }
        }
        createMatchedPairEdges("Word", "position", "Word", "position", "NEXT_WORD", edges);
    }

    private void createDependencyEdges(List<SentenceNode> sentences) {
        List<Map<String, Object>> edges = new ArrayList<>();
        for (SentenceNode s : sentences) {
            for (var w : s.getWords()) {
                for (var dependency : w.getDependencies()) {
                    edges.add(Map.of("fromId", w.getPosition(), "toId", dependency.getTargetWord().getPosition(), "type", dependency.getDependencyType()));
                }
            }
        }
        if (edges.isEmpty()) {
            return;
        }
        for (List<Map<String, Object>> chunk : chunk(edges, BATCH_CHUNK_SIZE)) {
            Long missing = neo4jClient.query("""
                    UNWIND $edges AS edge
                    OPTIONAL MATCH (s:Word {position: edge.fromId})
                    OPTIONAL MATCH (t:Word {position: edge.toId})
                    WITH s, t, edge
                    FOREACH (ignore IN CASE WHEN s IS NOT NULL AND t IS NOT NULL THEN [1] ELSE [] END |
                        CREATE (s)-[:DEPENDENCY {dependencyType: edge.type}]->(t))
                    RETURN sum(CASE WHEN s IS NULL OR t IS NULL THEN 1 ELSE 0 END) AS missing
                    """)
                    .bind(chunk)
                    .to("edges")
                    .fetchAs(Long.class)
                    .one()
                    .orElse(0L);
            if (missing != null && missing > 0) {
                logger.warn("DEPENDENCY: {} endpoint(s) not found", missing);
            }
        }
    }

    private void createHasRootPhraseEdges(List<SentenceNode> sentences, Map<PhraseNode, String> phraseIds) {
        List<Map<String, Object>> edges = new ArrayList<>();
        for (SentenceNode s : sentences) {
            for (PhraseNode root : s.getRootPhrases()) {
                edges.add(Map.of("fromId", s.getArdocoId(), "toId", phraseIds.get(root)));
            }
        }
        createMatchedPairEdges("Sentence", "ardocoId", "Phrase", "id", "HAS_ROOT_PHRASE", edges);
    }

    private void createPhraseContainsWordEdges(Map<PhraseNode, String> phraseIds) {
        List<Map<String, Object>> edges = new ArrayList<>();
        for (Map.Entry<PhraseNode, String> entry : phraseIds.entrySet()) {
            for (var w : entry.getKey().getContainedWords()) {
                edges.add(Map.of("fromId", entry.getValue(), "toId", w.getPosition()));
            }
        }
        createMatchedPairEdges("Phrase", "id", "Word", "position", "CONTAINS_WORD", edges);
    }

    private void createHasChildPhraseEdges(Map<PhraseNode, String> phraseIds) {
        List<Map<String, Object>> edges = new ArrayList<>();
        for (Map.Entry<PhraseNode, String> entry : phraseIds.entrySet()) {
            for (PhraseNode child : entry.getKey().getChildPhrases()) {
                edges.add(Map.of("fromId", entry.getValue(), "toId", phraseIds.get(child)));
            }
        }
        createMatchedPairEdges("Phrase", "id", "Phrase", "id", "HAS_CHILD_PHRASE", edges);
    }

    private void createMatchedPairEdges(String fromLabel, String fromKey, String toLabel, String toKey, String relationshipType,
            List<Map<String, Object>> edges) {
        if (edges.isEmpty()) {
            return;
        }
        for (List<Map<String, Object>> chunk : chunk(edges, BATCH_CHUNK_SIZE)) {
            Long missing = neo4jClient.query("""
                    UNWIND $edges AS edge
                    OPTIONAL MATCH (s:%s {%s: edge.fromId})
                    OPTIONAL MATCH (t:%s {%s: edge.toId})
                    WITH s, t
                    FOREACH (ignore IN CASE WHEN s IS NOT NULL AND t IS NOT NULL THEN [1] ELSE [] END | CREATE (s)-[:%s]->(t))
                    RETURN sum(CASE WHEN s IS NULL OR t IS NULL THEN 1 ELSE 0 END) AS missing
                    """.formatted(fromLabel, fromKey, toLabel, toKey, relationshipType))
                    .bind(chunk)
                    .to("edges")
                    .fetchAs(Long.class)
                    .one()
                    .orElse(0L);
            if (missing != null && missing > 0) {
                logger.warn("{}: {} endpoint(s) not found", relationshipType, missing);
            }
        }
    }

    private void runBatchedMatchedEdges(String cypher, Map<String, Object> scalarParams, String listParamName, List<?> listParam) {
        if (listParam.isEmpty()) {
            return;
        }
        for (List<?> chunk : chunk(listParam, BATCH_CHUNK_SIZE)) {
            var spec = neo4jClient.query(cypher);
            var boundSpec = spec.bindAll(scalarParams);
            boundSpec.bind(chunk).to(listParamName).run();
        }
    }

    private static <T> List<List<T>> chunk(List<T> items, int size) {
        List<List<T>> chunks = new ArrayList<>();
        for (int i = 0; i < items.size(); i += size) {
            chunks.add(items.subList(i, Math.min(i + size, items.size())));
        }
        return chunks;
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
