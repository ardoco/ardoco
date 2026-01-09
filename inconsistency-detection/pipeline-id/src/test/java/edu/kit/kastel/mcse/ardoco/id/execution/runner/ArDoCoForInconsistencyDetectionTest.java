/* Licensed under MIT 2023-2026. */
package edu.kit.kastel.mcse.ardoco.id.execution.runner;

import java.io.File;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import edu.kit.kastel.mcse.ardoco.core.api.models.ModelFormat;
import edu.kit.kastel.mcse.ardoco.core.execution.ConfigurationHelper;
import edu.kit.kastel.mcse.ardoco.core.execution.RunnerBaseTest;

class InconsistencyDetectionTest extends RunnerBaseTest {

    @Test
    @DisplayName("Test ARDoCo for Inconsistency Detection (PCM)")
    void testInconsistencyDetectionPcm() {
        var runner = new InconsistencyDetection(projectName);
        File additionalConfigsFile = new File(additionalConfigs);
        var additionalConfigsMap = ConfigurationHelper.loadAdditionalConfigs(additionalConfigsFile);
        runner.setUp(inputText, inputModelArchitecture, ModelFormat.PCM, additionalConfigsMap, outputDir);

        testRunnerAssertions(runner);
        Assertions.assertNotNull(runner.run());
    }

    @Test
    @DisplayName("Test ARDoCo for Inconsistency Detection (UML)")
    void testInconsistencyDetectionUml() {
        var runner = new InconsistencyDetection(projectName);
        var additionalConfigsMap = ConfigurationHelper.loadAdditionalConfigs(new File(additionalConfigs));
        runner.setUp(inputText, inputModelArchitectureUml, ModelFormat.UML, additionalConfigsMap, outputDir);

        testRunnerAssertions(runner);
        Assertions.assertNotNull(runner.run());
    }

}
