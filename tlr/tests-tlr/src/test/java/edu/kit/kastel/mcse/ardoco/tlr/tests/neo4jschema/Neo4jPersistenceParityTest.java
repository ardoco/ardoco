/* Licensed under MIT 2026. */
package edu.kit.kastel.mcse.ardoco.tlr.tests.neo4jschema;

import java.io.File;
import java.util.SortedSet;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import edu.kit.kastel.mcse.ardoco.core.api.models.ModelFormat;
import edu.kit.kastel.mcse.ardoco.core.api.output.ArdocoResult;
import edu.kit.kastel.mcse.ardoco.core.api.stage.recommendationgenerator.RecommendationStates;
import edu.kit.kastel.mcse.ardoco.core.api.stage.textextraction.TextState;
import edu.kit.kastel.mcse.ardoco.tlr.execution.Swattr;
import edu.kit.kastel.mcse.ardoco.tlr.models.agents.ArchitectureConfiguration;
import edu.kit.kastel.mcse.ardoco.tlr.tests.neo4jschema.util.PersistenceTestSupport;

/**
 * Ensures Neo4j TextState / Recommendation dual-write does not change SWATTR semantic outputs.
 */
class Neo4jPersistenceParityTest extends AbstractPersistenceTest {

    @Test
    @DisplayName("SWATTR with Neo4j fully off vs text/recommendation flags on yields same NM/RI keys and architecture trace links")
    void testSwattrPersistenceParity() {
        Snapshot baseline = runSwattrAndSnapshot(false, false, false);
        Snapshot withTextAndRecommendations = runSwattrAndSnapshot(true, true, true);

        Assertions.assertEquals(baseline.nounMappingCount, withTextAndRecommendations.nounMappingCount, "NounMapping list sizes must match");
        Assertions.assertEquals(baseline.nounMappingKeys, withTextAndRecommendations.nounMappingKeys, "NounMapping (reference, kind) sets must match");
        Assertions.assertEquals(baseline.recommendedInstanceCount, withTextAndRecommendations.recommendedInstanceCount,
                "RecommendedInstance list sizes must match");
        Assertions.assertEquals(baseline.recommendedInstanceKeys, withTextAndRecommendations.recommendedInstanceKeys,
                "RecommendedInstance (name, type) sets must match");
        Assertions.assertEquals(baseline.architectureTraceLinkKeys, withTextAndRecommendations.architectureTraceLinkKeys,
                "Architecture trace-link endpoints must match");

        pauseForNeo4jInspection();
    }

    private Snapshot runSwattrAndSnapshot(boolean usePersistence, boolean persistTextState, boolean persistRecommendations) {
        clearNeo4jGraph();
        var runner = new Swattr(projectName);
        runner.setUp(inputText, new ArchitectureConfiguration(new File(inputModelArchitecture), ModelFormat.PCM),
                getConfigsWithPersistence(usePersistence, persistTextState, persistRecommendations), outputDir);
        testRunnerAssertions(runner);
        ArdocoResult result = runner.run();
        Assertions.assertNotNull(result);

        var dataRepository = runner.getArdoco().getDataRepository();
        var activeMetamodels = PersistenceTestSupport.activeMetamodels(dataRepository);
        TextState textState = dataRepository.getData(TextState.ID, TextState.class).orElseThrow();
        RecommendationStates recommendationStates = dataRepository.getData(RecommendationStates.ID, RecommendationStates.class).orElseThrow();

        SortedSet<String> nounMappingKeys = PersistenceTestSupport.nounMappingSemanticKeys(textState);
        SortedSet<String> recommendedInstanceKeys = PersistenceTestSupport.recommendedInstanceSemanticKeys(recommendationStates, activeMetamodels);
        SortedSet<String> architectureTraceLinkKeys = PersistenceTestSupport.architectureTraceLinkSemanticKeys(result);

        return new Snapshot(textState.getNounMappings().size(), nounMappingKeys,
                PersistenceTestSupport.countRecommendedInstances(recommendationStates, activeMetamodels), recommendedInstanceKeys,
                architectureTraceLinkKeys);
    }

    private record Snapshot(int nounMappingCount, SortedSet<String> nounMappingKeys, long recommendedInstanceCount,
            SortedSet<String> recommendedInstanceKeys, SortedSet<String> architectureTraceLinkKeys) {
    }
}
