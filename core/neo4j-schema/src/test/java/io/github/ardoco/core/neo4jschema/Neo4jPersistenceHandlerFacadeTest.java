package io.github.ardoco.core.neo4jschema;

import edu.kit.kastel.mcse.ardoco.core.api.entity.ModelEntity;
import edu.kit.kastel.mcse.ardoco.core.api.models.ArchitectureModelWithComponentsAndInterfaces;
import edu.kit.kastel.mcse.ardoco.core.api.models.CodeModel;
import edu.kit.kastel.mcse.ardoco.core.api.models.CodeModelWithCompilationUnitsAndPackages;
import edu.kit.kastel.mcse.ardoco.core.api.models.Metamodel;
import edu.kit.kastel.mcse.ardoco.core.api.stage.codetraceability.ArchitectureCodeTraceLink;
import edu.kit.kastel.mcse.ardoco.core.api.stage.connectiongenerator.SentenceModelTraceLink;

import edu.kit.kastel.mcse.ardoco.core.api.text.Sentence;

import edu.kit.kastel.mcse.ardoco.core.api.text.Text;
import edu.kit.kastel.mcse.ardoco.core.api.tracelink.TransitiveTraceLink;
import edu.kit.kastel.mcse.ardoco.id.types.ModelEntityAbsentFromTextInconsistency;
import edu.kit.kastel.mcse.ardoco.id.types.TextEntityAbsentFromModelInconsistency;
import io.github.ardoco.core.neo4jschema.adapter.Neo4jTextInconsistency;
import io.github.ardoco.core.neo4jschema.util.FakeCodeEntity;
import io.github.ardoco.core.neo4jschema.util.FakeSentence;

import io.github.ardoco.core.neo4jschema.util.models.ArchitectureModelEqualityHelper;
import io.github.ardoco.core.neo4jschema.util.models.ArchitectureModelFactory;
import io.github.ardoco.core.neo4jschema.util.models.CodeModelEqualityHelper;
import io.github.ardoco.core.neo4jschema.util.models.CodeModelFactory;

import io.github.ardoco.core.neo4jschema.util.models.TextEqualityHelper;
import io.github.ardoco.core.neo4jschema.util.models.TextFactory;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.neo4j.core.Neo4jClient;

@SpringBootTest(classes = io.github.ardoco.core.neo4jschema.Main.class)
public class Neo4jPersistenceHandlerFacadeTest extends AbstractNeo4jTest {

    @Autowired
    protected Neo4jPersistenceHandler persistenceHandler;

    @Autowired
    private Neo4jClient neo4jClient;

    @BeforeEach
    void clearDatabase() {
        neo4jClient.query("MATCH (n) DETACH DELETE n").run();
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
    @DisplayName("Facade: Test Roundtrip for Architecture-Code Trace Links")
    void testArchitectureCodeTraceLinkPersistence() {
        var archModel = ArchitectureModelFactory.createArchitectureModel();
        var codeModel = CodeModelFactory.createCodeModel();
        persistenceHandler.saveModel(Metamodel.ARCHITECTURE_WITH_COMPONENTS_AND_INTERFACES, archModel);
        persistenceHandler.saveModel(Metamodel.CODE_WITH_COMPILATION_UNITS_AND_PACKAGES, codeModel);

        var archEntity = archModel.getEndpoints().getFirst();
        var codeEntity = codeModel.getEndpoints().getFirst();

        ArchitectureCodeTraceLink link = new ArchitectureCodeTraceLink(archEntity, codeEntity);

        persistenceHandler.saveSamCodeTraceLinks(List.of(link));
        var loadedLinks = persistenceHandler.loadSamCodeTraceLinks();

        Assertions.assertFalse(loadedLinks.isEmpty());
        //TODO: currently we only load the nodes itself but not their relationships, to not having to load the entire models. This means that the loaded links are not fully functional and only contain the IDs of the connected entities. For a more robust test, we would need to load the entire models and check if the relationships are correctly established. For now, we can only check if a link with the same IDs exists.

        Assertions.assertTrue(loadedLinks.stream().anyMatch(l ->
                l.getFirstEndpoint().getId().equals(archEntity.getId())
                && l.getSecondEndpoint().getId().equals(codeEntity.getId())
                && l.getFirstEndpoint().getName().equals(archEntity.getName())
                && l.getSecondEndpoint().getName().equals(codeEntity.getName())
        ));
//        Assertions.assertTrue(loadedLinks.stream().anyMatch(l -> l.equals(link)));
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

        persistenceHandler.saveTransitiveTraceLinks(List.of(transitiveLink));
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

        persistenceHandler.saveSentenceModelTraceLinks(List.of(link));
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


}
