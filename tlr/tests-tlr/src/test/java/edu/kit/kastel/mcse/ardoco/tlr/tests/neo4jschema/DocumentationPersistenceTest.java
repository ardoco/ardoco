/* Licensed under MIT 2026. */
package edu.kit.kastel.mcse.ardoco.tlr.tests.neo4jschema;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.util.Optional;
import java.util.Properties;

import edu.kit.kastel.mcse.ardoco.core.api.output.ArdocoResult;

import org.eclipse.collections.api.map.sorted.ImmutableSortedMap;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.Neo4jContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import edu.kit.kastel.mcse.ardoco.core.api.InputTextData;
import edu.kit.kastel.mcse.ardoco.core.api.models.ModelFormat;
import edu.kit.kastel.mcse.ardoco.core.api.text.Text;
import edu.kit.kastel.mcse.ardoco.core.common.util.DataRepositoryHelper;
import edu.kit.kastel.mcse.ardoco.core.data.DataRepository;
import edu.kit.kastel.mcse.ardoco.core.execution.ConfigurationHelper;
import edu.kit.kastel.mcse.ardoco.core.execution.RunnerBaseTest;
import edu.kit.kastel.mcse.ardoco.tlr.execution.Swattr;
import edu.kit.kastel.mcse.ardoco.tlr.models.agents.ArchitectureConfiguration;
import edu.kit.kastel.mcse.ardoco.tlr.tests.neo4jschema.util.TextEqualityHelper;
import edu.kit.kastel.mcse.ardoco.tlr.text.providers.informants.corenlp.CoreNLPProvider;
import edu.kit.kastel.mcse.ardoco.tlr.text.providers.informants.corenlp.TextImpl;
import edu.stanford.nlp.pipeline.CoreDocument;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import io.github.ardoco.core.neo4jschema.Main;
import io.github.ardoco.core.neo4jschema.entities.documentation.TextNode;
import io.github.ardoco.core.neo4jschema.repository.documentation.TextNodeRepository;
import io.github.ardoco.core.neo4jschema.service.documentation.DocumentationMapper;
import io.github.ardoco.core.neo4jschema.service.documentation.DocumentationPersistenceService;

@Testcontainers
@SpringBootTest(classes = Main.class)
class DocumentationPersistenceTest extends RunnerBaseTest {

    @Autowired
    private DocumentationPersistenceService persistenceService;

    @Autowired
    private TextNodeRepository textNodeRepository;

    private static StanfordCoreNLP pipeline;

    //    @Container
    //    @ServiceConnection // This automatically overwrites spring.neo4j.uri, username, and password!
    //    static Neo4jContainer<?> neo4j = new Neo4jContainer<>("neo4j:5.15.0")
    //            .withRandomPassword();

    @Container
    static Neo4jContainer<?> neo4j = new Neo4jContainer<>("neo4j:5").withRandomPassword();

    @DynamicPropertySource
    static void neo4jProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.neo4j.uri", neo4j::getBoltUrl);
        registry.add("spring.neo4j.authentication.username", () -> "neo4j");
        registry.add("spring.neo4j.authentication.password", neo4j::getAdminPassword);
    }

    @BeforeAll
    static void setupPipeline() {
        Properties props = new Properties();
        props.setProperty("ner.useSUTime", "false");
        props.setProperty("annotators", "tokenize,ssplit,pos,lemma,ner,parse,depparse");
        pipeline = new StanfordCoreNLP(props);
    }

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
        pipeline.annotate(coreDocument);

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

        Optional<TextNode> retrievedNodeOptional = textNodeRepository.findByArdocoId(documentId);
        if (retrievedNodeOptional.isEmpty()) {
            System.out.println("No TextNode found for document ID: " + documentId);
            return;
        }

        TextNode retrievedNode = retrievedNodeOptional.get();


        org.assertj.core.api.Assertions.assertThat(retrievedNode).isNotNull();
        org.assertj.core.api.Assertions.assertThat(retrievedNode.getSentences()).hasSize(2);

        // Check the first sentence
        var firstSentence = retrievedNode.getSentences().get(0);
        org.assertj.core.api.Assertions.assertThat(firstSentence.getText()).contains("The quick brown fox");
        org.assertj.core.api.Assertions.assertThat(firstSentence.getSentenceNumber()).isEqualTo(0);

        // Check the word chain
        var words = firstSentence.getWords();
        org.assertj.core.api.Assertions.assertThat(words).isNotEmpty();

        var firstWord = words.get(0);
        org.assertj.core.api.Assertions.assertThat(firstWord.getText()).isEqualTo("The");
        org.assertj.core.api.Assertions.assertThat(firstWord.getPosTag()).isEqualTo("DT");
        org.assertj.core.api.Assertions.assertThat(firstWord.getNextWord()).isNotNull();
        org.assertj.core.api.Assertions.assertThat(firstWord.getNextWord().getText()).isEqualTo("quick");

        DocumentationMapper mapper = new DocumentationMapper();
        Text restoredText = mapper.mapToDomain(retrievedNode);
        assertThat(restoredText.getSentences()).hasSize(2);

        var neo4jFirstSentence = restoredText.getSentences().get(0);
        org.assertj.core.api.Assertions.assertThat(firstSentence.getText()).contains("The quick brown fox");

        assertThat(neo4jFirstSentence.getPhrases()).isNotEmpty();
        var rootPhrase = neo4jFirstSentence.getPhrases().get(0);
        assertThat(rootPhrase.getPhraseType().toString()).isEqualTo("ROOT");

        org.assertj.core.api.Assertions.assertThat(firstWord.getText()).isEqualTo("The");
        org.assertj.core.api.Assertions.assertThat(firstWord.getNextWord().getText()).isEqualTo("quick");
    }

    @Test
    void testSwattrPipelineWithNeo4j() throws InterruptedException {
        var runner = new Swattr(projectName);
        var additionalConfigsMap = ConfigurationHelper.loadAdditionalConfigs(new File(additionalConfigs));
        runner.setUp(inputText, new ArchitectureConfiguration(new File(inputModelArchitecture), ModelFormat.PCM),
                (ImmutableSortedMap<String, String>) additionalConfigsMap, outputDir);

        testRunnerAssertions(runner);
        ArdocoResult result = runner.run();
        Assertions.assertNotNull(result);
    }

    @Test
    void saveAndLoadDocumentation() {
        DataRepository dataRepository = new DataRepository();
        DataRepositoryHelper.putInputText(dataRepository, this.inputText);
        CoreNLPProvider provider = new CoreNLPProvider(dataRepository);
        Text originalText = provider.getAnnotatedText();
        persistenceService.savePreprocessedText(originalText, InputTextData.ID);
        Text loadedText = persistenceService.loadPreprocessedText(InputTextData.ID);
        TextEqualityHelper.assertTextsEqual(originalText, loadedText);
    }
}
