/* Licensed under MIT 2025. */
package edu.kit.kastel.mcse.ardoco.tlr.tests.integration.evaluation;

import java.util.List;
import java.util.TreeSet;
import java.util.stream.Collectors;

import edu.kit.kastel.mcse.ardoco.core.api.model.Metamodel;
import edu.kit.kastel.mcse.ardoco.core.api.model.Model;
import edu.kit.kastel.mcse.ardoco.core.api.model.ModelStates;
import edu.kit.kastel.mcse.ardoco.core.api.output.ArDoCoResult;
import edu.kit.kastel.mcse.ardoco.core.api.text.Text;
import edu.kit.kastel.mcse.ardoco.core.common.tuple.Pair;
import edu.kit.kastel.mcse.ardoco.core.common.util.DataRepositoryHelper;
import edu.kit.kastel.mcse.ardoco.core.data.DataRepository;
import edu.kit.kastel.mcse.ardoco.metrics.ClassificationMetricsCalculator;
import edu.kit.kastel.mcse.ardoco.metrics.result.SingleClassificationResult;

abstract class AbstractDocumentationToCodeTlrEvaluation extends AbstractEvaluation {

    protected SingleClassificationResult<String> calculateEvaluationResults(ArDoCoResult result, List<Pair<Integer, String>> goldStandard,
            Metamodel metamodel) {
        var sadSamCodeTlsAsString = result.getSadCodeTraceLinks()
                .collect(tl -> tl.getFirstEndpoint().getSentence().getSentenceNumber() + 1 + " -> " + tl.getSecondEndpoint().toString())
                .toSortedSet();
        var goldStandardAsStrings = goldStandard.stream().map(pair -> pair.first() + " -> " + pair.second()).collect(Collectors.toCollection(TreeSet::new));

        int confusionMatrixSum = getConfusionMatrixSum(result, metamodel);
        var calculator = ClassificationMetricsCalculator.getInstance();
        return calculator.calculateMetrics(sadSamCodeTlsAsString, goldStandardAsStrings, confusionMatrixSum);
    }

    private int getConfusionMatrixSum(ArDoCoResult result, Metamodel metamodel) {
        DataRepository dataRepository = result.dataRepository();

        Text text = DataRepositoryHelper.getAnnotatedText(dataRepository);
        int sentences = text.getSentences().size();

        ModelStates modelStatesData = DataRepositoryHelper.getModelStatesData(dataRepository);
        Model codeModel = modelStatesData.getModel(metamodel);
        var codeModelEndpoints = codeModel.getEndpoints().size();

        return sentences * codeModelEndpoints;
    }
}
