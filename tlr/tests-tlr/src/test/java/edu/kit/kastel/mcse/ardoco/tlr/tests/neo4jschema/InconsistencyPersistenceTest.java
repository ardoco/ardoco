/* Licensed under MIT 2026. */
package edu.kit.kastel.mcse.ardoco.tlr.tests.neo4jschema;

import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.map.sorted.ImmutableSortedMap;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import edu.kit.kastel.mcse.ardoco.core.api.models.ModelFormat;
import edu.kit.kastel.mcse.ardoco.core.api.output.ArdocoResult;
import edu.kit.kastel.mcse.ardoco.core.api.stage.inconsistency.Inconsistency;
import edu.kit.kastel.mcse.ardoco.core.api.stage.inconsistency.ModelInconsistency;
import edu.kit.kastel.mcse.ardoco.core.api.stage.inconsistency.TextInconsistency;
import edu.kit.kastel.mcse.ardoco.id.execution.runner.InconsistencyDetection;

public class InconsistencyPersistenceTest extends AbstractPersistenceTest {

    @Test
    void testInconsistencyDetectionWithNeo4j() throws Exception {
        executeInconsistencyTest(true);
    }

    @Test
    void testInconsistencyDetectionWithoutPersistence() throws Exception {
        executeInconsistencyTest(false);
    }

    private void executeInconsistencyTest(boolean persistence) throws Exception {
        var runner = new InconsistencyDetection(projectName);
        ImmutableSortedMap<String, String> configs = getInconsistencyConfigsWithPersistence(persistence);
        runner.setUp(inputText, inputModelArchitecture, ModelFormat.PCM, configs, outputDir);

        testRunnerAssertions(runner);
        ArdocoResult result = runner.run();
        Assertions.assertNotNull(result);
        ImmutableList<Inconsistency> inconsistencies = result.getAllInconsistencies(); // 9
        Assertions.assertEquals(29, inconsistencies.size());

        ImmutableList<TextInconsistency> textInconsistencies = result.getAllTextInconsistencies(); // 24 on traceview website
        Assertions.assertEquals(24, textInconsistencies.size());

        ImmutableList<ModelInconsistency> modelInconsistencies = result.getAllModelInconsistencies();
        Assertions.assertEquals(5, modelInconsistencies.size()); // 5 on traceview website
    }

}
