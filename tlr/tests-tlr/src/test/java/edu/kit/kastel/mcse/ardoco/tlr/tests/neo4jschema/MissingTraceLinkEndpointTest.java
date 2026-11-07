/* Licensed under MIT 2026. */
package edu.kit.kastel.mcse.ardoco.tlr.tests.neo4jschema;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import edu.kit.kastel.mcse.ardoco.core.api.models.ModelFormat;
import edu.kit.kastel.mcse.ardoco.core.api.stage.connectiongenerator.RecommendationModelTraceLink;
import edu.kit.kastel.mcse.ardoco.core.common.persistence.PersistenceBridge;
import edu.kit.kastel.mcse.ardoco.core.common.persistence.PersistenceHandler;
import edu.kit.kastel.mcse.ardoco.tlr.execution.Transarc;
import edu.kit.kastel.mcse.ardoco.tlr.models.agents.ArchitectureConfiguration;
import io.github.ardoco.core.neo4jschema.service.MissingTraceLinkEndpointException;

class MissingTraceLinkEndpointTest extends AbstractPersistenceTest {

    @Test
    @DisplayName("A trace link endpoint deleted mid-run fails the run, wipes the database and exports nothing")
    void endpointDeletedMidRunFailsTheRun(@TempDir File runOutputDirectory) {
        clearNeo4jGraph();
        var runner = new Transarc(projectName);
        runner.setUp(new File(inputText), new ArchitectureConfiguration(new File(inputModelArchitecture), ModelFormat.PCM), codeConfiguration,
                getConfigsWithNeo4jStorage(), runOutputDirectory);
        testRunnerAssertions(runner);

        PersistenceHandler realHandler = PersistenceBridge.getHandler();
        Assertions.assertNotNull(realHandler);
        List<String> deletedIds = new ArrayList<>();
        PersistenceBridge.setHandler(deletingFirstRecommendedInstance(realHandler, deletedIds));
        MissingTraceLinkEndpointException thrown;
        try {
            thrown = Assertions.assertThrows(MissingTraceLinkEndpointException.class, runner::run);
        } finally {
            PersistenceBridge.setHandler(realHandler);
        }

        Assertions.assertEquals(1, deletedIds.size(), "exactly one linked RecommendedInstance must have been deleted mid-run");
        String deletedId = deletedIds.getFirst();
        Assertions.assertTrue(thrown.getMessage().contains(deletedId), thrown.getMessage());
        Assertions.assertTrue(thrown.getMessage().contains("RECOMMENDATION_"), thrown.getMessage());
        Assertions.assertEquals(0L, countAllNodes(), "a failed run must leave the database empty");
        Assertions.assertFalse(new File(runOutputDirectory, "knowledgeGraph_" + projectName + ".json").exists(),
                "a failed run must not export a knowledge graph");
        String[] writtenFiles = runOutputDirectory.list();
        Assertions.assertTrue(writtenFiles == null || writtenFiles.length == 0, "a failed run must not write output files: " + List.of(writtenFiles == null ?
                new String[0] :
                writtenFiles));
    }

    private PersistenceHandler deletingFirstRecommendedInstance(PersistenceHandler real, List<String> deletedIds) {
        return (PersistenceHandler) Proxy.newProxyInstance(PersistenceHandler.class.getClassLoader(), new Class<?>[] { PersistenceHandler.class }, (proxy,
                method, args) -> {
            if (method.getName().equals("saveTraceLinks") && deletedIds.isEmpty()) {
                for (Object link : (Collection<?>) args[0]) {
                    if (link instanceof RecommendationModelTraceLink recommendationLink) {
                        String id = recommendationLink.getFirstEndpoint().getId();
                        neo4jClient.query("MATCH (ri:RecommendedInstance {ardocoId: $id}) DETACH DELETE ri").bindAll(Map.of("id", id)).run();
                        deletedIds.add(id);
                        break;
                    }
                }
            }
            try {
                return method.invoke(real, args);
            } catch (InvocationTargetException e) {
                throw e.getCause();
            }
        });
    }

    private long countAllNodes() {
        return neo4jClient.query("MATCH (n) RETURN count(n) AS c").fetch().one().map(row -> ((Number) row.get("c")).longValue()).orElse(0L);
    }
}
