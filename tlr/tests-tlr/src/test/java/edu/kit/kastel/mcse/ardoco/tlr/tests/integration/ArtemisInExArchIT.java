/* Licensed under MIT 2025. */
package edu.kit.kastel.mcse.ardoco.tlr.tests.integration;

import static edu.kit.kastel.mcse.ardoco.core.api.models.Metamodel.CODE_WITH_COMPILATION_UNITS;

import java.util.List;

import org.eclipse.collections.api.factory.Lists;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.condition.DisabledIfEnvironmentVariable;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.kit.kastel.mcse.ardoco.core.common.util.Environment;
import edu.kit.kastel.mcse.ardoco.metrics.result.SingleClassificationResult;
import edu.kit.kastel.mcse.ardoco.tlr.models.informants.LargeLanguageModel;
import edu.kit.kastel.mcse.ardoco.tlr.models.informants.LlmArchitecturePrompt;
import edu.kit.kastel.mcse.ardoco.tlr.tests.approach.ArDoCodeEvaluationProject;
import edu.kit.kastel.mcse.ardoco.tlr.tests.integration.evaluation.ArtemisInExArchEvaluation;

@Disabled("Only for manual execution")
class ArtemisInExArchIT extends AbstractArdocoIT {
    private static final int NUMBER_OF_RUNS = 5;

    private static final Logger logger = LoggerFactory.getLogger(ArtemisInExArchIT.class);
    protected static final String LOGGING_ARDOCO_CORE = "org.slf4j.simpleLogger.log.edu.kit.kastel.mcse.ardoco.core";

    @BeforeAll
    static void beforeAll() {
        Assumptions.assumeTrue(Environment.getEnv("OPENAI_API_KEY") != null || Environment.getEnv("OLLAMA_HOST") != null);

        System.setProperty(LOGGING_ARDOCO_CORE, "info");
        Assumptions.assumeTrue(Environment.getEnv("OPENAI_API_KEY") != null || Environment.getEnv("OLLAMA_HOST") != null);
    }

    @AfterAll
    static void afterAll() {
        System.setProperty(LOGGING_ARDOCO_CORE, "error");
    }

    @DisabledIfEnvironmentVariable(named = "mutipleRuns", matches = ".*")
    @DisplayName("Evaluate Artemis @ ExArch (SAD-SAM-via-LLM-Code) TLR")
    @ParameterizedTest(name = "{0}")
    @EnumSource(ArDoCodeEvaluationProject.class)
    void evaluateArtemisInExArch(ArDoCodeEvaluationProject project) {
        Assumptions.assumeTrue(Environment.getEnv("CI") == null);

        LargeLanguageModel llmForExArch = LargeLanguageModel.GPT_4_O;
        LlmArchitecturePrompt docPrompt = LlmArchitecturePrompt.EXTRACT_FROM_ARCHITECTURE;
        LlmArchitecturePrompt codePrompt = null;
        LlmArchitecturePrompt aggPrompt = null;

        LlmArchitecturePrompt.Features codeFeatures = LlmArchitecturePrompt.Features.PACKAGES;

        LargeLanguageModel llmForNer = LargeLanguageModel.GPT_5;

        logger.info("Evaluating project {} with LLM '{}', '{}'", project, llmForExArch, llmForNer);
        logger.info("Prompts: {}, {}, {}", docPrompt, codePrompt, aggPrompt);
        logger.info("Features: {}", codeFeatures);

        var evaluation = new ArtemisInExArchEvaluation(project, llmForExArch, docPrompt, codePrompt, codeFeatures, aggPrompt, llmForNer);
        var result = evaluation.runTraceLinkEvaluation();
        Assertions.assertNotNull(result);
    }

    @EnabledIfEnvironmentVariable(named = "mutipleRuns", matches = ".*")
    @DisplayName("Evaluate Artemis @ ExArch (SAD-SAM-via-LLM-Code) TLR Multi")
    @ParameterizedTest(name = "{0}")
    @EnumSource(ArDoCodeEvaluationProject.class)
    void evaluateArtemisInExArchMultiple(ArDoCodeEvaluationProject project) {
        Assumptions.assumeTrue(Environment.getEnv("CI") == null);

        LargeLanguageModel llmForExArch = LargeLanguageModel.GPT_4_O;
        LlmArchitecturePrompt docPrompt = LlmArchitecturePrompt.EXTRACT_FROM_ARCHITECTURE;
        LlmArchitecturePrompt codePrompt = null;
        LlmArchitecturePrompt aggPrompt = null;

        LlmArchitecturePrompt.Features codeFeatures = LlmArchitecturePrompt.Features.PACKAGES;

        LargeLanguageModel llmForNer = LargeLanguageModel.GPT_5;

        logger.info("Evaluating project {} with LLM '{}', '{}'", project, llmForExArch, llmForNer);
        logger.info("Prompts: {}, {}, {}", docPrompt, codePrompt, aggPrompt);
        logger.info("Features: {}", codeFeatures);

        List<SingleClassificationResult<String>> results = Lists.mutable.empty();
        for (int i = 0; i < NUMBER_OF_RUNS; i++) {
            logger.info("Eval run {}/{} [{}]", i, NUMBER_OF_RUNS, project);
            var evaluation = new ArtemisInExArchEvaluation(project, llmForExArch, docPrompt, codePrompt, codeFeatures, aggPrompt, llmForNer);
            var result = evaluation.runTraceLinkEvaluation();
            Assertions.assertNotNull(result);

            var goldStandard = project.getTlrTask().getExpectedTraceLinks();
            goldStandard = ArtemisInExArchEvaluation.enrollGoldStandard(goldStandard, result, CODE_WITH_COMPILATION_UNITS);
            var evalResult = ArtemisInExArchEvaluation.calculateEvaluationResults(result, goldStandard, CODE_WITH_COMPILATION_UNITS);
            results.add(evalResult);
        }
        averageAndLog(results);
    }
}
