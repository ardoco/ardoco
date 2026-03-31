package io.github.ardoco.core.neo4jschema;

import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.neo4j.core.Neo4jClient;

import edu.kit.kastel.mcse.ardoco.core.api.models.ArchitectureModelWithComponentsAndInterfaces;
import edu.kit.kastel.mcse.ardoco.core.api.models.CodeModel;
import edu.kit.kastel.mcse.ardoco.core.api.models.CodeModelWithCompilationUnitsAndPackages;
import edu.kit.kastel.mcse.ardoco.core.api.models.Metamodel;
import edu.kit.kastel.mcse.ardoco.core.api.models.architecture.ArchitectureMethod;
import edu.kit.kastel.mcse.ardoco.core.api.stage.codetraceability.ArchitectureCodeTraceLink;
import edu.kit.kastel.mcse.ardoco.core.api.stage.connectiongenerator.SentenceModelTraceLink;
import edu.kit.kastel.mcse.ardoco.core.api.text.Text;
import edu.kit.kastel.mcse.ardoco.core.api.tracelink.TransitiveTraceLink;
import edu.kit.kastel.mcse.ardoco.id.types.ModelEntityAbsentFromTextInconsistency;
import edu.kit.kastel.mcse.ardoco.id.types.TextEntityAbsentFromModelInconsistency;
import io.github.ardoco.core.neo4jschema.adapter.Neo4jTextInconsistency;
import io.github.ardoco.core.neo4jschema.util.models.ArchitectureModelEqualityHelper;
import io.github.ardoco.core.neo4jschema.util.models.ArchitectureModelFactory;
import io.github.ardoco.core.neo4jschema.util.models.CodeModelEqualityHelper;
import io.github.ardoco.core.neo4jschema.util.models.CodeModelFactory;
import io.github.ardoco.core.neo4jschema.util.models.TextEqualityHelper;
import io.github.ardoco.core.neo4jschema.util.models.TextFactory;

@SpringBootTest(classes = io.github.ardoco.core.neo4jschema.Main.class)
public class Neo4jPersistenceHandlerFacadeTest extends AbstractNeo4jTest {

    @Autowired
    protected Neo4jPersistenceHandler persistenceHandler;

    @Autowired
    private Neo4jClient neo4jClient;

    @BeforeEach
    void clearDatabase() {
        neo4jClient.query("MATCH (n) DETACH DELETE n").run();

        // --- VISUALIZATION BLOCK ---
        System.out.println("----------------------------------------------------------");
        System.out.println("neo4j browser: " + neo4jContainer.getHttpUrl()); // e.g., http://localhost:32789
        System.out.println("password:      " + neo4jContainer.getAdminPassword());
        System.out.println("Connect URL:   " + neo4jContainer.getBoltUrl());
        System.out.println("----------------------------------------------------------");
    }

    @Test
    @DisplayName("Facade: Code Model Persistence and Metamodel Lookup")
    void testCodeModelPersistence() {
        CodeModelWithCompilationUnitsAndPackages codeModel = CodeModelFactory.createCodeModel();
        persistenceHandler.saveModel(Metamodel.CODE_WITH_COMPILATION_UNITS_AND_PACKAGES, codeModel);

        var storedMetamodels = persistenceHandler.getStoredMetamodels();
        Assertions.assertTrue(storedMetamodels.contains(Metamodel.CODE_WITH_COMPILATION_UNITS_AND_PACKAGES));

        CodeModel loaded = (CodeModel) persistenceHandler.loadModel(Metamodel.CODE_WITH_COMPILATION_UNITS_AND_PACKAGES);
        Assertions.assertNotNull(loaded);
        Assertions.assertEquals(codeModel.getId(), loaded.getId());
        CodeModelEqualityHelper.assertCodeModelsEqual(codeModel, loaded);
    }

    @Test
    @DisplayName("Facade: Architecture Model Persistence and Metamodel Lookup")
    void testArchitectureModelPersistence() {
        ArchitectureModelWithComponentsAndInterfaces archModel = ArchitectureModelFactory.createArchitectureModel();
        persistenceHandler.saveModel(Metamodel.ARCHITECTURE_WITH_COMPONENTS_AND_INTERFACES, archModel);

        var storedMetamodels = persistenceHandler.getStoredMetamodels();
        Assertions.assertTrue(storedMetamodels.contains(Metamodel.ARCHITECTURE_WITH_COMPONENTS_AND_INTERFACES));

        var loaded = (ArchitectureModelWithComponentsAndInterfaces) persistenceHandler.loadModel(Metamodel.ARCHITECTURE_WITH_COMPONENTS_AND_INTERFACES);
        Assertions.assertNotNull(loaded);
        Assertions.assertEquals(archModel.getId(), loaded.getId());
        ArchitectureModelEqualityHelper.assertArchitectureModelsEqual(archModel, loaded);
    }

    @Test
    @DisplayName("Facade: Overwriting existing model with smaller version")
    void testModelOverwrite() {

        var largeModel = CodeModelFactory.createCodeModel();
        persistenceHandler.saveModel(Metamodel.CODE_WITH_COMPILATION_UNITS_AND_PACKAGES, largeModel);

        var smallModel = CodeModelFactory.createSimpleCodeModel(largeModel.getId());
        persistenceHandler.saveModel(Metamodel.CODE_WITH_COMPILATION_UNITS_AND_PACKAGES, smallModel);

        CodeModel loaded = (CodeModel) persistenceHandler.loadModel(Metamodel.CODE_WITH_COMPILATION_UNITS_AND_PACKAGES);
        CodeModelEqualityHelper.assertCodeModelsEqual(smallModel, loaded);

        Long totalCodeItemsInDb = neo4jClient.query("MATCH (n:CodeItem) RETURN count(n)").fetchAs(Long.class).one().orElse(0L);

        int expectedCount = smallModel.getContent().size() + 1; // +1 for the CodeModel node itself, which also has CodeItem properties

        Assertions.assertEquals((long) expectedCount, totalCodeItemsInDb, "Database contains dangling CodeItem nodes from the previous large model!");

        var archModel = ArchitectureModelFactory.createArchitectureModel();
        persistenceHandler.saveModel(Metamodel.ARCHITECTURE_WITH_COMPONENTS_AND_INTERFACES, archModel);
        var storedMetamodels = persistenceHandler.getStoredMetamodels();
        Assertions.assertEquals(2, storedMetamodels.size());
        Assertions.assertTrue(storedMetamodels.contains(Metamodel.ARCHITECTURE_WITH_COMPONENTS_AND_INTERFACES));
        Assertions.assertTrue(storedMetamodels.contains(Metamodel.CODE_WITH_COMPILATION_UNITS_AND_PACKAGES));
    }

    @Test
    @DisplayName("Facade: Documentation Persistence")
    void testDocumentationModelPersistence() {
        Text documentation = TextFactory.createComplexText("text_id_01");
        persistenceHandler.savePreprocessedText(documentation, "identifier_01");

        Assertions.assertTrue(persistenceHandler.hasPreprocessedText("identifier_01"));
        Text loaded = persistenceHandler.loadPreprocessedText("identifier_01");
        Assertions.assertNotNull(loaded);
        TextEqualityHelper.assertTextsEqual(documentation, loaded);
    }

    @Test
    @DisplayName("Facade: Documentation with empty text")
    void testEmptyTextPersistence() {
        // Some NLP providers might return a text object with 0 sentences
        Text emptyText = TextFactory.createEmptyText("empty_id");
        persistenceHandler.savePreprocessedText(emptyText, "empty_id");

        Assertions.assertTrue(persistenceHandler.hasPreprocessedText("empty_id"));
        Text loaded = persistenceHandler.loadPreprocessedText("empty_id");
        Assertions.assertEquals(0, loaded.getSentences().size());
    }

    @Test
    @DisplayName("Facade: Trace Link to non-existent model entity")
    void testTraceLinkWithMissingTarget() {
        var codeModel = CodeModelFactory.createCodeModel();
        persistenceHandler.saveModel(Metamodel.CODE_WITH_COMPILATION_UNITS_AND_PACKAGES, codeModel);

        var archEntity = new ArchitectureMethod("fakeEntity", "ghost_id");
        var codeEntity = codeModel.getEndpoints().getFirst();

        ArchitectureCodeTraceLink link = new ArchitectureCodeTraceLink(archEntity, codeEntity);

        persistenceHandler.saveTraceLinks(List.of(link));

        var loaded = persistenceHandler.loadArchitectureCodeTraceLinks();
        Assertions.assertTrue(loaded.stream().noneMatch(l -> l.getFirstEndpoint().getId().equals("ghost_id")));
    }

    @Test
    @DisplayName("Facade: Test Roundtrip for Architecture-Code Trace Links")
    void testArchitectureCodeTraceLinkPersistence() {
        var archModel = ArchitectureModelFactory.createArchitectureModel();
        var codeModel = CodeModelFactory.createCodeModel();
        persistenceHandler.saveModel(Metamodel.ARCHITECTURE_WITH_COMPONENTS_AND_INTERFACES, archModel);
        persistenceHandler.saveModel(Metamodel.CODE_WITH_COMPILATION_UNITS_AND_PACKAGES, codeModel);

        var archEntity = archModel.getEndpoints().getFirst();
        var codeEntity = codeModel.getEndpoints().getFirst();

        ArchitectureCodeTraceLink link = new ArchitectureCodeTraceLink(archEntity, codeEntity);

        persistenceHandler.saveTraceLinks(List.of(link));
        var loadedLinks = persistenceHandler.loadArchitectureCodeTraceLinks();

        Assertions.assertFalse(loadedLinks.isEmpty());
        Assertions.assertTrue(loadedLinks.stream()
                .anyMatch(l -> l.getFirstEndpoint().getId().equals(archEntity.getId()) && l.getSecondEndpoint()
                        .getId()
                        .equals(codeEntity.getId()) && l.getFirstEndpoint().getName().equals(archEntity.getName()) && l.getSecondEndpoint()
                        .getName()
                        .equals(codeEntity.getName())));
    }

    @Test
    @DisplayName("Facade: Test Roundtrip for Transitive Trace Links")
    void testTransitiveTraceLinkPersistence() {
        Text text = TextFactory.createComplexText("transitive_doc");
        var archModel = ArchitectureModelFactory.createArchitectureModel();
        var codeModel = CodeModelFactory.createCodeModel();

        persistenceHandler.savePreprocessedText(text, "PreprocessingData");
        persistenceHandler.saveModel(Metamodel.ARCHITECTURE_WITH_COMPONENTS_AND_INTERFACES, archModel);
        persistenceHandler.saveModel(Metamodel.CODE_WITH_COMPILATION_UNITS_AND_PACKAGES, codeModel);

        var sentence = text.getSentences().get(0);
        var archEntity = archModel.getEndpoints().get(0);
        var codeItem = codeModel.getEndpoints().get(0);

        var link1 = new SentenceModelTraceLink(sentence, archEntity);
        var link2 = new ArchitectureCodeTraceLink(archEntity, codeItem);

        var transitiveLink = TransitiveTraceLink.createTransitiveTraceLink(link1, link2)
                .orElseThrow(() -> new IllegalStateException("Failed to create transitive link - IDs didn't match"));

        persistenceHandler.saveTraceLinks(List.of(transitiveLink));
        var loaded = persistenceHandler.loadTransitiveTraceLinks();

        Assertions.assertFalse(loaded.isEmpty(), "Transitive link should be persisted");
        Assertions.assertTrue(loaded.stream().anyMatch(l -> l instanceof TransitiveTraceLink));
    }

    @Test
    @DisplayName("Facade: Test Roundtrip for SentenceModelTraceLinks")
    void testSentenceModelTraceLinkPersistence() {
        Text text = TextFactory.createComplexText("PreprocessingData");
        var codeModel = CodeModelFactory.createCodeModel();
        persistenceHandler.savePreprocessedText(text, "PreprocessingData");
        persistenceHandler.saveModel(Metamodel.CODE_WITH_COMPILATION_UNITS_AND_PACKAGES, codeModel);

        var sentence = text.getSentences().get(0);
        var codeItem = codeModel.getEndpoints().get(0);

        SentenceModelTraceLink link = new SentenceModelTraceLink(sentence, codeItem);

        persistenceHandler.saveTraceLinks(List.of(link));
        var loaded = persistenceHandler.loadSentenceModelTraceLinks();

        Assertions.assertFalse(loaded.isEmpty());
        Assertions.assertTrue(loaded.stream().anyMatch(l -> l.getSentenceNumber() == sentence.getSentenceNumber()));
    }

    @Test
    @DisplayName("Facade: Test Roundtrip for Inconsistencies")
    void testInconsistencyPersistence() {

        var archModel = ArchitectureModelFactory.createArchitectureModel();
        persistenceHandler.saveModel(Metamodel.ARCHITECTURE_WITH_COMPONENTS_AND_INTERFACES, archModel);
        var archEntity = archModel.getEndpoints().get(0);

        Text text = TextFactory.createComplexText("inconsistency_doc");
        persistenceHandler.savePreprocessedText(text, "PreprocessingData");
        var sentenceNumber = text.getSentences().get(0).getSentenceNumber();

        var modelInc = new ModelEntityAbsentFromTextInconsistency(archEntity);
        var textInc = new TextEntityAbsentFromModelInconsistency("MissingComponent", sentenceNumber, 0.95, null);

        persistenceHandler.addInconsistencies(List.of(modelInc, textInc));
        var loaded = persistenceHandler.getInconsistencies();

        Assertions.assertEquals(2, loaded.size(), "Should have loaded both inconsistencies");

        boolean foundModelInc = loaded.stream()
                .filter(i -> i instanceof ModelEntityAbsentFromTextInconsistency)
                .map(i -> (ModelEntityAbsentFromTextInconsistency) i)
                .anyMatch(i -> i.getModelInstanceUid().equals(archEntity.getId()));

        boolean foundTextInc = loaded.stream()
                .filter(i -> i instanceof Neo4jTextInconsistency)
                .map(i -> (Neo4jTextInconsistency) i)
                .anyMatch(i -> i.getSentenceNumber() == sentenceNumber);

        Assertions.assertTrue(foundModelInc, "ModelEntityAbsentFromTextInconsistency should be restored with correct ID");
        Assertions.assertTrue(foundTextInc, "TextEntityAbsentFromModelInconsistency should be restored with correct sentence number.");
    }

    //-----------------------------------------------------------------------------------------------

    @Test
    @DisplayName("Facade: Delete specific model and its items")
    void testDeleteModel() {
        var archModel = ArchitectureModelFactory.createArchitectureModel();
        var codeModel = CodeModelFactory.createCodeModel();
        persistenceHandler.saveModel(Metamodel.ARCHITECTURE_WITH_COMPONENTS_AND_INTERFACES, archModel);
        persistenceHandler.saveModel(Metamodel.CODE_WITH_COMPILATION_UNITS_AND_PACKAGES, codeModel);

        persistenceHandler.deleteModel(Metamodel.ARCHITECTURE_WITH_COMPONENTS_AND_INTERFACES);

        Assertions.assertNull(persistenceHandler.loadModel(Metamodel.ARCHITECTURE_WITH_COMPONENTS_AND_INTERFACES));
        Assertions.assertNotNull(persistenceHandler.loadModel(Metamodel.CODE_WITH_COMPILATION_UNITS_AND_PACKAGES));

        Long archItemsCount = neo4jClient.query("MATCH (n:ArchitectureItem) RETURN count(n)").fetchAs(Long.class).one().get();
        Assertions.assertEquals(0L, archItemsCount, "Architecture items were not cleaned up!");
    }

    @Test
    @DisplayName("Facade: Delete model should cascade to TraceLinks")
    void testDeleteModelCascadesToTraceLinks() {
        var archModel = ArchitectureModelFactory.createArchitectureModel();
        var codeModel = CodeModelFactory.createCodeModel();
        persistenceHandler.saveModel(Metamodel.ARCHITECTURE_WITH_COMPONENTS_AND_INTERFACES, archModel);
        persistenceHandler.saveModel(Metamodel.CODE_WITH_COMPILATION_UNITS_AND_PACKAGES, codeModel);

        var link = new ArchitectureCodeTraceLink(archModel.getEndpoints().get(0), codeModel.getEndpoints().get(0));
        persistenceHandler.saveTraceLinks(List.of(link));
        Long codeItemsCountBefore = neo4jClient.query("MATCH (n:CodeItem) RETURN count(n)").fetchAs(Long.class).one().get();

        persistenceHandler.deleteModel(Metamodel.ARCHITECTURE_WITH_COMPONENTS_AND_INTERFACES);

        var links = persistenceHandler.loadArchitectureCodeTraceLinks();
        Assertions.assertTrue(links.isEmpty(), "TraceLink should have been deleted when its endpoint model was removed.");
        Long archItemsCount = neo4jClient.query("MATCH (n:ArchitectureItem) RETURN count(n)").fetchAs(Long.class).one().get();
        Assertions.assertEquals(0L, archItemsCount, "Architecture items were not cleaned up!");
        Long codeItemsCountAfter = neo4jClient.query("MATCH (n:CodeItem) RETURN count(n)").fetchAs(Long.class).one().get();
        Assertions.assertEquals(codeItemsCountBefore, codeItemsCountAfter, "Code items have been lost!");
    }

    @Test
    @DisplayName("Facade: Delete documentation and associated inconsistencies")
    void testDeleteDocumentationCascades() {
        Text text = TextFactory.createComplexText("doc_to_delete");
        persistenceHandler.savePreprocessedText(text, "doc_id");

        var textInc = new TextEntityAbsentFromModelInconsistency("Missing", text.getSentences().get(0).getSentenceNumber(), 1.0, null);
        persistenceHandler.addInconsistencies(List.of(textInc));

        persistenceHandler.deletePreprocessedText("doc_id");

        Assertions.assertFalse(persistenceHandler.hasPreprocessedText("doc_id"));
        Assertions.assertTrue(persistenceHandler.getInconsistencies().isEmpty(), "Inconsistency should be gone after text deletion.");

        Long wordCount = neo4jClient.query("MATCH (w:Word) RETURN count(w)").fetchAs(Long.class).one().get();
        Assertions.assertEquals(0L, wordCount, "Words from deleted text are still in the DB!");
    }

    @Test
    @DisplayName("Facade: Global deleteAllData should leave DB empty")
    void testDeleteAllData() {
        persistenceHandler.saveModel(Metamodel.ARCHITECTURE_WITH_COMPONENTS_AND_INTERFACES, ArchitectureModelFactory.createArchitectureModel());
        persistenceHandler.savePreprocessedText(TextFactory.createComplexText("test"), "test_id");

        persistenceHandler.deleteAllData();

        Long nodeCount = neo4jClient.query("MATCH (n) RETURN count(n)").fetchAs(Long.class).one().get();
        Assertions.assertEquals(0L, nodeCount, "Database is not empty after deleteAllData!");
    }

    @Test
    @DisplayName("Edge Case: Breaking a transitive chain by deleting the middle element")
    void testBreakTransitiveChain() {
        Text text = TextFactory.createComplexText("chain_doc");
        var archModel = ArchitectureModelFactory.createArchitectureModel();
        var codeModel = CodeModelFactory.createCodeModel();
        persistenceHandler.savePreprocessedText(text, "doc_id");
        persistenceHandler.saveModel(Metamodel.ARCHITECTURE_WITH_COMPONENTS_AND_INTERFACES, archModel);
        persistenceHandler.saveModel(Metamodel.CODE_WITH_COMPILATION_UNITS_AND_PACKAGES, codeModel);

        var archItem = archModel.getEndpoints().get(0);
        var link1 = new SentenceModelTraceLink(text.getSentences().get(0), archItem);
        var link2 = new ArchitectureCodeTraceLink(archItem, codeModel.getEndpoints().get(0));
        persistenceHandler.saveTraceLinks(List.of(link1, link2));

        persistenceHandler.deleteModel(Metamodel.ARCHITECTURE_WITH_COMPONENTS_AND_INTERFACES);
        Assertions.assertTrue(persistenceHandler.loadArchitectureCodeTraceLinks().isEmpty(), "Arch-Code link should be gone");
        Assertions.assertTrue(persistenceHandler.loadSentenceModelTraceLinks().isEmpty(), "Sentence-Arch link should be gone");
        Assertions.assertTrue(persistenceHandler.loadTransitiveTraceLinks().isEmpty(), "Transitive reconstruction should fail");
    }

    @Test
    @DisplayName("Edge Case: Replacing a model with a completely different ID under same Metamodel type")
    void testMetamodelBucketReplacement() {
        var modelOne = CodeModelFactory.createCodeModel(); // ID: "Model_A"
        persistenceHandler.saveModel(Metamodel.CODE_WITH_COMPILATION_UNITS_AND_PACKAGES, modelOne);

        var modelTwo = CodeModelFactory.createCodeModel(); // ID: "Model_B" (different UUID)
        persistenceHandler.saveModel(Metamodel.CODE_WITH_COMPILATION_UNITS_AND_PACKAGES, modelTwo);

        var loaded = (CodeModel) persistenceHandler.loadModel(Metamodel.CODE_WITH_COMPILATION_UNITS_AND_PACKAGES);
        Assertions.assertEquals(modelTwo.getId(), loaded.getId(), "The second model should have replaced the first");

        // Verify no nodes from Model One remain
        Long modelOneNodes = neo4jClient.query("MATCH (n:CodeItem {modelId: $id}) RETURN count(n)")
                .bind(modelOne.getId())
                .to("id")
                .fetchAs(Long.class)
                .one()
                .get();
        Assertions.assertEquals(0L, modelOneNodes, "Nodes from the first model still exist in the database");
    }

}
