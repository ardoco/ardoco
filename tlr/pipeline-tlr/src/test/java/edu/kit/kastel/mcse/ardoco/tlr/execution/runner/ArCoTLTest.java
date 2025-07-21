/* Licensed under MIT 2023-2025. */
package edu.kit.kastel.mcse.ardoco.tlr.execution.runner;

import java.io.File;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import edu.kit.kastel.mcse.ardoco.core.api.models.ModelFormat;
import edu.kit.kastel.mcse.ardoco.core.execution.CodeRunnerBaseTest;
import edu.kit.kastel.mcse.ardoco.core.execution.ConfigurationHelper;
import edu.kit.kastel.mcse.ardoco.tlr.execution.ArCoTL;
import edu.kit.kastel.mcse.ardoco.tlr.models.agents.ArchitectureConfiguration;

class ArCoTLTest extends CodeRunnerBaseTest {

    @Test
    @DisplayName("Test ArDoCo for SAM-Code-TLR (PCM)")
    void testSamCodeTlrPcm() {
        var runner = new ArCoTL(projectName);
        var additionalConfigsMap = ConfigurationHelper.loadAdditionalConfigs(new File(additionalConfigs));
        runner.setUp(new ArchitectureConfiguration(new File(inputModelArchitecture), ModelFormat.PCM), this.codeConfiguration, additionalConfigsMap, new File(
                outputDir));

        testRunnerAssertions(runner);
        Assertions.assertNotNull(runner.run());
    }

    @Disabled("Disabled for faster builds. Enable if you need to check UML models.")
    @Test
    @DisplayName("Test ArDoCo for SAM-Code-TLR (UML)")
    void testSamCodeTlrUml() {
        var runner = new ArCoTL(projectName);
        var additionalConfigsMap = ConfigurationHelper.loadAdditionalConfigs(new File(additionalConfigs));
        runner.setUp(new ArchitectureConfiguration(new File(inputModelArchitectureUml), ModelFormat.UML), codeConfiguration, additionalConfigsMap, new File(
                outputDir));

        testRunnerAssertions(runner);
        Assertions.assertNotNull(runner.run());
    }

}
