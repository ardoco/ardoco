/* Licensed under MIT 2023-2024. */
package edu.kit.kastel.mcse.ardoco.tlr.tests.integration;

import static edu.kit.kastel.mcse.ardoco.tlr.tests.integration.TraceLinkEvaluationIT.OUTPUT;

import java.io.File;
import java.util.*;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import edu.kit.kastel.mcse.ardoco.core.api.models.ArchitectureModelType;
import edu.kit.kastel.mcse.ardoco.core.execution.ConfigurationHelper;
import edu.kit.kastel.mcse.ardoco.core.execution.runner.ArDoCoRunner;
import edu.kit.kastel.mcse.ardoco.core.tests.eval.Project;
import edu.kit.kastel.mcse.ardoco.core.tests.eval.results.EvaluationResults;
import edu.kit.kastel.mcse.ardoco.core.tests.eval.results.ExpectedResults;
import edu.kit.kastel.mcse.ardoco.tlr.execution.ArDoCoForSadSamTraceabilityLinkRecovery;

class TraceLinkEvaluationSadSamViaLlmIT {
    protected static final String LOGGING_ARDOCO_CORE = "org.slf4j.simpleLogger.log.edu.kit.kastel.mcse.ardoco.core";
    protected static final String OUTPUT = "target/testout-tlr-it-llm";

    @BeforeAll
    static void beforeAll() {
        System.setProperty(LOGGING_ARDOCO_CORE, "info");
    }

    @AfterAll
    static void afterAll() {
        System.setProperty(LOGGING_ARDOCO_CORE, "error");
    }

    @DisplayName("Evaluate SAD-SAM-via-LLM TLR (MS)")
    @Test
    void evaluateSadCodeTlrITMs() throws Exception {
        Project projectToConsider = Project.MEDIASTORE;
        File inputModel = new File("src/test/resources/mediastore/mediastore_gpt4o_from_docs.txt");

        SadSamTraceabilityLinkRecoveryEvaluation<Project> evaluation = new SadSamTraceabilityLinkRecoveryEvaluation<>() {
            @Override
            protected ArDoCoRunner getAndSetupRunner(Project project) {
                var additionalConfigsMap = ConfigurationHelper.loadAdditionalConfigs(project.getAdditionalConfigurationsFile());

                String name = project.getProjectName();
                File inputText = project.getTextFile();
                File outputDir = new File(OUTPUT);

                var runner = new ArDoCoForSadSamTraceabilityLinkRecovery(name);
                runner.setUp(inputText, inputModel, ArchitectureModelType.RAW, additionalConfigsMap, outputDir);
                return runner;
            }

            @Override
            protected void compareResults(EvaluationResults<String> results, ExpectedResults expectedResults) {
                // Disable Error Logging
            }
        };
        var result = evaluation.runTraceLinkEvaluation(projectToConsider);
        Assertions.assertNotNull(result);
    }

    @DisplayName("Evaluate SAD-SAM-via-LLM TLR (BBB)")
    @Test
    void evaluateSadCodeTlrITBbb() throws Exception {
        Project projectToConsider = Project.BIGBLUEBUTTON;
        File inputModel = new File("src/test/resources/bbb/bbb_codellama13b_from_docs.txt");

        SadSamTraceabilityLinkRecoveryEvaluation<Project> evaluation = new SadSamTraceabilityLinkRecoveryEvaluation<>() {
            @Override
            protected ArDoCoRunner getAndSetupRunner(Project project) {
                var additionalConfigsMap = ConfigurationHelper.loadAdditionalConfigs(project.getAdditionalConfigurationsFile());

                String name = project.getProjectName();
                File inputText = project.getTextFile();
                File outputDir = new File(OUTPUT);

                var runner = new ArDoCoForSadSamTraceabilityLinkRecovery(name);
                runner.setUp(inputText, inputModel, ArchitectureModelType.RAW, additionalConfigsMap, outputDir);
                return runner;
            }

            @Override
            protected void compareResults(EvaluationResults<String> results, ExpectedResults expectedResults) {
                // Disable Error Logging
            }
        };
        var result = evaluation.runTraceLinkEvaluation(projectToConsider);
        Assertions.assertNotNull(result);
    }

}
