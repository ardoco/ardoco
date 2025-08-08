/* Licensed under MIT 2025. */
package edu.kit.kastel.mcse.ardoco.tlr.tests.integration;

import java.util.List;
import java.util.Locale;

import org.eclipse.collections.api.factory.Lists;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.condition.DisabledIfEnvironmentVariable;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.kit.kastel.mcse.ardoco.core.api.output.ArDoCoResult;
import edu.kit.kastel.mcse.ardoco.core.common.util.Environment;
import edu.kit.kastel.mcse.ardoco.core.execution.runner.ArDoCoRunner;
import edu.kit.kastel.mcse.ardoco.metrics.result.SingleClassificationResult;
import edu.kit.kastel.mcse.ardoco.tlr.tests.approach.ArtemisEvaluationProject;
import edu.kit.kastel.mcse.ardoco.tlr.tests.integration.evaluation.ArtemisEvaluation;

class ArtemisIT extends AbstractArdocoIT {
    private static final int NUMBER_OF_RUNS = 5;
    protected Logger logger = LoggerFactory.getLogger(this.getClass());

    @BeforeAll
    static void beforeAll() {
        Assumptions.assumeTrue(Environment.getEnv("OPENAI_API_KEY") != null || Environment.getEnv("OLLAMA_HOST") != null);
    }

    @DisabledIfEnvironmentVariable(named = "mutipleRuns", matches = ".*")
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

    @EnabledIfEnvironmentVariable(named = "mutipleRuns", matches = ".*")
    @DisplayName("Evaluate ArTEMiS (SAD-SAM TLR with NER) Multi")
    @ParameterizedTest(name = "{0}")
    @EnumSource(ArtemisEvaluationProject.class)
    void evaluateSadSamTlrMultipleIT(ArtemisEvaluationProject project) {
        Assumptions.assumeTrue(Environment.getEnv("CI") == null);
        if (checkLlmProvision()) {
            logger.info("Skipping evaluation of ArTEMiS as the LLM provider is not properly set");
            return;
        }

        List<SingleClassificationResult<String>> results = Lists.mutable.empty();
        for (int i = 0; i < NUMBER_OF_RUNS; i++) {
            logger.info("Eval run {}/{}", i, NUMBER_OF_RUNS);
            var result = runTraceLinkEvaluation(project);
            Assertions.assertNotNull(result);
            results.add(result);
        }
        averageAndLog(results);
    }

    private void averageAndLog(List<SingleClassificationResult<String>> results) {
        var avgPrecision = results.stream().mapToDouble(SingleClassificationResult::getPrecision).average().orElse(0.0);
        var avgRecall = results.stream().mapToDouble(SingleClassificationResult::getRecall).average().orElse(0.0);
        var avgF1 = results.stream().mapToDouble(SingleClassificationResult::getF1).average().orElse(0.0);
        var avgAccuracy = results.stream().mapToDouble(SingleClassificationResult::getAccuracy).average().orElse(0.0);
        var avgSpecificity = results.stream().mapToDouble(SingleClassificationResult::getSpecificity).average().orElse(0.0);
        var avgPhi = results.stream().mapToDouble(SingleClassificationResult::getPhiCoefficient).average().orElse(0.0);

        if (logger.isInfoEnabled()) {
            var logString = String.format(Locale.ENGLISH, """

                    Average results:
                    \tPrecision:%8.2f
                    \tRecall:%11.2f
                    \tF1:%15.2f
                    \tAccuracy:%9.2f
                    \tSpecificity:%6.2f
                    \tPhi Coef.:%8.2f
                    """, //
                    avgPrecision, avgRecall, avgF1, avgAccuracy, avgSpecificity, avgPhi);
            logger.info(logString);
        }
    }

    public SingleClassificationResult<String> runTraceLinkEvaluation(ArtemisEvaluationProject project) {
        var evaluation = new ArtemisEvaluation(project);
        ArDoCoRunner artemis = evaluation.createArtemis();
        ArDoCoResult result = artemis.run();
        Assertions.assertNotNull(result);

        var goldStandard = project.getTlrTask().getExpectedTraceLinks();
        return evaluation.calculateEvaluationResults(result, goldStandard);
    }

    private static boolean checkLlmProvision() {
        // TODO maybe make this more flexible for other settings
        return Environment.getEnv("OPENAI_API_KEY") == null;
    }

}
