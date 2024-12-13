package edu.kit.kastel.mcse.ardoco.secdreqan;

import edu.kit.kastel.mcse.ardoco.core.tests.eval.results.ExpectedResults;

import edu.kit.kastel.mcse.ardoco.metrics.result.AggregatedClassificationResult;
import edu.kit.kastel.mcse.ardoco.metrics.result.ClassificationResult;

import org.slf4j.Logger;

import java.util.List;
import java.util.Locale;

public class TestUtil {


    public static void logExtendedResultsWithExpected(Logger logger, Object testClass, String name, ClassificationResult result,
            ExpectedResults expectedResults) {
        var infoString = String.format(Locale.ENGLISH, """

                %s (%s):
                %s""", name, testClass.getClass().getSimpleName(), getClassificationResultStringWithExpected(result, expectedResults));
        logger.info(infoString);
    }


    public static String getClassificationResultStringWithExpected(ClassificationResult result, ExpectedResults expectedResults) {
        StringBuilder outputBuilder = new StringBuilder();
        outputBuilder.append(String.format(Locale.ENGLISH, """
                \tPrecision:%8.2f (min. expected: %.2f)
                \tRecall:%11.2f (min. expected: %.2f)
                \tF1:%15.2f (min. expected: %.2f)""", result.getPrecision(), expectedResults.precision(), result.getRecall(), expectedResults.recall(), result.getF1(), expectedResults
                .f1()));
        outputBuilder.append(String.format(Locale.ENGLISH, """

                \tAccuracy:%9.2f (min. expected: %.2f)
                \tSpecificity:%6.2f (min. expected: %.2f)""", result.getAccuracy(), expectedResults.accuracy(), result.getSpecificity(), expectedResults.specificity()));
        outputBuilder.append(String.format(Locale.ENGLISH, """

                \tPhi Coef.:%8.2f (min. expected: %.2f)
                \tPhi/PhiMax:%7.2f (Phi Max: %.2f)
                %s""", result.getPhiCoefficient(), expectedResults.phiCoefficient(), result.getPhiOverPhiMax(), result.getPhiCoefficientMax(), toRow(result)));
        return outputBuilder.toString();
    }

    public static String toRow(ClassificationResult result) {
        return String.format(Locale.ENGLISH, """
                %4s & %4s & %4s & %4s & %4s & %4s & %4s
                %4.2f & %4.2f & %4.2f & %4.2f & %4.2f & %4.2f & %4.2f""", "P", "R", "F1", "Acc", "Spec", "Phi", "PhiN",
                result.getPrecision(), result.getRecall(), result.getF1(), result.getAccuracy(), result.getSpecificity(), result.getPhiCoefficient(), result.getPhiOverPhiMax());
    }



    public static void logAggregatedResults(Logger logger, Object testClass, String name, List<AggregatedClassificationResult> results) {
        var infoString = String.format(Locale.ENGLISH, """

                %s (%s):
                %s
                %s
                %s""", name, testClass.getClass().getSimpleName(), results.get(0), results.get(1), results.get(2));

        logger.info(infoString);
    }
}
