/* Licensed under MIT 2025-2026. */
package edu.kit.kastel.mcse.ardoco.id.tests.integration.inconsistencyhelper;

import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;

import org.eclipse.collections.api.collection.ImmutableCollection;
import org.slf4j.Logger;

import edu.kit.kastel.mcse.ardoco.core.api.output.ArdocoResult;
import edu.kit.kastel.mcse.ardoco.core.tests.eval.ExpectedResults;
import edu.kit.kastel.mcse.ardoco.metrics.ClassificationMetricsCalculator;
import edu.kit.kastel.mcse.ardoco.metrics.result.AggregatedClassificationResult;
import edu.kit.kastel.mcse.ardoco.metrics.result.SingleClassificationResult;

public final class InconsistencyDetectionEvaluationUtil {
    private InconsistencyDetectionEvaluationUtil() {
        throw new IllegalAccessError("Utility class should not be instantiated");
    }

    /**
     * compares the inconsistencies results with the expected results and creates a new {@link SingleClassificationResult}.
     *
     * @param arDoCoResult the ARDoCoResult
     * @param results      Collection representing the results
     * @param goldStandard Collection representing the gold standard
     * @return the result of the comparison
     */
    public static <T> SingleClassificationResult<T> compareInconsistencies(ArdocoResult arDoCoResult, ImmutableCollection<T> results,
            ImmutableCollection<T> goldStandard) {

        Set<T> distinctTraceLinks = new LinkedHashSet<>(results.castToCollection());
        Set<T> distinctGoldStandard = new LinkedHashSet<>(goldStandard.castToCollection());

        int confusionMatrixSum = arDoCoResult.getText().getSentences().size();

        var calculator = ClassificationMetricsCalculator.getInstance();
        return calculator.calculateMetrics(distinctTraceLinks, distinctGoldStandard, confusionMatrixSum);
    }

    /**
     * Log the provided {@link AggregatedClassificationResult} using the provided logger and name. The log put out the result string provided by the
     * {@link AggregatedClassificationResult}.
     *
     * @param logger  Logger to use
     * @param name    Name to show in the output
     * @param results the results
     */
    public static void logResults(Logger logger, String name, AggregatedClassificationResult results) {
        if (logger.isInfoEnabled())
            logger.info(createResultLogString(name, results));
    }

    /**
     * Logs extended results with expected results comparison using the provided logger.
     *
     * @param logger          the logger to use
     * @param testClass       the test class object
     * @param name            the name to show in the output
     * @param results         the results to log
     * @param expectedResults the expected results for comparison
     */
    public static void logExtendedResultsWithExpected(Logger logger, Object testClass, String name, AggregatedClassificationResult results,
            ExpectedResults expectedResults) {
        var infoString = String.format(Locale.ENGLISH, """

                %s (%s):
                %s""", name, testClass.getClass().getSimpleName(), getExtendedResultStringWithExpected(results, expectedResults));
        logger.info(infoString);
    }

    private static String getExtendedResultStringWithExpected(AggregatedClassificationResult results, ExpectedResults expectedResults) {
        return "" //
                + String.format(Locale.ENGLISH, """
                        \tPrecision:%8.2f (min. expected: %.2f)
                        \tRecall:%11.2f (min. expected: %.2f)
                        \tF1:%15.2f (min. expected: %.2f)""", results.getPrecision(), expectedResults.precision(), results.getRecall(), expectedResults
                        .recall(), results.getF1(), expectedResults.f1()) //
                + String.format(Locale.ENGLISH, """

                        \tAccuracy:%9.2f (min. expected: %.2f)
                        \tSpecificity:%6.2f (min. expected: %.2f)""", results.getAccuracy(), expectedResults.accuracy(), results.getSpecificity(),
                        expectedResults.specificity()) //
                + String.format(Locale.ENGLISH, """

                        \tPhi Coef.:%8.2f (min. expected: %.2f)
                        \tPhi/PhiMax:%7.2f (Phi Max: %.2f)
                        %s""", results.getPhiCoefficient(), expectedResults.phiCoefficient(), results.getPhiOverPhiMax(), results.getPhiCoefficientMax(), toRow(
                        results));
    }

    public static String toRow(AggregatedClassificationResult results) {
        return String.format(Locale.ENGLISH, """
                %4s & %4s & %4s & %4s & %4s & %4s & %4s
                %4.2f & %4.2f & %4.2f & %4.2f & %4.2f & %4.2f & %4.2f""", "P", "R", "F1", "Acc", "Spec", "Phi", "PhiN", results.getPrecision(), results
                .getRecall(), results.getF1(), results.getAccuracy(), results.getSpecificity(), results.getPhiCoefficient(), results.getPhiOverPhiMax());
    }

    /**
     * Log the provided {@link SingleClassificationResult} using the provided logger and name. Additionally, logs TP, FP, TN and FN used to calculate the
     * metrics.
     *
     * @param logger  Logger to use
     * @param name    Name to show in the output
     * @param results the results
     */
    public static void logExplicitResults(Logger logger, String name, SingleClassificationResult<?> results) {
        var tp = results.getTruePositives().size();
        var fp = results.getFalsePositives().size();
        var fn = results.getFalseNegatives().size();
        var precisionDenominator = tp + fp;
        var recallDenominator = tp + fn;
        var logString = String.format(Locale.ENGLISH, "%n%s:%n\tPrecision:%7d/%d = %.3f%n\tRecall:%10d/%d = %.3f", name, tp, precisionDenominator, results
                .getPrecision(), tp, recallDenominator, results.getRecall());
        logger.info(logString);
    }

    /**
     * Creates a string from the given results that can be used, e.g., for logging. Extracts the name as well as precision, recall and F1-score and displays
     * them line by line.
     *
     * @param name    the name that should be displayed
     * @param results the results
     * @return a String containing the name and the results (precision, recall, F1) line by line
     */
    public static String createResultLogString(String name, SingleClassificationResult<?> results) {
        return String.format(Locale.ENGLISH, "%n%s:%n%s", name, results);
    }

    /**
     * Creates a string from the given results that can be used, e.g., for logging. Extracts the name as well as precision, recall and F1-score and displays
     * them line by line.
     *
     * @param name    the name that should be displayed
     * @param results the results
     * @return a String containing the name and the results (precision, recall, F1) line by line
     */
    public static String createResultLogString(String name, AggregatedClassificationResult results) {
        return String.format(Locale.ENGLISH, "%n%s:%n%s", name, results);
    }
}
