/* Licensed under MIT 2023-2025. */
package edu.kit.kastel.mcse.ardoco.tlr.execution.runner;

import java.io.File;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import edu.kit.kastel.mcse.ardoco.core.execution.CodeRunnerBaseTest;
import edu.kit.kastel.mcse.ardoco.core.execution.ConfigurationHelper;
import edu.kit.kastel.mcse.ardoco.tlr.execution.ArDoCode;

class ArDoCodeTest extends CodeRunnerBaseTest {

    @Test
    @DisplayName("Test ArDoCo for SAD-Code-TLR")
    void testSadCodeTlr() {
        var runner = new ArDoCode(projectName);
        var additionalConfigsMap = ConfigurationHelper.loadAdditionalConfigs(new File(additionalConfigs));
        runner.setUp(new File(inputText), codeConfiguration, additionalConfigsMap, new File(outputDir));

        testRunnerAssertions(runner);
        Assertions.assertNotNull(runner.run());
    }

}
