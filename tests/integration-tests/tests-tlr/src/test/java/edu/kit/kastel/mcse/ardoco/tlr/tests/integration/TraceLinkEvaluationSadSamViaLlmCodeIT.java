/* Licensed under MIT 2023-2024. */
package edu.kit.kastel.mcse.ardoco.tlr.tests.integration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.collections.api.tuple.Pair;
import org.eclipse.collections.impl.tuple.Tuples;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.kit.kastel.mcse.ardoco.core.api.output.ArDoCoResult;
import edu.kit.kastel.mcse.ardoco.core.tests.eval.CodeProject;
import edu.kit.kastel.mcse.ardoco.metrics.ClassificationMetricsCalculator;
import edu.kit.kastel.mcse.ardoco.metrics.result.AggregationType;
import edu.kit.kastel.mcse.ardoco.metrics.result.SingleClassificationResult;
import edu.kit.kastel.mcse.ardoco.tlr.models.informants.LLMArchitecturePrompt;
import edu.kit.kastel.mcse.ardoco.tlr.models.informants.LargeLanguageModel;

class TraceLinkEvaluationSadSamViaLlmCodeIT {
    private static final Logger logger = LoggerFactory.getLogger(TraceLinkEvaluationSadSamViaLlmCodeIT.class);
    protected static final String LOGGING_ARDOCO_CORE = "org.slf4j.simpleLogger.log.edu.kit.kastel.mcse.ardoco.core";

    private static final Map<Pair<CodeProject, LargeLanguageModel>, ArDoCoResult> RESULTS = new HashMap<>();

    @BeforeAll
    static void beforeAll() {
        System.setProperty(LOGGING_ARDOCO_CORE, "info");
        Assumptions.assumeTrue(System.getenv("OPENAI_API_KEY") != null || System.getenv("OLLAMA_HOST") != null);
    }

    @AfterAll
    static void afterAll() {
        System.setProperty(LOGGING_ARDOCO_CORE, "error");
    }

    @DisplayName("Evaluate SAD-SAM-via-LLM-Code TLR")
    @ParameterizedTest(name = "{0} ({1})")
    @MethodSource("llmsXprojects")
    void evaluateSadCodeTlrIT(CodeProject project, LargeLanguageModel llm) {
        Assumptions.assumeTrue(System.getenv("CI") == null);

        LLMArchitecturePrompt docPrompt = LLMArchitecturePrompt.DOCUMENTATION_ONLY_V1;
        LLMArchitecturePrompt codePrompt = null;
        LLMArchitecturePrompt aggPrompt = null;

        LLMArchitecturePrompt.Features codeFeatures = LLMArchitecturePrompt.Features.PACKAGES;

        logger.info("###############################################");
        logger.info("Evaluating project {} with LLM '{}'", project, llm);
        logger.info("Prompts: {}, {}, {}", docPrompt, codePrompt, aggPrompt);
        logger.info("Features: {}", codeFeatures);

        var evaluation = new SadSamViaLlmCodeTraceabilityLinkRecoveryEvaluation(true, llm, docPrompt, codePrompt, codeFeatures, aggPrompt);
        var result = evaluation.runTraceLinkEvaluation(project);
        if (result != null) {
            RESULTS.put(Tuples.pair(project, llm), result);
        }
        System.err.printf("--- Evaluated project %s with LLM '%s' ---%n", project, llm);
        logger.info("###############################################");
    }

    @AfterAll
    static void printResults() {
        logger.info("!!!!!!!!! Results !!!!!!!!!!");
        System.out.println(Arrays.stream(CodeProject.values())
                .map(Enum::name)
                .collect(Collectors.joining(" & ")) + " & Macro Avg & Weighted Average" + " \\\\");
        for (LargeLanguageModel llm : LargeLanguageModel.values()) {
            if (llm.isGeneric() && RESULTS.keySet().stream().noneMatch(k -> k.getTwo() == llm)) {
                continue;
            }
            StringBuilder llmResult = new StringBuilder(llm.getHumanReadableName() + " ");

            List<SingleClassificationResult<String>> classificationResults = new ArrayList<>();
            for (CodeProject project : CodeProject.values()) {
                if (!RESULTS.containsKey(Tuples.pair(project, llm))) {
                    llmResult.append("&--&--&--");
                    continue;
                }
                ArDoCoResult result = RESULTS.get(Tuples.pair(project, llm));

                // Just some instance .. parameters do not matter ..
                var evaluation = new SadSamViaLlmCodeTraceabilityLinkRecoveryEvaluation(true, llm, LLMArchitecturePrompt.DOCUMENTATION_ONLY_V1, null, null,
                        null);
                var goldStandard = project.getSadCodeGoldStandard();
                goldStandard = TraceabilityLinkRecoveryEvaluation.enrollGoldStandardForCode(goldStandard, result);
                var evaluationResults = evaluation.calculateEvaluationResults(result, goldStandard);
                classificationResults.add(evaluationResults.classificationResult());
                llmResult.append(String.format(Locale.ENGLISH, "&%.2f&%.2f&%.2f", evaluationResults.precision(), evaluationResults.recall(), evaluationResults
                        .f1()));
            }
            ClassificationMetricsCalculator classificationMetricsCalculator = ClassificationMetricsCalculator.getInstance();
            var averages = classificationMetricsCalculator.calculateAverages(classificationResults, null);

            var macro = averages.stream().filter(it -> it.getType() == AggregationType.MACRO_AVERAGE).findFirst().orElseThrow();
            var weighted = averages.stream().filter(it -> it.getType() == AggregationType.WEIGHTED_AVERAGE).findFirst().orElseThrow();

            llmResult.append(String.format(Locale.ENGLISH, "&%.2f&%.2f&%.2f&%.2f&%.2f&%.2f\\\\", macro.getPrecision(), macro.getRecall(), macro.getF1(),
                    weighted.getPrecision(), weighted.getRecall(), weighted.getF1())); // end of line
            System.out.println(llmResult.toString().replace("0.", ".").replace("1.00", "1.0"));
        }
    }

    private static Stream<Arguments> llmsXprojects() {
        List<Arguments> result = new ArrayList<>();
        for (LargeLanguageModel llm : LargeLanguageModel.values()) {
            if (llm.isGeneric())
                continue;
            for (CodeProject codeProject : CodeProject.values()) {
                result.add(Arguments.of(codeProject, llm));
            }
        }
        return result.stream();
    }
}
