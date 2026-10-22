/* Licensed under MIT 2026. */
package edu.kit.kastel.mcse.ardoco.id.tests.integration;

import static edu.kit.kastel.mcse.ardoco.id.tests.integration.inconsistencyhelper.InconsistencyDetectionEvaluationUtil.logExtendedResultsWithExpected;

import java.util.Arrays;
import java.util.stream.Stream;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.condition.DisabledIfEnvironmentVariable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.kit.kastel.mcse.ardoco.id.tests.integration.inconsistencyhelper.ProgrammingLanguageInconsistencyDetectionEvaluation;
import edu.kit.kastel.mcse.ardoco.id.tests.tasks.ProgrammingLanguageInconsistencyDetectionTask;
import edu.kit.kastel.mcse.ardoco.metrics.ClassificationMetricsCalculator;
import edu.kit.kastel.mcse.ardoco.metrics.result.AggregationType;
import edu.kit.kastel.mcse.ardoco.metrics.result.SingleClassificationResult;
import edu.kit.kastel.mcse.ardoco.tlr.models.agents.CodeConfiguration;
import edu.kit.kastel.mcse.ardoco.tlr.models.informants.LargeLanguageModel;

@Disabled("Only for manual execution")
class ProgrammingLanguageInconsistencyDetectionEvaluationIT {

    private static final Logger logger = LoggerFactory.getLogger(ProgrammingLanguageInconsistencyDetectionEvaluationIT.class);

    private static final LargeLanguageModel LLM = LargeLanguageModel.GPT_5_5;
    private static final LargeLanguageModel VALIDATION_AGENT_LLM = LargeLanguageModel.GPT_5_4_MINI;

    private static Stream<Arguments> projectsXCodeConfigurationTypes() {
        return Arrays.stream(ProgrammingLanguageInconsistencyDetectionTask.values())
                .flatMap(task -> Arrays.stream(CodeConfiguration.CodeConfigurationType.values())
                        .map(configurationType -> Arguments.of(task, configurationType)));
    }

    @DisabledIfEnvironmentVariable(named = "mutipleRuns", matches = ".*")
    @DisplayName("Evaluate programming language inconsistency detection")
    @ParameterizedTest(name = "Evaluating programming language inconsistency detection for {0} with {1}")
    @MethodSource("projectsXCodeConfigurationTypes")
    void programmingLanguageInconsistencyDetectionIT(ProgrammingLanguageInconsistencyDetectionTask task,
            CodeConfiguration.CodeConfigurationType configurationType) {
        var evaluation = new ProgrammingLanguageInconsistencyDetectionEvaluation(task, LLM, VALIDATION_AGENT_LLM);

        SingleClassificationResult<String> result = evaluation.runInconsistencyDetectionEvaluation(configurationType);

        Assertions.assertNotNull(result);
        logExtendedResultsWithExpected(logger, task.getEvaluationProject().name() + " programming language ID", result, task.getExpectedResults());
    }

    @DisabledIfEnvironmentVariable(named = "mutipleRuns", matches = ".*")
    @DisplayName("Evaluate programming language inconsistency detection extended")
    @ParameterizedTest(name = "Evaluating programming language inconsistency detection extended for {0} with {1}")
    @MethodSource("projectsXCodeConfigurationTypes")
    void programmingLanguageInconsistencyDetectionExtendedIT(ProgrammingLanguageInconsistencyDetectionTask task,
            CodeConfiguration.CodeConfigurationType configurationType) {
        var evaluation = new ProgrammingLanguageInconsistencyDetectionEvaluation(task, LLM, VALIDATION_AGENT_LLM);

        var results = evaluation.runInconsistencyDetectionExtendedEvaluation(configurationType);

        Assertions.assertNotNull(results);
        var metrics = ClassificationMetricsCalculator.getInstance();
        var weightedAverageResult = metrics.calculateAverages(results.castToList(), null)
                .asList()
                .stream()
                .filter(it -> it.getType() == AggregationType.WEIGHTED_AVERAGE)
                .findFirst()
                .orElseThrow();
        logExtendedResultsWithExpected(logger, this, task.getEvaluationProject().name() + " programming language ID extended", weightedAverageResult, task
                .getExpectedResults());
    }

    @DisabledIfEnvironmentVariable(named = "mutipleRuns", matches = ".*")
    @DisplayName("Evaluate programming language NER")
    @ParameterizedTest(name = "Evaluating programming language NER for {0}")
    @EnumSource(ProgrammingLanguageInconsistencyDetectionTask.class)
    void programmingLanguageNerIT(ProgrammingLanguageInconsistencyDetectionTask task) {
        var evaluation = new ProgrammingLanguageInconsistencyDetectionEvaluation(task, LLM, VALIDATION_AGENT_LLM);

        SingleClassificationResult<String> result = evaluation.runNerEvaluation();

        Assertions.assertNotNull(result);
        logExtendedResultsWithExpected(logger, task.getEvaluationProject().name() + " programming language NER", result, task.getExpectedResults());
    }
}
