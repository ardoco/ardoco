/* Licensed under MIT 2025. */
package edu.kit.kastel.mcse.ardoco.tlr.tests.integration;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import edu.kit.kastel.mcse.ardoco.tlr.tests.approach.ArDoCodeEvaluationProject;
import edu.kit.kastel.mcse.ardoco.tlr.tests.integration.evaluation.ArDoCodeEvaluation;

class ArDoCodeIT extends AbstractIT {
    @DisplayName("Evaluate ArDoCode (SAD-Code TLR)")
    @ParameterizedTest(name = "{0}")
    @EnumSource(ArDoCodeEvaluationProject.class)
    void evaluateSadSamCodeTlrIT(ArDoCodeEvaluationProject project) {
        var evaluation = new ArDoCodeEvaluation(project);
        var results = evaluation.runTraceLinkEvaluation();
        Assertions.assertNotNull(results);
    }
}
