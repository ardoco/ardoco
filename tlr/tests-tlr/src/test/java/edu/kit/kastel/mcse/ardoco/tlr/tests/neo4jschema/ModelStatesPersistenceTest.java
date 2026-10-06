/* Licensed under MIT 2026. */
package edu.kit.kastel.mcse.ardoco.tlr.tests.neo4jschema;

import static edu.kit.kastel.mcse.ardoco.tlr.tests.neo4jschema.util.ArchitectureModelEqualityHelper.assertArchitectureModelsEqual;
import static edu.kit.kastel.mcse.ardoco.tlr.tests.neo4jschema.util.CodeModelEqualityHelper.assertCodeModelsEqual;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import edu.kit.kastel.mcse.ardoco.core.api.models.ArchitectureModel;
import edu.kit.kastel.mcse.ardoco.core.api.models.CodeModel;
import edu.kit.kastel.mcse.ardoco.core.api.models.Metamodel;
import edu.kit.kastel.mcse.ardoco.core.api.models.ModelStates;
import edu.kit.kastel.mcse.ardoco.core.common.persistence.PersistenceBridge;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.architecture.uml.UmlExtractor;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.code.CodeExtractor;
import io.github.ardoco.core.neo4jschema.Neo4jPersistenceHandler;

/**
 * Regression for {@link ModelStates} reload policy (Option A): save must not mark models dirty and shrink in-memory graphs on the next {@code getModel}.
 */
class ModelStatesPersistenceTest extends AbstractPersistenceTest {

    @Autowired
    private Neo4jPersistenceHandler persistenceHandler;

    @Test
    @DisplayName("ModelStates addModel then getModel does not shrink endpoint count")
    void testAddModelThenGetModelPreservesEndpoints() {
        PersistenceBridge.setHandler(persistenceHandler);
        PersistenceBridge.getInstance()
                .applyConfiguration(getConfigsWithPersistence(true, false, false));

        UmlExtractor extractor = new UmlExtractor(this.inputModelArchitectureUml, Metamodel.ARCHITECTURE_WITH_COMPONENTS);
        ArchitectureModel original = extractor.extractModel();
        int endpointsBefore = original.getEndpoints().size();
        int contentBefore = original.getContent().size();
        Assertions.assertTrue(endpointsBefore > 0);

        ModelStates modelStates = new ModelStates();
        modelStates.addModel(Metamodel.ARCHITECTURE_WITH_COMPONENTS, original);

        ArchitectureModel afterGet = (ArchitectureModel) modelStates.getModel(Metamodel.ARCHITECTURE_WITH_COMPONENTS);
        Assertions.assertSame(original, afterGet, "getModel must return the in-memory instance, not reload from Neo4j");
        Assertions.assertEquals(endpointsBefore, afterGet.getEndpoints().size(),
                "getModel after save must not replace in-memory model with a smaller graph");
        Assertions.assertEquals(contentBefore, afterGet.getContent().size());
        assertArchitectureModelsEqual(original, afterGet);

        pauseForNeo4jInspection();
    }

    @Test
    @DisplayName("ModelStates loads architecture model from Neo4j when absent in memory")
    void testGetModelLoadsFromNeo4jOnResume() {
        PersistenceBridge.setHandler(persistenceHandler);
        PersistenceBridge.getInstance()
                .applyConfiguration(getConfigsWithPersistence(true, false, false));

        UmlExtractor extractor = new UmlExtractor(this.inputModelArchitectureUml, Metamodel.ARCHITECTURE_WITH_COMPONENTS);
        ArchitectureModel original = extractor.extractModel();
        persistenceHandler.saveModel(Metamodel.ARCHITECTURE_WITH_COMPONENTS, original);

        ModelStates modelStates = new ModelStates();
        ArchitectureModel loaded = (ArchitectureModel) modelStates.getModel(Metamodel.ARCHITECTURE_WITH_COMPONENTS);
        assertArchitectureModelsEqual(original, loaded);

        pauseForNeo4jInspection();
    }

    @Test
    @DisplayName("ModelStates loads code model from Neo4j when absent in memory")
    void testGetModelLoadsCodeModelFromNeo4jOnResume() {
        PersistenceBridge.setHandler(persistenceHandler);
        PersistenceBridge.getInstance()
                .applyConfiguration(getConfigsWithPersistence(true, false, false));

        CodeModel original = CodeExtractor.readInCodeModel(codeConfiguration.code(), Metamodel.CODE_WITH_COMPILATION_UNITS);
        persistenceHandler.saveModel(Metamodel.CODE_WITH_COMPILATION_UNITS, original);

        ModelStates modelStates = new ModelStates();
        CodeModel loaded = (CodeModel) modelStates.getModel(Metamodel.CODE_WITH_COMPILATION_UNITS);
        assertCodeModelsEqual(original, loaded);
        Assertions.assertEquals(0, countModelItemsMissingTraceableLabel(),
                "CodeItem nodes persisted with the code model must also be labeled Traceable");

        pauseForNeo4jInspection();
    }
}
