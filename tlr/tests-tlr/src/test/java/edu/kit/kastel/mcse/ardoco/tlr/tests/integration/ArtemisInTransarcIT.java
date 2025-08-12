/* Licensed under MIT 2025. */
package edu.kit.kastel.mcse.ardoco.tlr.tests.integration;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import edu.kit.kastel.mcse.ardoco.tlr.models.informants.LargeLanguageModel;
import edu.kit.kastel.mcse.ardoco.tlr.tests.approach.TransArCEvaluationProject;
import edu.kit.kastel.mcse.ardoco.tlr.tests.integration.evaluation.ArtemisInTransarcEvaluation;

class ArtemisInTransarcIT extends AbstractArdocoIT {

    @DisplayName("Evaluate Artemis @ TransArC (SAD-SAM-Code TLR)")
    @ParameterizedTest(name = "{0}")
    @EnumSource(TransArCEvaluationProject.class)
    void evaluateSadSamCodeTlrIT(TransArCEvaluationProject project) {
        LargeLanguageModel llmForNer = LargeLanguageModel.GPT_5;
        var evaluation = new ArtemisInTransarcEvaluation(project, llmForNer);
        var results = evaluation.runTraceLinkEvaluation();
        Assertions.assertNotNull(results);
    }
}
