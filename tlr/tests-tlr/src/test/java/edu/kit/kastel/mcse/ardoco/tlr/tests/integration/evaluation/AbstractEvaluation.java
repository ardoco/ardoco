/* Licensed under MIT 2025. */
package edu.kit.kastel.mcse.ardoco.tlr.tests.integration.evaluation;

import java.io.Serializable;
import java.util.List;
import java.util.Locale;

import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.MutableList;
import org.junit.jupiter.api.Assertions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.kit.kastel.mcse.ardoco.core.api.models.Metamodel;
import edu.kit.kastel.mcse.ardoco.core.api.models.Model;
import edu.kit.kastel.mcse.ardoco.core.api.output.ArDoCoResult;
import edu.kit.kastel.mcse.ardoco.core.common.tuple.Pair;
import edu.kit.kastel.mcse.ardoco.core.common.util.DataRepositoryHelper;
import edu.kit.kastel.mcse.ardoco.core.tests.eval.ExpectedResults;
import edu.kit.kastel.mcse.ardoco.metrics.result.SingleClassificationResult;

abstract class AbstractEvaluation {
    protected Logger logger = LoggerFactory.getLogger(this.getClass());

    protected void logExtendedResultsWithExpected(String name, SingleClassificationResult<?> results, ExpectedResults expectedResults) {
        var infoString = String.format(Locale.ENGLISH, """

                %s:
                %s""", name, getExtendedResultStringWithExpected(results, expectedResults));
        logger.info(infoString);
    }

    private String getExtendedResultStringWithExpected(SingleClassificationResult<?> result, ExpectedResults expectedResults) {
        return "" +//
                String.format(Locale.ENGLISH, """
                        \tPrecision:%8.2f (min. expected: %.2f)
                        \tRecall:%11.2f (min. expected: %.2f)
                        \tF1:%15.2f (min. expected: %.2f)""", //
                        result.getPrecision(), expectedResults.precision(), result.getRecall(), expectedResults.recall(), result.getF1(), expectedResults.f1()) //
                + String.format(Locale.ENGLISH, """

                        \tAccuracy:%9.2f (min. expected: %.2f)
                        \tSpecificity:%6.2f (min. expected: %.2f)""",//
                        result.getAccuracy(), expectedResults.accuracy(), result.getSpecificity(), expectedResults.specificity())//
                + String.format(Locale.ENGLISH, """

                        \tPhi Coef.:%8.2f (min. expected: %.2f)
                        \tPhi/PhiMax:%7.2f (Phi Max: %.2f)
                        %s""",//
                        result.getPhiCoefficient(), expectedResults.phiCoefficient(), result.getPhiOverPhiMax(), result.getPhiCoefficientMax(), toRow(result));
    }

    private String toRow(SingleClassificationResult<?> result) {
        return String.format(Locale.ENGLISH, """
                %4s & %4s & %4s & %4s & %4s & %4s & %4s
                %4.2f & %4.2f & %4.2f & %4.2f & %4.2f & %4.2f & %4.2f""", "P", "R", "F1", "Acc", "Spec", "Phi", "PhiN",//
                result.getPrecision(), result.getRecall(), result.getF1(),//
                result.getAccuracy(), result.getSpecificity(), result.getPhiCoefficient(), result.getPhiOverPhiMax());
    }

    protected void compareResults(SingleClassificationResult<?> results, ExpectedResults expectedResults) {
        Assertions.assertAll(//
                () -> Assertions.assertTrue(results.getPrecision() >= expectedResults.precision(), //
                        "Precision " + results.getPrecision() + " is below the expected minimum value " + expectedResults.precision() //
                ), //
                () -> Assertions.assertTrue(results.getRecall() >= expectedResults.recall(),//
                        "Recall " + results.getRecall() + " is below the expected minimum value " + expectedResults.recall()//
                ), //
                () -> Assertions.assertTrue(results.getF1() >= expectedResults.f1(),//
                        "F1 " + results.getF1() + " is below the expected minimum value " + expectedResults.f1()//
                ), () -> Assertions.assertTrue(results.getAccuracy() >= expectedResults.accuracy(), //
                        "Accuracy " + results.getAccuracy() + " is below the expected minimum value " + expectedResults.accuracy()//
                ), //
                () -> Assertions.assertTrue(results.getPhiCoefficient() >= expectedResults.phiCoefficient(),//
                        "Phi coefficient " + results.getPhiCoefficient() + " is below the expected minimum value " + expectedResults.phiCoefficient()//
                )//
        );
    }

    protected <T extends Serializable> List<Pair<T, String>> enrollGoldStandard(List<Pair<T, String>> goldStandard, ArDoCoResult result, Metamodel metamodel) {
        MutableList<Pair<T, String>> enrolledGoldStandard = Lists.mutable.empty();
        Model codeModel = DataRepositoryHelper.getModelStatesData(result.dataRepository()).getModel(metamodel);

        for (var traceLink : goldStandard) {
            if (!traceLink.second().endsWith("/")) {
                // Not a directory.
                enrolledGoldStandard.add(traceLink);
            } else {
                // Enroll directory.
                for (var endpoint : codeModel.getEndpoints()) {
                    var endpointPath = endpoint.toString();
                    if (endpointPath.startsWith(traceLink.second())) {
                        Pair<T, String> fileTraceLink = new Pair<>(traceLink.first(), endpointPath);
                        enrolledGoldStandard.add(fileTraceLink);
                    }
                }
            }
        }
        return enrolledGoldStandard;
    }
}
