/* Licensed under MIT 2025. */
package edu.kit.kastel.mcse.ardoco.tlr.tests.integration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;

import org.eclipse.collections.api.factory.Lists;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.condition.DisabledIfEnvironmentVariable;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.kit.kastel.mcse.ardoco.core.api.output.ArDoCoResult;
import edu.kit.kastel.mcse.ardoco.core.common.util.Environment;
import edu.kit.kastel.mcse.ardoco.core.execution.runner.ArDoCoRunner;
import edu.kit.kastel.mcse.ardoco.metrics.result.SingleClassificationResult;
import edu.kit.kastel.mcse.ardoco.tlr.models.informants.LargeLanguageModel;
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
    @ParameterizedTest(name = "{0} ({1})")
    @MethodSource("llmsXprojects")
    void evaluateSadSamTlrIT(ArtemisEvaluationProject project, LargeLanguageModel llm) {
        Assumptions.assumeTrue(Environment.getEnv("CI") == null);
        if (checkLlmProvision()) {
            logger.info("Skipping evaluation of ArTEMiS as the LLM provider is not properly set");
            return;
        }

        var evaluation = new ArtemisEvaluation(project, llm);
        var result = evaluation.runTraceLinkEvaluation();
        Assertions.assertNotNull(result);
    }

    @EnabledIfEnvironmentVariable(named = "mutipleRuns", matches = ".*")
    @DisplayName("Evaluate ArTEMiS (SAD-SAM TLR with NER) Multi")
    @ParameterizedTest(name = "{0} ({1})")
    @MethodSource("llmsXprojects")
    void evaluateSadSamTlrMultipleIT(ArtemisEvaluationProject project, LargeLanguageModel llm) {
        Assumptions.assumeTrue(Environment.getEnv("CI") == null);
        if (checkLlmProvision()) {
            logger.info("Skipping evaluation of ArTEMiS as the LLM provider is not properly set");
            return;
        }

        List<SingleClassificationResult<String>> results = Lists.mutable.empty();
        for (int i = 0; i < NUMBER_OF_RUNS; i++) {
            logger.info("Eval run {}/{}", i, NUMBER_OF_RUNS);
            var result = runTraceLinkEvaluation(project, llm);
            Assertions.assertNotNull(result);
            results.add(result);
        }
        averageAndLog(results);
    }

    private void averageAndLog(List<SingleClassificationResult<String>> results) {
        var precisions = results.stream().mapToDouble(SingleClassificationResult::getPrecision).toArray();
        var recalls = results.stream().mapToDouble(SingleClassificationResult::getRecall).toArray();
        var f1s = results.stream().mapToDouble(SingleClassificationResult::getF1).toArray();
        var accuracies = results.stream().mapToDouble(SingleClassificationResult::getAccuracy).toArray();
        var specificities = results.stream().mapToDouble(SingleClassificationResult::getSpecificity).toArray();
        var phis = results.stream().mapToDouble(SingleClassificationResult::getPhiCoefficient).toArray();

        var avgPrecision = Arrays.stream(precisions).average().orElse(0.0);
        var avgRecall = Arrays.stream(recalls).average().orElse(0.0);
        var avgF1 = Arrays.stream(f1s).average().orElse(0.0);
        var avgAccuracy = Arrays.stream(accuracies).average().orElse(0.0);
        var avgSpecificity = Arrays.stream(specificities).average().orElse(0.0);
        var avgPhi = Arrays.stream(phis).average().orElse(0.0);

        var stdPrecision = stddev(precisions, avgPrecision);
        var stdRecall = stddev(recalls, avgRecall);
        var stdF1 = stddev(f1s, avgF1);
        var stdAccuracy = stddev(accuracies, avgAccuracy);
        var stdSpecificity = stddev(specificities, avgSpecificity);
        var stdPhi = stddev(phis, avgPhi);

        if (logger.isInfoEnabled()) {
            var logString = String.format(Locale.ENGLISH, """

                    Average results (± StdDev):
                    	Precision:   %8.2f ± %6.2f
                    	Recall:      %8.2f ± %6.2f
                    	F1:          %8.2f ± %6.2f
                    	Accuracy:    %8.2f ± %6.2f
                    	Specificity: %8.2f ± %6.2f
                    	Phi Coef.:   %8.2f ± %6.2f
                    """, avgPrecision, stdPrecision, avgRecall, stdRecall, avgF1, stdF1, avgAccuracy, stdAccuracy, avgSpecificity, stdSpecificity, avgPhi,
                    stdPhi);
            logger.info(logString);
        }
    }

    private static double stddev(double[] values, double mean) {
        if (values.length == 0)
            return 0.0;
        double sum = 0.0;
        for (double v : values) {
            sum += (v - mean) * (v - mean);
        }
        return Math.sqrt(sum / values.length);
    }

    public SingleClassificationResult<String> runTraceLinkEvaluation(ArtemisEvaluationProject project, LargeLanguageModel llm) {
        var evaluation = new ArtemisEvaluation(project, llm);
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

    private static Stream<Arguments> llmsXprojects() {
        List<Arguments> result = new ArrayList<>();
        for (LargeLanguageModel llm : LargeLanguageModel.values()) {
            if (llm.isGeneric())
                continue;
            for (ArtemisEvaluationProject codeProject : ArtemisEvaluationProject.values()) {
                result.add(Arguments.of(codeProject, llm));
            }
        }
        return result.stream();
    }

}
