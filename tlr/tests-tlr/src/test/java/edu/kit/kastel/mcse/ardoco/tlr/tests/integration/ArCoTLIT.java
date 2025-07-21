/* Licensed under MIT 2025. */
package edu.kit.kastel.mcse.ardoco.tlr.tests.integration;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import edu.kit.kastel.mcse.ardoco.core.common.RepositoryHandler;
import edu.kit.kastel.mcse.ardoco.tlr.tests.approach.ArCoTLEvaluationProject;
import edu.kit.kastel.mcse.ardoco.tlr.tests.integration.evaluation.ArCoTLEvaluation;

class ArCoTLIT extends AbstractArDoCoIT {

    @DisplayName("Evaluate ArCoTL (SAM-Code TLR)")
    @ParameterizedTest(name = "{0}")
    @EnumSource(ArCoTLEvaluationProject.class)
    void evaluateSamCodeTlrIT(ArCoTLEvaluationProject project) {
        var evaluation = new ArCoTLEvaluation(project, true);
        var results = evaluation.runTraceLinkEvaluation();
        Assertions.assertNotNull(results);
    }

    @EnabledIfEnvironmentVariable(named = "testCodeFull", matches = ".*")
    @DisplayName("Evaluate ArCoTL (SAM-Code TLR) (Full)")
    @ParameterizedTest(name = "{0}")
    @EnumSource(ArCoTLEvaluationProject.class)
    void evaluateSamCodeTlrITFull(ArCoTLEvaluationProject project) {
        RepositoryHandler.removeRepository(project.getTlrTask().getCodeDirectoryWithoutCloning().getAbsolutePath());

        var evaluation = new ArCoTLEvaluation(project, false);
        var results = evaluation.runTraceLinkEvaluation();
        Assertions.assertNotNull(results);
    }

}
