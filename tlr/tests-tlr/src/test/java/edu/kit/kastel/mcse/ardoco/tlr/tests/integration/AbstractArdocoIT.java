/* Licensed under MIT 2025. */
package edu.kit.kastel.mcse.ardoco.tlr.tests.integration;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.kit.kastel.mcse.ardoco.metrics.result.SingleClassificationResult;

abstract class AbstractArdocoIT {
    protected static final String LOGGING_ARDOCO_CORE = "org.slf4j.simpleLogger.log.edu.kit.kastel.mcse.ardoco.core";

    protected Logger logger = LoggerFactory.getLogger(this.getClass());

    @BeforeAll
    static void beforeAll() {
        System.setProperty(LOGGING_ARDOCO_CORE, "info");
    }

    @AfterAll
    static void afterAll() {
        System.setProperty(LOGGING_ARDOCO_CORE, "error");
    }

    protected final void averageAndLog(List<SingleClassificationResult<String>> results) {
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
}
