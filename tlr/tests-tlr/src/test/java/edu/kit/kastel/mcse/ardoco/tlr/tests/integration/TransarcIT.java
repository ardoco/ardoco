/* Licensed under MIT 2025-2026. */
package edu.kit.kastel.mcse.ardoco.tlr.tests.integration;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import edu.kit.kastel.mcse.ardoco.core.common.RepositoryHandler;
import edu.kit.kastel.mcse.ardoco.tlr.tests.approach.TransarcEvaluationProject;
import edu.kit.kastel.mcse.ardoco.tlr.tests.integration.evaluation.TransarcEvaluation;

class TransarcIT extends AbstractArdocoIT {

    @DisplayName("Evaluate TransArC (SAD-SAM-Code TLR)")
    @ParameterizedTest(name = "{0}")
    @EnumSource(TransarcEvaluationProject.class)
    void evaluateSadSamCodeTlrIT(TransarcEvaluationProject project) {
        var evaluation = new TransarcEvaluation(project, true);
        var results = evaluation.runTraceLinkEvaluation();
        Assertions.assertNotNull(results);
    }

    @EnabledIfEnvironmentVariable(named = "testCodeFull", matches = ".*")
    @DisplayName("Evaluate TransArC (SAD-SAM-Code TLR) (Full)")
    @ParameterizedTest(name = "{0}")
    @EnumSource(TransarcEvaluationProject.class)
    void evaluateSadSamCodeTlrITFull(TransarcEvaluationProject project) {
        RepositoryHandler.removeRepository(project.getTlrTask().getCodeDirectoryWithoutCloning().getAbsolutePath());

        var evaluation = new TransarcEvaluation(project, false);
        var results = evaluation.runTraceLinkEvaluation();
        Assertions.assertNotNull(results);
    }
}
