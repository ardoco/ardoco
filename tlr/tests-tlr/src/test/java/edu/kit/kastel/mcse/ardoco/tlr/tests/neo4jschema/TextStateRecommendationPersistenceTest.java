/* Licensed under MIT 2026. */
package edu.kit.kastel.mcse.ardoco.tlr.tests.neo4jschema;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import edu.kit.kastel.mcse.ardoco.core.api.models.Metamodel;
import edu.kit.kastel.mcse.ardoco.core.api.models.ModelFormat;
import edu.kit.kastel.mcse.ardoco.core.api.output.ArdocoResult;
import edu.kit.kastel.mcse.ardoco.core.api.stage.textextraction.NounMapping;
import edu.kit.kastel.mcse.ardoco.core.api.text.Text;
import edu.kit.kastel.mcse.ardoco.core.common.persistence.PersistenceBridge;
import edu.kit.kastel.mcse.ardoco.core.data.PreprocessingData;
import edu.kit.kastel.mcse.ardoco.tlr.execution.Swattr;
import edu.kit.kastel.mcse.ardoco.tlr.models.agents.ArchitectureConfiguration;

/**
 * Phase 4: SWATTR dual-write of TextState and RecommendationStates is gated by flags.
 * Existing {@link TraceLinkPersistenceTest} stays Stage 0 unless Maven {@code -D} flags are set.
 */
public class TextStateRecommendationPersistenceTest extends AbstractPersistenceTest {

    @Test
    @DisplayName("SWATTR with Neo4j does not write NounMapping or RecommendedInstance when new flags are off")
    void testSwattrDoesNotWriteTextStateOrRecommendationsWhenFlagsOff() {
        runSwattr(true, false, false);

        Assertions.assertEquals(0, countNodesWithLabel("NounMapping"));
        Assertions.assertEquals(0, countNodesWithLabel("RecommendedInstance"));
        Assertions.assertEquals(0, countRelationshipsWithType("MAPS_WORD"));
        Assertions.assertEquals(0, countRelationshipsWithType("HAS_NAME_MAPPING"));
        Assertions.assertTrue(countRelationshipsWithType("TRACES_TO") > 0);
    }

    @Test
    @DisplayName("SWATTR with Neo4j writes NounMappings when persistTextState is on")
    void testSwattrWritesNounMappingsWhenTextStateFlagOn() {
        runSwattr(true, true, false);

        Assertions.assertTrue(countNodesWithLabel("NounMapping") > 0);
        Assertions.assertTrue(countRelationshipsWithType("MAPS_WORD") > 0);
        Assertions.assertTrue(countRelationshipsWithType("IN_PHRASE") > 0,
                "NounMappings should link to Phrase nodes via contained word positions");
        Assertions.assertEquals(0, countNodesWithLabel("RecommendedInstance"));
        Assertions.assertEquals(0, countRelationshipsWithType("HAS_NAME_MAPPING"));
    }

    @Test
    @DisplayName("SWATTR with persistRecommendations only still writes NounMappings (TextState auto-enabled)")
    void testSwattrRecommendationsOnlyAutoEnablesTextState() {
        runSwattr(true, false, true);

        Assertions.assertTrue(countNodesWithLabel("NounMapping") > 0);
        Assertions.assertTrue(countNodesWithLabel("RecommendedInstance") > 0);
        Assertions.assertTrue(countRelationshipsWithType("HAS_NAME_MAPPING") > 0);
    }

    @Test
    @DisplayName("SWATTR with Neo4j writes RecommendedInstances linked to NounMappings when both flags are on")
    void testSwattrWritesRecommendedInstancesWhenBothFlagsOn() {
        runSwattr(true, true, true);

        Assertions.assertTrue(countNodesWithLabel("NounMapping") > 0);
        Assertions.assertTrue(countNodesWithLabel("RecommendedInstance") > 0);
        Assertions.assertTrue(countRelationshipsWithType("HAS_NAME_MAPPING") > 0);
        Assertions.assertTrue(countRelationshipsWithType("TRACES_TO") > 0);
        Assertions.assertTrue(countRecommendationArchitectureLinks() > 0,
                "ConnectionState instance links (RI→Architecture) should be persisted");
    }

    @Test
    @DisplayName("Resume hydrate reloads NounMappings and RecommendedInstances from Neo4j without re-running SWATTR")
    void testLoadOnResumeHydratesTextStateAndRecommendations() {
        runSwattr(true, true, true);

        long nounMappingCount = countNodesWithLabel("NounMapping");
        long recommendedInstanceCount = countNodesWithLabel("RecommendedInstance");
        Assertions.assertTrue(nounMappingCount > 0);
        Assertions.assertTrue(recommendedInstanceCount > 0);

        var handler = PersistenceBridge.getHandler();
        Assertions.assertNotNull(handler);
        Assertions.assertTrue(handler.hasNounMappings());
        Assertions.assertTrue(handler.hasRecommendedInstances());

        Text annotatedText = handler.loadPreprocessedText(PreprocessingData.ID);
        Assertions.assertNotNull(annotatedText);

        Collection<NounMapping> loadedMappings = handler.loadNounMappings(annotatedText);
        Assertions.assertEquals(nounMappingCount, loadedMappings.size());

        Map<String, NounMapping> byId = new HashMap<>();
        for (NounMapping mapping : loadedMappings) {
            byId.put(mapping.getArdocoId(), mapping);
        }

        long loadedRis = 0;
        for (Metamodel metamodel : Metamodel.values()) {
            loadedRis += handler.loadRecommendedInstances(metamodel, byId).size();
        }
        Assertions.assertEquals(recommendedInstanceCount, loadedRis);
        Assertions.assertTrue(handler.hasRecommendationModelTraceLinks());
    }

    private long countRecommendationArchitectureLinks() {
        return neo4jClient.query("""
                MATCH (:RecommendedInstance)-[r:TRACES_TO]->(:Traceable)
                WHERE r.traceLinkType = 'RECOMMENDATION_ARCHITECTURE'
                RETURN count(r) AS c
                """)
                .fetch()
                .one()
                .map(row -> ((Number) row.get("c")).longValue())
                .orElse(0L);
    }

    private void runSwattr(boolean usePersistence, boolean persistTextState, boolean persistRecommendations) {
        clearNeo4jGraph();
        var runner = new Swattr(projectName);
        runner.setUp(inputText, new ArchitectureConfiguration(new File(inputModelArchitecture), ModelFormat.PCM), getConfigsWithPersistence(usePersistence,
                persistTextState, persistRecommendations), outputDir);

        testRunnerAssertions(runner);
        ArdocoResult result = runner.run();
        Assertions.assertNotNull(result);
        Assertions.assertEquals(20, result.getArchitectureTraceLinks().size());

        pauseForNeo4jInspection();
    }
}
