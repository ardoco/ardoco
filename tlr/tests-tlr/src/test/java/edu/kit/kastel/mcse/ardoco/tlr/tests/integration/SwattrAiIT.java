/* Licensed under MIT 2025. */
package edu.kit.kastel.mcse.ardoco.tlr.tests.integration;

import java.io.File;

import org.eclipse.collections.api.factory.SortedMaps;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import edu.kit.kastel.mcse.ardoco.core.api.models.ModelFormat;
import edu.kit.kastel.mcse.ardoco.core.execution.runner.ArDoCoRunner;
import edu.kit.kastel.mcse.ardoco.core.tests.eval.ExpectedResults;
import edu.kit.kastel.mcse.ardoco.metrics.result.SingleClassificationResult;
import edu.kit.kastel.mcse.ardoco.tlr.execution.Swattr;
import edu.kit.kastel.mcse.ardoco.tlr.models.agents.ArchitectureConfiguration;
import edu.kit.kastel.mcse.ardoco.tlr.tests.approach.SwattrEvaluationProject;
import edu.kit.kastel.mcse.ardoco.tlr.tests.integration.evaluation.SwattrEvaluation;

class SwattrAiIT {
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
    void evaluateSadCodeTlrITMs() {
        SwattrEvaluationProject projectToConsider = SwattrEvaluationProject.MEDIASTORE;
        File inputModel = new File("src/test/resources/mediastore/mediastore_gpt4o_from_docs.txt");

        SwattrEvaluation evaluation = new SwattrEvaluation(projectToConsider) {
            @Override
            protected ArDoCoRunner createSwattrRunner() {

                String name = project.name();
                File inputText = project.getTlrTask().getTextFile();
                File outputDir = new File(OUTPUT);

                var runner = new Swattr(name);
                runner.setUp(inputText, new ArchitectureConfiguration(inputModel, ModelFormat.RAW), SortedMaps.immutable.empty(), outputDir);
                return runner;
            }

            @Override
            protected void compareResults(SingleClassificationResult<?> results, ExpectedResults expectedResults) {
                // Disable Error Logging
            }
        };
        var result = evaluation.runTraceLinkEvaluation();
        Assertions.assertNotNull(result);
    }

    @DisplayName("Evaluate SAD-SAM-via-LLM TLR (BBB)")
    @Test
    void evaluateSadCodeTlrITBbb() {
        SwattrEvaluationProject projectToConsider = SwattrEvaluationProject.BIGBLUEBUTTON;
        File inputModel = new File("src/test/resources/bbb/bbb_codellama13b_from_docs.txt");

        SwattrEvaluation evaluation = new SwattrEvaluation(projectToConsider) {
            @Override
            protected ArDoCoRunner createSwattrRunner() {
                String name = project.name();
                File inputText = project.getTlrTask().getTextFile();
                File outputDir = new File(OUTPUT);

                var runner = new Swattr(name);
                runner.setUp(inputText, new ArchitectureConfiguration(inputModel, ModelFormat.RAW), SortedMaps.immutable.empty(), outputDir);
                return runner;
            }

            @Override
            protected void compareResults(SingleClassificationResult<?> results, ExpectedResults expectedResults) {
                // Disable Error Logging
            }
        };
        var result = evaluation.runTraceLinkEvaluation();
        Assertions.assertNotNull(result);
    }

}
