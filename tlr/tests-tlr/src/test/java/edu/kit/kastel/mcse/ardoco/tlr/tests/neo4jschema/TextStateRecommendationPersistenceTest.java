/* Licensed under MIT 2026. */
package edu.kit.kastel.mcse.ardoco.tlr.tests.neo4jschema;

import java.io.File;
import java.util.Set;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import edu.kit.kastel.mcse.ardoco.core.api.models.ModelFormat;
import edu.kit.kastel.mcse.ardoco.core.api.output.ArdocoResult;
import edu.kit.kastel.mcse.ardoco.tlr.execution.Swattr;
import edu.kit.kastel.mcse.ardoco.tlr.models.agents.ArchitectureConfiguration;
import edu.kit.kastel.mcse.ardoco.tlr.tests.neo4jschema.util.PersistenceTestSupport;

public class TextStateRecommendationPersistenceTest extends AbstractPersistenceTest {

    @Test
    @DisplayName("SWATTR with storage=neo4j exports NounMappings and RecommendedInstances as a consistent graph")
    void testSwattrNeo4jStorageWritesConsistentTextStateAndRecommendations() {
        clearNeo4jGraph();
        deleteExportedGraph();
        var runner = new Swattr(projectName);
        runner.setUp(inputText, new ArchitectureConfiguration(new File(inputModelArchitecture), ModelFormat.PCM), getConfigsWithNeo4jStorage(), outputDir);

        testRunnerAssertions(runner);
        ArdocoResult result = runner.run();
        Assertions.assertNotNull(result);
        Assertions.assertEquals(20, result.getArchitectureTraceLinks().size());

        PersistenceTestSupport.ExportedGraph graph = readExportedGraph();
        Assertions.assertTrue(graph.countNodesWithLabel("NounMapping") > 0);
        Assertions.assertTrue(graph.countNodesWithLabel("RecommendedInstance") > 0);
        Assertions.assertTrue(graph.countRelationshipsWithType("MAPS_WORD") > 0);
        Assertions.assertTrue(graph.countRelationshipsWithType("IN_PHRASE") > 0, "NounMappings should link to Phrase nodes via contained word positions");
        Assertions.assertTrue(graph.countRelationshipsWithType("HAS_NAME_MAPPING") > 0);
        Assertions.assertTrue(graph.countRelationshipsWithType("TRACES_TO") > 0);
        Assertions.assertTrue(graph.countTraceLinksOfType("RECOMMENDATION_ARCHITECTURE") > 0,
                "ConnectionState instance links (RI to architecture) should be persisted");
        Assertions.assertEquals(0, graph.countNodesWithLabelWithoutOutgoing("RecommendedInstance", Set.of("HAS_NAME_MAPPING", "HAS_TYPE_MAPPING")),
                "Every persisted RecommendedInstance must have HAS_NAME_MAPPING or HAS_TYPE_MAPPING");
        Assertions.assertEquals(0, graph.countNodesWithLabelWithoutOutgoing("NounMapping", Set.of("MAPS_WORD")),
                "Every persisted NounMapping must map at least one Word");
        Assertions.assertEquals(0L, countAllNodesInDatabase(), "storage=neo4j must leave the database empty after the run");
    }

    private long countAllNodesInDatabase() {
        return neo4jClient.query("MATCH (n) RETURN count(n) AS c").fetch().one().map(row -> ((Number) row.get("c")).longValue()).orElse(0L);
    }
}
