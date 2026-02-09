/* Licensed under MIT 2025-2026. */
package edu.kit.kastel.mcse.ardoco.tlr.tests.integration;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import edu.kit.kastel.mcse.ardoco.tlr.tests.approach.ArdocodeEvaluationProject;
import edu.kit.kastel.mcse.ardoco.tlr.tests.integration.evaluation.ArdocodeEvaluation;

class ArdocodeIT extends AbstractArdocoIT {
    @DisplayName("Evaluate ARDoCode (SAD-Code TLR)")
    @ParameterizedTest(name = "{0}")
    @EnumSource(ArdocodeEvaluationProject.class)
    void evaluateSadSamCodeTlrIT(ArdocodeEvaluationProject project) {
        var evaluation = new ArdocodeEvaluation(project);
        var results = evaluation.runTraceLinkEvaluation();
        Assertions.assertNotNull(results);
    }
}
