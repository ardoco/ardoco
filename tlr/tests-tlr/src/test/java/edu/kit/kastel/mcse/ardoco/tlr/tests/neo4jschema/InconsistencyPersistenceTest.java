package edu.kit.kastel.mcse.ardoco.tlr.tests.neo4jschema;

import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.map.sorted.ImmutableSortedMap;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import edu.kit.kastel.mcse.ardoco.core.api.models.ModelFormat;
import edu.kit.kastel.mcse.ardoco.core.api.output.ArdocoResult;
import edu.kit.kastel.mcse.ardoco.core.api.stage.inconsistency.Inconsistency;
import edu.kit.kastel.mcse.ardoco.core.api.stage.inconsistency.InconsistentSentence;
import edu.kit.kastel.mcse.ardoco.core.api.stage.inconsistency.ModelInconsistency;
import edu.kit.kastel.mcse.ardoco.core.api.stage.inconsistency.TextInconsistency;
import edu.kit.kastel.mcse.ardoco.id.execution.runner.InconsistencyDetection;

public class InconsistencyPersistenceTest extends AbstractPersistenceTest {

    @Test
    void testInconsistencyDetectionWithNeo4j() {
        executeInconsistencyTest(true);
    }

    @Test
    void testInconsistencyDetectionWithoutPersistence() {
        executeInconsistencyTest(false);
    }

    private void executeInconsistencyTest(boolean persistence) {
        var runner = new InconsistencyDetection(projectName);
        ImmutableSortedMap<String, String> configs = getConfigsWithPersistence(persistence);
        System.out.println("Configs: " + configs);
        runner.setUp(inputText, inputModelArchitecture, ModelFormat.PCM, configs, outputDir);

        testRunnerAssertions(runner);
        ArdocoResult result = runner.run();
        Assertions.assertNotNull(result);
        ImmutableList<Inconsistency> inconsistencies = result.getAllInconsistencies();
        System.out.println("Found a total of " + inconsistencies.size() + " inconsistencies."); // 9
        Assertions.assertEquals(9, inconsistencies.size());

        ImmutableList<TextInconsistency> textInconsistencies = result.getAllTextInconsistencies();
        System.out.println("Found a total of " + textInconsistencies.size() + " text inconsistencies."); // 4 vs 24 on traceview website
        Assertions.assertEquals(4, textInconsistencies.size());

        ImmutableList<ModelInconsistency> modelInconsistencies = result.getAllModelInconsistencies();
        Assertions.assertEquals(5, modelInconsistencies.size());
        System.out.println("Found a total of " + modelInconsistencies.size() + " model inconsistencies."); // 5 (also 5 on traceview website)

        ImmutableList<InconsistentSentence> inconsistentSentences = result.getInconsistentSentences();
        System.out.println("Found a total of " + inconsistentSentences.size() + " inconsistent sentences."); //4
    }

}
