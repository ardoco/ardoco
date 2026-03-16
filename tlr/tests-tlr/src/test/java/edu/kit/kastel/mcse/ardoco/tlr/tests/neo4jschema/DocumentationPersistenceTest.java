/* Licensed under MIT 2026. */
package edu.kit.kastel.mcse.ardoco.tlr.tests.neo4jschema;

import static com.github.javaparser.utils.Utils.assertNotNull;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.fail;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import edu.kit.kastel.mcse.ardoco.core.api.InputTextData;
import edu.kit.kastel.mcse.ardoco.core.api.text.Text;
import edu.kit.kastel.mcse.ardoco.core.common.util.DataRepositoryHelper;
import edu.kit.kastel.mcse.ardoco.core.data.DataRepository;
import edu.kit.kastel.mcse.ardoco.tlr.tests.neo4jschema.util.TextEqualityHelper;
import edu.kit.kastel.mcse.ardoco.tlr.text.providers.informants.corenlp.CoreNLPProvider;
import edu.kit.kastel.mcse.ardoco.tlr.text.providers.informants.corenlp.TextImpl;
import edu.stanford.nlp.pipeline.CoreDocument;
import io.github.ardoco.core.neo4jschema.entities.documentation.TextNode;
import io.github.ardoco.core.neo4jschema.repository.documentation.TextNodeRepository;
import io.github.ardoco.core.neo4jschema.service.documentation.DocumentationMapper;
import io.github.ardoco.core.neo4jschema.service.documentation.DocumentationPersistenceService;

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

        // --- VISUALIZATION BLOCK ---
        System.out.println("----------------------------------------------------------");
        System.out.println("neo4j browser: " + neo4j.getHttpUrl()); // e.g., http://localhost:32789
        System.out.println("password:      " + neo4j.getAdminPassword());
        System.out.println("Connect URL:   " + neo4j.getBoltUrl());
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
        Text restoredText = mapper.mapToDomain(retrievedNode);

        assertThat(restoredText.getSentences()).hasSize(2);

        var neo4jFirstSentence = restoredText.getSentences().get(0);

        assertThat(neo4jFirstSentence.getPhrases()).isNotEmpty();
        assertThat(neo4jFirstSentence.getPhrases().get(0).getPhraseType().toString()).isEqualTo("ROOT");
    }

    @Test
    void testSaveAndLoadDocumentationFromNLPProvider() {
        DataRepository dataRepository = new DataRepository();
        DataRepositoryHelper.putInputText(dataRepository, this.inputText);

        CoreNLPProvider provider = new CoreNLPProvider(dataRepository);
        Text originalText = provider.getAnnotatedText();

        persistenceService.savePreprocessedText(originalText, InputTextData.ID);
        Text loadedText = persistenceService.loadPreprocessedText(InputTextData.ID);

        TextEqualityHelper.assertTextsEqual(originalText, loadedText);
    }
}
