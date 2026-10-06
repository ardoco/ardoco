/* Licensed under MIT 2026. */
package edu.kit.kastel.mcse.ardoco.tlr.tests.neo4jschema;

import java.io.File;
import java.lang.reflect.Method;

import org.eclipse.collections.api.map.sorted.ImmutableSortedMap;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import edu.kit.kastel.mcse.ardoco.core.api.models.Metamodel;
import edu.kit.kastel.mcse.ardoco.core.api.models.ModelFormat;
import edu.kit.kastel.mcse.ardoco.core.api.models.ModelStates;
import edu.kit.kastel.mcse.ardoco.core.api.output.ArdocoResult;
import edu.kit.kastel.mcse.ardoco.core.api.stage.connectiongenerator.ConnectionStates;
import edu.kit.kastel.mcse.ardoco.core.api.stage.recommendationgenerator.RecommendationStates;
import edu.kit.kastel.mcse.ardoco.core.api.stage.textextraction.TextState;
import edu.kit.kastel.mcse.ardoco.core.common.persistence.PersistenceBridge;
import edu.kit.kastel.mcse.ardoco.core.data.DataRepository;
import edu.kit.kastel.mcse.ardoco.core.pipeline.AbstractExecutionStage;
import edu.kit.kastel.mcse.ardoco.tlr.connectiongenerator.ConnectionGenerator;
import edu.kit.kastel.mcse.ardoco.tlr.execution.Swattr;
import edu.kit.kastel.mcse.ardoco.tlr.models.agents.ArchitectureConfiguration;
import edu.kit.kastel.mcse.ardoco.tlr.recommendationgenerator.RecommendationGenerator;
import edu.kit.kastel.mcse.ardoco.tlr.tests.neo4jschema.util.PersistenceTestSupport;
import edu.kit.kastel.mcse.ardoco.tlr.textextraction.TextExtraction;
import io.github.ardoco.core.neo4jschema.Neo4jPersistenceHandler;

/**
 * Simulates pipeline resume: Neo4j populated by SWATTR, fresh {@link DataRepository}, stage {@code initializeState} only.
 */
class Neo4jResumeIntegrationTest extends AbstractPersistenceTest {

    @Autowired
    private Neo4jPersistenceHandler persistenceHandler;

    @BeforeEach
    void wirePersistenceBridge() {
        PersistenceBridge.setHandler(persistenceHandler);
    }

    @Test
    @DisplayName("Fresh DataRepository hydrates TextState, Recommendations, and Connection links from Neo4j")
    void testStageInitializeStateResumesFromNeo4j() throws Exception {
        ImmutableSortedMap<String, String> configs = getConfigsWithPersistence(true, true, true);
        PersistenceBridge.getInstance().applyConfiguration(configs);

        clearNeo4jGraph();
        var runner = new Swattr(projectName);
        runner.setUp(inputText, new ArchitectureConfiguration(new File(inputModelArchitecture), ModelFormat.PCM), configs, outputDir);
        testRunnerAssertions(runner);
        ArdocoResult result = runner.run();
        Assertions.assertNotNull(result);

        DataRepository sourceRepository = runner.getArdoco().getDataRepository();
        var activeMetamodels = PersistenceTestSupport.activeMetamodels(sourceRepository);
        int expectedNounMappings = sourceRepository.getData(TextState.ID, TextState.class).orElseThrow().getNounMappings().size();
        RecommendationStates sourceRecommendations = sourceRepository.getData(RecommendationStates.ID, RecommendationStates.class).orElseThrow();
        var expectedRiSemanticKeys = PersistenceTestSupport.recommendedInstanceSemanticKeys(sourceRecommendations, activeMetamodels);
        var expectedRiMappingKeys = PersistenceTestSupport.recommendedInstanceNounMappingKeys(sourceRecommendations, activeMetamodels);
        long expectedRecommendedInstances = PersistenceTestSupport.countRecommendedInstances(sourceRecommendations, activeMetamodels);
        ConnectionStates sourceConnections = sourceRepository.getData(ConnectionStates.ID, ConnectionStates.class).orElseThrow();
        long expectedArchLinks = sourceConnections.getConnectionState(Metamodel.ARCHITECTURE_WITH_COMPONENTS).getInstanceLinks().size();

        Assertions.assertTrue(expectedNounMappings > 0);
        Assertions.assertTrue(expectedRecommendedInstances > 0);
        Assertions.assertTrue(expectedArchLinks > 0);

        Assertions.assertEquals(expectedNounMappings, countNodesWithLabel("NounMapping"), "Neo4j NounMapping count should match in-memory pipeline");
        Assertions.assertEquals(expectedRecommendedInstances, countNodesWithLabel("RecommendedInstance"));
        Assertions.assertEquals(expectedArchLinks, countRecommendationArchitectureLinks(),
                "Neo4j RECOMMENDATION_ARCHITECTURE links should match in-memory instance links after SWATTR");
        Assertions.assertEquals(0, countModelItemsMissingTraceableLabel(),
                "ArchitectureItem and CodeItem nodes must also be labeled Traceable for indexed link writes");

        DataRepository freshRepository = new DataRepository();
        ModelStates modelStates = new ModelStates();
        freshRepository.addData(ModelStates.ID, modelStates);
        modelStates.getModel(Metamodel.ARCHITECTURE_WITH_COMPONENTS);

        invokeInitializeState(TextExtraction.get(configs, freshRepository));
        TextState textState = freshRepository.getData(TextState.ID, TextState.class).orElseThrow();
        Assertions.assertEquals(expectedNounMappings, textState.getNounMappings().size());

        invokeInitializeState(RecommendationGenerator.get(configs, freshRepository));
        RecommendationStates recommendationStates = freshRepository.getData(RecommendationStates.ID, RecommendationStates.class).orElseThrow();
        long loadedRis = PersistenceTestSupport.countRecommendedInstances(recommendationStates, activeMetamodels);
        Assertions.assertEquals(expectedRecommendedInstances, loadedRis);
        Assertions.assertEquals(expectedRiSemanticKeys, PersistenceTestSupport.recommendedInstanceSemanticKeys(recommendationStates, activeMetamodels));
        Assertions.assertEquals(expectedRiMappingKeys, PersistenceTestSupport.recommendedInstanceNounMappingKeys(recommendationStates, activeMetamodels));

        invokeInitializeState(ConnectionGenerator.get(configs, freshRepository));
        ConnectionStates connectionStates = freshRepository.getData(ConnectionStates.ID, ConnectionStates.class).orElseThrow();
        long hydratedLinks = connectionStates.getConnectionState(Metamodel.ARCHITECTURE_WITH_COMPONENTS).getInstanceLinks().size();
        Assertions.assertEquals(expectedArchLinks, hydratedLinks);

        Assertions.assertEquals(0, countRecommendedInstancesWithoutMappingEdges(),
                "Every persisted RecommendedInstance must have HAS_NAME_MAPPING or HAS_TYPE_MAPPING");

        pauseForNeo4jInspection();
    }

    private static void invokeInitializeState(AbstractExecutionStage stage) throws Exception {
        Method initializeState = stage.getClass().getDeclaredMethod("initializeState");
        initializeState.setAccessible(true);
        initializeState.invoke(stage);
    }

    private long countRecommendationArchitectureLinks() {
        return neo4jClient.query("""
                MATCH (:RecommendedInstance)-[r:TRACES_TO]->()
                WHERE r.traceLinkType = 'RECOMMENDATION_ARCHITECTURE'
                RETURN count(r) AS c
                """)
                .fetch()
                .one()
                .map(row -> ((Number) row.get("c")).longValue())
                .orElse(0L);
    }

    private long countRecommendedInstancesWithoutMappingEdges() {
        return neo4jClient.query("""
                MATCH (ri:RecommendedInstance)
                WHERE NOT (ri)-[:HAS_NAME_MAPPING|:HAS_TYPE_MAPPING]->()
                RETURN count(ri) AS c
                """)
                .fetch()
                .one()
                .map(row -> ((Number) row.get("c")).longValue())
                .orElse(0L);
    }
}
