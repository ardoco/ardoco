/* Licensed under MIT 2025. */
package edu.kit.kastel.mcse.ardoco.tlr.tests.integration;

import static edu.kit.kastel.mcse.ardoco.core.api.models.Metamodel.CODE_WITH_COMPILATION_UNITS;

import java.util.List;

import org.eclipse.collections.api.factory.Lists;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.condition.DisabledIfEnvironmentVariable;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import edu.kit.kastel.mcse.ardoco.core.common.util.Environment;
import edu.kit.kastel.mcse.ardoco.metrics.result.SingleClassificationResult;
import edu.kit.kastel.mcse.ardoco.tlr.models.informants.LargeLanguageModel;
import edu.kit.kastel.mcse.ardoco.tlr.tests.approach.TransArCEvaluationProject;
import edu.kit.kastel.mcse.ardoco.tlr.tests.integration.evaluation.ArtemisInExArchEvaluation;
import edu.kit.kastel.mcse.ardoco.tlr.tests.integration.evaluation.ArtemisInTransarcEvaluation;

class ArtemisInTransarcIT extends AbstractArdocoIT {
    private static final int NUMBER_OF_RUNS = 5;

    @BeforeAll
    static void beforeAll() {
        Assumptions.assumeTrue(Environment.getEnv("OPENAI_API_KEY") != null || Environment.getEnv("OLLAMA_HOST") != null);
    }

    @DisabledIfEnvironmentVariable(named = "mutipleRuns", matches = ".*")
    @DisplayName("Evaluate Artemis @ TransArC (SAD-SAM-Code TLR)")
    @ParameterizedTest(name = "{0}")
    @EnumSource(TransArCEvaluationProject.class)
    void evaluateSadSamCodeTlrIT(TransArCEvaluationProject project) {
        LargeLanguageModel llmForNer = LargeLanguageModel.GPT_5;
        var evaluation = new ArtemisInTransarcEvaluation(project, llmForNer);
        var results = evaluation.runTraceLinkEvaluation();
        Assertions.assertNotNull(results);
    }

    @EnabledIfEnvironmentVariable(named = "mutipleRuns", matches = ".*")
    @DisplayName("Evaluate Artemis @ TransArC (SAD-SAM-Code TLR) Multi")
    @ParameterizedTest(name = "{0}")
    @EnumSource(TransArCEvaluationProject.class)
    void evaluateSadSamCodeTlrMultiIT(TransArCEvaluationProject project) {
        LargeLanguageModel llmForNer = LargeLanguageModel.GPT_5;

        List<SingleClassificationResult<String>> results = Lists.mutable.empty();
        for (int i = 0; i < NUMBER_OF_RUNS; i++) {
            logger.info("Eval run {}/{} [{}]", i, NUMBER_OF_RUNS, project);
            var evaluation = new ArtemisInTransarcEvaluation(project, llmForNer);
            var result = evaluation.runTraceLinkEvaluation();
            Assertions.assertNotNull(results);

            var goldStandard = project.getTlrTask().getExpectedTraceLinks();
            goldStandard = ArtemisInExArchEvaluation.enrollGoldStandard(goldStandard, result, CODE_WITH_COMPILATION_UNITS);
            var evalResult = ArtemisInExArchEvaluation.calculateEvaluationResults(result, goldStandard, CODE_WITH_COMPILATION_UNITS);
            results.add(evalResult);
        }
        averageAndLog(results);
    }
}
