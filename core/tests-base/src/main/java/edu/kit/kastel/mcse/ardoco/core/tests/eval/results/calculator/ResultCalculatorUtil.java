/* Licensed under MIT 2023-2024. */
package edu.kit.kastel.mcse.ardoco.core.tests.eval.results.calculator;

import java.util.List;

import org.eclipse.collections.api.factory.Sets;
import org.eclipse.collections.api.list.ImmutableList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.kit.kastel.mcse.ardoco.core.tests.eval.results.EvaluationResults;
import edu.kit.kastel.mcse.ardoco.metrics.ClassificationMetricsCalculator;
import edu.kit.kastel.mcse.ardoco.metrics.result.AggregatedClassificationResult;
import edu.kit.kastel.mcse.ardoco.metrics.result.AggregationType;
import edu.kit.kastel.mcse.ardoco.metrics.result.SingleClassificationResult;

/**
 * This utility class provides methods to form the average of several {@link EvaluationResults}
 */
public final class ResultCalculatorUtil {
    private static final Logger logger = LoggerFactory.getLogger(ResultCalculatorUtil.class);

    private ResultCalculatorUtil() {
        throw new IllegalAccessError();
    }

    public static <T> EvaluationResults<T> calculateMacroAverageResults(ImmutableList<EvaluationResults<T>> results) {
        var averages = getAverages(results);
        if (averages == null)
            return null;

        var macroAverage = averages.stream().filter(it -> it.getType() == AggregationType.MACRO_AVERAGE).findFirst().orElseThrow();
        return evaluationResults(macroAverage);
    }

    public static <T> EvaluationResults<T> calculateWeightedAverageResults(ImmutableList<EvaluationResults<T>> results) {
        var averages = getAverages(results);
        if (averages == null)
            return null;

        var macroAverage = averages.stream().filter(it -> it.getType() == AggregationType.WEIGHTED_AVERAGE).findFirst().orElseThrow();
        return evaluationResults(macroAverage);
    }

    public static EvaluationResults<String> calculateMicroAverageResults(ImmutableList<EvaluationResults<String>> results) {
        var averages = getAverages(results);
        if (averages == null)
            return null;

        var microAverage = averages.stream().filter(it -> it.getType() == AggregationType.MICRO_AVERAGE).findFirst().orElseThrow();
        return evaluationResults(microAverage);
    }

    private static <T> EvaluationResults<T> evaluationResults(AggregatedClassificationResult average) {
        var weightedAverageAsSingle = new SingleClassificationResult<T>(Sets.mutable.empty(), Sets.mutable.empty(), Sets.mutable.empty(), null, average
                .getPrecision(), average.getRecall(), average.getF1(), average.getAccuracy(), average.getSpecificity(), average.getPhiCoefficient(), average
                        .getPhiCoefficientMax(), average.getPhiOverPhiMax());

        return new EvaluationResults<>(weightedAverageAsSingle);
    }

    private static <T> List<AggregatedClassificationResult> getAverages(ImmutableList<EvaluationResults<T>> results) {
        if (results.isEmpty()) {
            throw new IllegalArgumentException("No results to calculate average from");
        }

        var calculator = ClassificationMetricsCalculator.getInstance();
        var classifications = results.stream().map(EvaluationResults::classificationResult).toList();

        return calculator.calculateAverages(classifications, null);
    }

}
