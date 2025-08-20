/* Licensed under MIT 2025. */
package edu.kit.kastel.mcse.ardoco.tlr.tests.integration.evaluation;

import java.util.List;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.slf4j.LoggerFactory;

import edu.kit.kastel.mcse.ardoco.core.api.models.Metamodel;
import edu.kit.kastel.mcse.ardoco.core.api.models.Model;
import edu.kit.kastel.mcse.ardoco.core.api.models.ModelStates;
import edu.kit.kastel.mcse.ardoco.core.api.output.ArDoCoResult;
import edu.kit.kastel.mcse.ardoco.core.common.tuple.Pair;
import edu.kit.kastel.mcse.ardoco.core.common.util.DataRepositoryHelper;
import edu.kit.kastel.mcse.ardoco.core.data.DataRepository;
import edu.kit.kastel.mcse.ardoco.metrics.ClassificationMetricsCalculator;
import edu.kit.kastel.mcse.ardoco.metrics.result.SingleClassificationResult;

abstract class AbstractDocumentationToCodeTlrEvaluation extends AbstractEvaluation {

    public static SingleClassificationResult<String> calculateEvaluationResults(ArDoCoResult result, List<Pair<Integer, String>> goldStandard,
            Metamodel metamodel) {
        var sadSamCodeTlsAsString = result.getSadCodeTraceLinks()
                .collect(tl -> tl.getFirstEndpoint().getSentence().getSentenceNumber() + 1 + " -> " + tl.getSecondEndpoint().toString())
                .toSortedSet();
        var goldStandardAsStrings = goldStandard.stream().map(pair -> pair.first() + " -> " + pair.second()).collect(Collectors.toCollection(TreeSet::new));

        int confusionMatrixSum = getConfusionMatrixSum(result, metamodel);

        LoggerFactory.getLogger(AbstractDocumentationToCodeTlrEvaluation.class)
                .info("Claimed Trace Links: {}, Gold Standard: {}", sadSamCodeTlsAsString.size(), goldStandardAsStrings.size());

        var calculator = ClassificationMetricsCalculator.getInstance();
        return calculator.calculateMetrics(sadSamCodeTlsAsString, goldStandardAsStrings, confusionMatrixSum);
    }

    private static int getConfusionMatrixSum(ArDoCoResult result, Metamodel metamodel) {
        DataRepository dataRepository = result.dataRepository();

        int sentences = (int) DataRepositoryHelper.getInputText(dataRepository).lines().count();

        ModelStates modelStatesData = DataRepositoryHelper.getModelStatesData(dataRepository);
        Model codeModel = modelStatesData.getModel(metamodel);

        // TODO Code Model Endpoints of #Files ??
        var codeModelEndpoints = codeModel.getEndpoints().size();

        LoggerFactory.getLogger(AbstractDocumentationToCodeTlrEvaluation.class)
                .info("Number of sentences: {}, Number of code model endpoints: {}", sentences, codeModelEndpoints);

        return sentences * codeModelEndpoints;
    }
}
