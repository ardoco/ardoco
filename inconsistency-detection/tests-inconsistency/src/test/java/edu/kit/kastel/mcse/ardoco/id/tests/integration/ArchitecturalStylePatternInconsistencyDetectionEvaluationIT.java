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

import edu.kit.kastel.mcse.ardoco.id.tests.integration.inconsistencyhelper.ArchitecturalStylePatternInconsistencyDetectionEvaluation;
import edu.kit.kastel.mcse.ardoco.id.tests.tasks.ArchitecturalStylePatternInconsistencyDetectionTask;
import edu.kit.kastel.mcse.ardoco.metrics.ClassificationMetricsCalculator;
import edu.kit.kastel.mcse.ardoco.metrics.result.AggregationType;
import edu.kit.kastel.mcse.ardoco.metrics.result.SingleClassificationResult;
import edu.kit.kastel.mcse.ardoco.tlr.models.agents.CodeConfiguration;
import edu.kit.kastel.mcse.ardoco.tlr.models.informants.LargeLanguageModel;

@Disabled("Only for manual execution")
class ArchitecturalStylePatternInconsistencyDetectionEvaluationIT {

    private static final Logger logger = LoggerFactory.getLogger(ArchitecturalStylePatternInconsistencyDetectionEvaluationIT.class);

    private static final LargeLanguageModel LLM = LargeLanguageModel.GPT_5_5;

    private static Stream<Arguments> projectsXCodeConfigurationTypes() {
        return Arrays.stream(ArchitecturalStylePatternInconsistencyDetectionTask.values())
                .flatMap(task -> Arrays.stream(CodeConfiguration.CodeConfigurationType.values())
                        .map(configurationType -> Arguments.of(task, configurationType)));
    }

    @DisabledIfEnvironmentVariable(named = "mutipleRuns", matches = ".*")
    @DisplayName("Evaluate architectural style/pattern inconsistency detection")
    @ParameterizedTest(name = "Evaluating architectural style/pattern inconsistency detection for {0} with {1}")
    @MethodSource("projectsXCodeConfigurationTypes")
    void architecturalStylePatternInconsistencyDetectionIT(ArchitecturalStylePatternInconsistencyDetectionTask task,
            CodeConfiguration.CodeConfigurationType configurationType) {
        var evaluation = new ArchitecturalStylePatternInconsistencyDetectionEvaluation(task, LLM);

        SingleClassificationResult<String> result = evaluation.runInconsistencyDetectionEvaluation(configurationType);

        Assertions.assertNotNull(result);
        logExtendedResultsWithExpected(logger, task.getEvaluationProject().name() + " architectural style/pattern ID", result, task.getExpectedResults());
    }

    @DisabledIfEnvironmentVariable(named = "mutipleRuns", matches = ".*")
    @DisplayName("Evaluate architectural style/pattern NER")
    @ParameterizedTest(name = "Evaluating architectural style/pattern NER for {0}")
    @EnumSource(ArchitecturalStylePatternInconsistencyDetectionTask.class)
    void architecturalStylePatternNerIT(ArchitecturalStylePatternInconsistencyDetectionTask task) {
        var evaluation = new ArchitecturalStylePatternInconsistencyDetectionEvaluation(task, LLM);

        SingleClassificationResult<String> result = evaluation.runNerEvaluation();

        Assertions.assertNotNull(result);
        logExtendedResultsWithExpected(logger, task.getEvaluationProject().name() + " architectural style/pattern NER", result, task.getExpectedResults());
    }

    @DisabledIfEnvironmentVariable(named = "mutipleRuns", matches = ".*")
    @DisplayName("Evaluate architectural style/pattern inconsistency detection extended")
    @ParameterizedTest(name = "Evaluating architectural style/pattern inconsistency detection extended for {0} with {1}")
    @MethodSource("projectsXCodeConfigurationTypes")
    void architecturalStylePatternInconsistencyDetectionExtendedIT(ArchitecturalStylePatternInconsistencyDetectionTask task,
            CodeConfiguration.CodeConfigurationType configurationType) {
        var evaluation = new ArchitecturalStylePatternInconsistencyDetectionEvaluation(task, LLM);

        var results = evaluation.runInconsistencyDetectionExtendedEvaluation(configurationType);

        Assertions.assertNotNull(results);
        var metrics = ClassificationMetricsCalculator.getInstance();
        var weightedAverageResult = metrics.calculateAverages(results.castToList(), null)
                .asList()
                .stream()
                .filter(it -> it.getType() == AggregationType.WEIGHTED_AVERAGE)
                .findFirst()
                .orElseThrow();
        logExtendedResultsWithExpected(logger, this, task.getEvaluationProject().name() + " architectural style/pattern ID extended", weightedAverageResult,
                task.getExpectedResults());
    }

}
