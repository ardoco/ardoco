/* Licensed under MIT 2026. */
package edu.kit.kastel.mcse.ardoco.tlr.tests.neo4jschema;

import static com.github.javaparser.utils.Utils.assertNotNull;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.File;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import edu.kit.kastel.mcse.ardoco.core.api.InputTextData;
import edu.kit.kastel.mcse.ardoco.core.api.text.Text;
import edu.kit.kastel.mcse.ardoco.core.common.util.CommonUtilities;
import edu.kit.kastel.mcse.ardoco.core.common.util.DataRepositoryHelper;
import edu.kit.kastel.mcse.ardoco.core.data.DataRepository;
import edu.kit.kastel.mcse.ardoco.tlr.tests.neo4jschema.util.TextEqualityHelper;
import edu.kit.kastel.mcse.ardoco.tlr.text.providers.informants.corenlp.CoreNLPProvider;
import edu.kit.kastel.mcse.ardoco.tlr.text.providers.informants.corenlp.TextImpl;
import edu.stanford.nlp.pipeline.CoreDocument;
import io.github.ardoco.core.neo4jschema.entities.documentation.DependencyRelationship;
import io.github.ardoco.core.neo4jschema.entities.documentation.PhraseNode;
import io.github.ardoco.core.neo4jschema.entities.documentation.SentenceNode;
import io.github.ardoco.core.neo4jschema.entities.documentation.TextNode;
import io.github.ardoco.core.neo4jschema.entities.documentation.WordNode;
import io.github.ardoco.core.neo4jschema.mapper.DocumentationMapper;
import io.github.ardoco.core.neo4jschema.repository.documentation.TextNodeRepository;
import io.github.ardoco.core.neo4jschema.service.DocumentationPersistenceService;

class DocumentationPersistenceTest extends AbstractPersistenceTest {

    @Autowired
    private DocumentationPersistenceService persistenceService;

    @Autowired
    private TextNodeRepository textNodeRepository;

    @AfterEach
    void tearDown() {
        textNodeRepository.deleteAll();
    }

    @Test
    @DisplayName("Should persist a Text object and retrieve it as a Graph Node")
    void testSaveAndRetrieveDocumentation() throws InterruptedException {

        String documentId = "test_doc_001";
        String content = "The quick brown fox jumps over the lazy dog. This is the second sentence.";
        CoreDocument coreDocument = new CoreDocument(content);
        this.getNLP().annotate(coreDocument);
        Text domainText = new TextImpl(coreDocument);

        persistenceService.savePreprocessedText(domainText, documentId);

        // --- VISUALIZATION BLOCK (Neo4j Desktop) ---
        System.out.println("----------------------------------------------------------");
        System.out.println("neo4j browser: " + NEO4J_BROWSER);
        System.out.println("password:      " + NEO4J_PASSWORD);
        System.out.println("Connect URL:   " + NEO4J_BOLT);
        System.out.println("----------------------------------------------------------");

        // Uncomment this line when you want to look at the graph.
        //Thread.sleep(1000 * 60 * 5); // Pauses for 5 minutes

        TextNode retrievedNode = textNodeRepository.findByArdocoId(documentId).orElseGet(() -> fail("No TextNode found for document ID: " + documentId));

        assertNotNull(retrievedNode);
        assertThat(retrievedNode.getSentences()).hasSize(2);

        // Check the first sentence
        var firstSentence = retrievedNode.getSentences().get(0);
        assertThat(firstSentence.getSentenceNumber()).isZero();
        assertThat(firstSentence.getText()).contains("The quick brown fox");

        // Check the word chain
        var words = firstSentence.getWords();
        assertThat(words).isNotEmpty();

        var firstWord = words.get(0);
        var secondWord = firstWord.getNextWord();

        assertThat(firstWord.getText()).isEqualTo("The");
        assertThat(firstWord.getPosTag()).isEqualTo("DT");
        assertThat(secondWord).isNotNull();
        assertThat(secondWord.getText()).isEqualTo("quick");

        // Assert - Domain Mapping
        DocumentationMapper mapper = new DocumentationMapper();
        //        Text restoredText = mapper.toDomain(retrievedNode);
        Text restoredText = mapper.toDomain(retrievedNode);

        assertThat(restoredText.getSentences()).hasSize(2);

        var neo4jFirstSentence = restoredText.getSentences().get(0);

        assertThat(neo4jFirstSentence.getPhrases()).isNotEmpty();
        assertThat(neo4jFirstSentence.getPhrases().get(0).getPhraseType().toString()).isEqualTo("ROOT");
    }

    @Test
    void testSaveAndLoadDocumentationFromNLPProvider() {
        DataRepository dataRepository = new DataRepository();
        // this.inputText is the path to the input file (see RunnerBaseTest), not its content - read the content first;
        // feeding the raw path string to CoreNLP fed it a nonsensical "document".
        DataRepositoryHelper.putInputText(dataRepository, CommonUtilities.readInputText(new File(this.inputText)));

        CoreNLPProvider provider = new CoreNLPProvider(dataRepository);
        Text originalText = provider.getAnnotatedText();

        persistenceService.savePreprocessedText(originalText, InputTextData.ID);
        Text loadedText = persistenceService.loadPreprocessedText(InputTextData.ID).orElse(null);

        // Compare against the Text materialized from the same toNode/toDomain path savePreprocessedText itself
        // uses, so both sides of the comparison go through the same canonicalization.
        DocumentationMapper mapper = new DocumentationMapper();
        Text expectedText = mapper.toDomain(mapper.toNode(originalText, InputTextData.ID));

        TextEqualityHelper.assertTextsEqual(expectedText, loadedText);
    }

    /**
     * Guards the write side directly: compares the persisted graph against the exact {@link TextNode} that was
     * saved, as strict multisets keyed by structural identity (not generated ids, which differ between the two
     * sides) - so a regression that writes the wrong edges/nodes fails here even if a later read-back happens to
     * tolerate it.
     */
    @Test
    void testPersistedGraphMatchesSavedTextNodeStructurally() {
        DataRepository dataRepository = new DataRepository();
        DataRepositoryHelper.putInputText(dataRepository, CommonUtilities.readInputText(new File(this.inputText)));
        CoreNLPProvider provider = new CoreNLPProvider(dataRepository);
        Text originalText = provider.getAnnotatedText();

        DocumentationMapper mapper = new DocumentationMapper();
        TextNode textNode = mapper.toNode(originalText, InputTextData.ID);
        persistenceService.savePreprocessedText(originalText, InputTextData.ID);

        Map<PhraseNode, String> phraseKeys = new IdentityHashMap<>();
        Deque<PhraseNode> stack = new ArrayDeque<>();
        for (SentenceNode s : textNode.getSentences()) {
            stack.addAll(s.getRootPhrases());
        }
        while (!stack.isEmpty()) {
            PhraseNode p = stack.pop();
            if (phraseKeys.containsKey(p)) {
                continue;
            }
            phraseKeys.put(p, phraseKey(p));
            stack.addAll(p.getChildPhrases());
        }

        Map<String, Integer> expectedPhraseKeys = new HashMap<>();
        phraseKeys.values().forEach(key -> expectedPhraseKeys.merge(key, 1, Integer::sum));

        Map<String, Integer> expectedChildPairs = new HashMap<>();
        for (Map.Entry<PhraseNode, String> entry : phraseKeys.entrySet()) {
            for (PhraseNode child : entry.getKey().getChildPhrases()) {
                expectedChildPairs.merge(entry.getValue() + " -> " + phraseKeys.get(child), 1, Integer::sum);
            }
        }

        Map<String, Integer> expectedRootPairs = new HashMap<>();
        for (SentenceNode s : textNode.getSentences()) {
            for (PhraseNode root : s.getRootPhrases()) {
                expectedRootPairs.merge(s.getArdocoId() + " -> " + phraseKeys.get(root), 1, Integer::sum);
            }
        }

        Map<String, Integer> expectedDependencies = new HashMap<>();
        Map<String, Integer> expectedNextWord = new HashMap<>();
        Map<String, Integer> expectedContainsWord = new HashMap<>();
        Map<String, Integer> expectedNextSentence = new HashMap<>();
        List<SentenceNode> sentences = textNode.getSentences();
        for (int i = 0; i < sentences.size(); i++) {
            SentenceNode s = sentences.get(i);
            List<WordNode> words = s.getWords();
            for (WordNode w : words) {
                expectedContainsWord.merge(s.getArdocoId() + "->" + w.getPosition(), 1, Integer::sum);
                for (DependencyRelationship dep : w.getDependencies()) {
                    expectedDependencies.merge(w.getPosition() + "->" + dep.getTargetWord().getPosition() + ":" + dep.getDependencyType(), 1, Integer::sum);
                }
            }
            for (int j = 0; j < words.size() - 1; j++) {
                expectedNextWord.merge(words.get(j).getPosition() + "->" + words.get(j + 1).getPosition(), 1, Integer::sum);
            }
            if (i < sentences.size() - 1) {
                expectedNextSentence.merge(s.getArdocoId() + "->" + sentences.get(i + 1).getArdocoId(), 1, Integer::sum);
            }
        }

        assertEquals(expectedPhraseKeys, phraseKeyCountsFromDb(), "Phrase node multiset mismatch");
        assertEquals(expectedChildPairs, childPhrasePairsFromDb(), "HAS_CHILD_PHRASE multiset mismatch");
        assertEquals(expectedRootPairs, rootPhrasePairsFromDb(), "HAS_ROOT_PHRASE multiset mismatch");
        assertEquals(expectedDependencies, pairMultisetFromDb("""
                MATCH (s:Word)-[r:DEPENDENCY]->(t:Word)
                RETURN s.position AS from, t.position AS to, r.dependencyType AS type
                """, "type"), "DEPENDENCY multiset mismatch");
        assertEquals(expectedNextWord, pairMultisetFromDb("""
                MATCH (s:Word)-[:NEXT_WORD]->(t:Word)
                RETURN s.position AS from, t.position AS to
                """, null), "NEXT_WORD multiset mismatch");
        assertEquals(expectedContainsWord, pairMultisetFromDb("""
                MATCH (s:Sentence)-[:CONTAINS_WORD]->(w:Word)
                RETURN s.ardocoId AS from, w.position AS to
                """, null), "Sentence CONTAINS_WORD multiset mismatch");
        assertEquals(expectedNextSentence, pairMultisetFromDb("""
                MATCH (s:Sentence)-[:NEXT_SENTENCE]->(t:Sentence)
                RETURN s.ardocoId AS from, t.ardocoId AS to
                """, null), "NEXT_SENTENCE multiset mismatch");
    }

    private static String phraseKey(PhraseNode p) {
        int firstPos = p.getContainedWords().stream().mapToInt(WordNode::getPosition).min().orElse(-1);
        return p.getPhraseType() + "|" + p.getText() + "|" + firstPos + "|" + p.getContainedWords().size();
    }

    private static String phraseKey(Object type, Object text, Object pos, Object wordCount) {
        return type + "|" + text + "|" + ((Number) pos).intValue() + "|" + ((Number) wordCount).intValue();
    }

    private Map<String, Integer> phraseKeyCountsFromDb() {
        Map<String, Integer> counts = new HashMap<>();
        for (Map<String, Object> row : neo4jClient.query("""
                MATCH (p:Phrase)
                OPTIONAL MATCH (p)-[:CONTAINS_WORD]->(w:Word)
                WITH p, min(w.position) AS minPos, count(w) AS wc
                RETURN p.phraseType AS type, p.text AS text, minPos, wc
                """).fetch().all()) {
            counts.merge(phraseKey(row.get("type"), row.get("text"), row.get("minPos"), row.get("wc")), 1, Integer::sum);
        }
        return counts;
    }

    private Map<String, Integer> childPhrasePairsFromDb() {
        Map<String, Integer> counts = new HashMap<>();
        for (Map<String, Object> row : neo4jClient.query("""
                MATCH (parent:Phrase)-[:HAS_CHILD_PHRASE]->(child:Phrase)
                OPTIONAL MATCH (parent)-[:CONTAINS_WORD]->(pw:Word)
                WITH parent, child, min(pw.position) AS parentMinPos, count(pw) AS parentWc
                OPTIONAL MATCH (child)-[:CONTAINS_WORD]->(cw:Word)
                WITH parent, child, parentMinPos, parentWc, min(cw.position) AS childMinPos, count(cw) AS childWc
                RETURN parent.phraseType AS pType, parent.text AS pText, parentMinPos AS pPos, parentWc AS pWc,
                       child.phraseType AS cType, child.text AS cText, childMinPos AS cPos, childWc AS cWc
                """).fetch().all()) {
            String parentKey = phraseKey(row.get("pType"), row.get("pText"), row.get("pPos"), row.get("pWc"));
            String childKey = phraseKey(row.get("cType"), row.get("cText"), row.get("cPos"), row.get("cWc"));
            counts.merge(parentKey + " -> " + childKey, 1, Integer::sum);
        }
        return counts;
    }

    private Map<String, Integer> rootPhrasePairsFromDb() {
        Map<String, Integer> counts = new HashMap<>();
        for (Map<String, Object> row : neo4jClient.query("""
                MATCH (s:Sentence)-[:HAS_ROOT_PHRASE]->(root:Phrase)
                OPTIONAL MATCH (root)-[:CONTAINS_WORD]->(w:Word)
                WITH s, root, min(w.position) AS minPos, count(w) AS wc
                RETURN s.ardocoId AS sId, root.phraseType AS rType, root.text AS rText, minPos, wc
                """).fetch().all()) {
            String rootKey = phraseKey(row.get("rType"), row.get("rText"), row.get("minPos"), row.get("wc"));
            counts.merge(row.get("sId") + " -> " + rootKey, 1, Integer::sum);
        }
        return counts;
    }

    private Map<String, Integer> pairMultisetFromDb(String cypher, String typeColumn) {
        Map<String, Integer> counts = new HashMap<>();
        for (Map<String, Object> row : neo4jClient.query(cypher).fetch().all()) {
            String key = row.get("from") + "->" + row.get("to") + (typeColumn == null ? "" : ":" + row.get(typeColumn));
            counts.merge(key, 1, Integer::sum);
        }
        return counts;
    }
}
