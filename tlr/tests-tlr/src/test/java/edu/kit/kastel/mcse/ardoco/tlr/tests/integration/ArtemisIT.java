/* Licensed under MIT 2025. */
package edu.kit.kastel.mcse.ardoco.tlr.tests.integration;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.kit.kastel.mcse.ardoco.core.common.util.Environment;
import edu.kit.kastel.mcse.ardoco.tlr.tests.approach.ArtemisEvaluationProject;
import edu.kit.kastel.mcse.ardoco.tlr.tests.integration.evaluation.ArtemisEvaluation;

class ArtemisIT extends AbstractArdocoIT {
    protected Logger logger = LoggerFactory.getLogger(this.getClass());

    @BeforeAll
    static void beforeAll() {
        Assumptions.assumeTrue(Environment.getEnv("OPENAI_API_KEY") != null || Environment.getEnv("OLLAMA_HOST") != null);
    }

    @DisplayName("Evaluate ArTEMiS (SAD-SAM TLR with NER)")
    @ParameterizedTest(name = "{0}")
    @EnumSource(ArtemisEvaluationProject.class)
    void evaluateSadSamTlrIT(ArtemisEvaluationProject project) {
        Assumptions.assumeTrue(Environment.getEnv("CI") == null);
        if (checkLlmProvision()) {
            logger.info("Skipping evaluation of ArTEMiS as the LLM provider is not properly set");
            return;
        }
        
        var evaluation = new ArtemisEvaluation(project);
        var result = evaluation.runTraceLinkEvaluation();
        Assertions.assertNotNull(result);
    }

    private static boolean checkLlmProvision() {
        // TODO maybe make this more flexible for other settings
        return Environment.getEnv("OPENAI_API_KEY") == null;
    }

}
