/* Licensed under MIT 2025. */
package edu.kit.kastel.mcse.ardoco.tlr.tests.integration;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import edu.kit.kastel.mcse.ardoco.tlr.tests.approach.SwattrEvaluationProject;
import edu.kit.kastel.mcse.ardoco.tlr.tests.integration.evaluation.SwattrEvaluation;

class SwattrIT extends AbstractIT {

    @DisplayName("Evaluate SWATTR (SAD-SAM TLR)")
    @ParameterizedTest(name = "{0}")
    @EnumSource(SwattrEvaluationProject.class)
    void evaluateSadSamTlrIT(SwattrEvaluationProject project) {
        var evaluation = new SwattrEvaluation(project);
        var result = evaluation.runTraceLinkEvaluation();
        Assertions.assertNotNull(result);
    }

}
