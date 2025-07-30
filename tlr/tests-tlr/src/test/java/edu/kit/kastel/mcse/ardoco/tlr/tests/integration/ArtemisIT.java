/* Licensed under MIT 2025. */
package edu.kit.kastel.mcse.ardoco.tlr.tests.integration;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import edu.kit.kastel.mcse.ardoco.core.common.util.Environment;
import edu.kit.kastel.mcse.ardoco.tlr.tests.approach.ArtemisEvaluationProject;
import edu.kit.kastel.mcse.ardoco.tlr.tests.integration.evaluation.ArtemisEvaluation;

class ArtemisIT extends AbstractArdocoIT {

    @BeforeAll
    static void beforeAll() {
        System.setProperty(LOGGING_ARDOCO_CORE, "info");
        Assumptions.assumeTrue(Environment.getEnv("OPENAI_API_KEY") != null); // TODO
    }

    @AfterAll
    static void afterAll() {
        System.setProperty(LOGGING_ARDOCO_CORE, "error");
    }

    @DisplayName("Evaluate ArTEMiS (SAD-SAM TLR with NER)")
    @ParameterizedTest(name = "{0}")
    @EnumSource(ArtemisEvaluationProject.class)
    void evaluateSadSamTlrIT(ArtemisEvaluationProject project) {
        Assumptions.assumeTrue(Environment.getEnv("CI") == null);

        var evaluation = new ArtemisEvaluation(project);
        var result = evaluation.runTraceLinkEvaluation();
        Assertions.assertNotNull(result);
    }

}
