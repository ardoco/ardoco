/* Licensed under MIT 2025. */
package edu.kit.kastel.mcse.ardoco.tlr.tests.integration;

import java.util.ArrayList;
import java.util.List;
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

import edu.kit.kastel.mcse.ardoco.core.api.output.ArDoCoResult;
import edu.kit.kastel.mcse.ardoco.core.common.util.Environment;
import edu.kit.kastel.mcse.ardoco.core.execution.runner.ArDoCoRunner;
import edu.kit.kastel.mcse.ardoco.metrics.result.SingleClassificationResult;
import edu.kit.kastel.mcse.ardoco.tlr.models.informants.LargeLanguageModel;
import edu.kit.kastel.mcse.ardoco.tlr.tests.approach.ArtemisEvaluationProject;
import edu.kit.kastel.mcse.ardoco.tlr.tests.integration.evaluation.ArtemisEvaluation;

class ArtemisIT extends AbstractArdocoIT {
    private static final int NUMBER_OF_RUNS = 5;

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
            logger.info("Eval run {}/{} [{},{}]", i, NUMBER_OF_RUNS, project, llm);
            var result = runTraceLinkEvaluation(project, llm);
            Assertions.assertNotNull(result);
            results.add(result);
        }
        averageAndLog(results);
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
