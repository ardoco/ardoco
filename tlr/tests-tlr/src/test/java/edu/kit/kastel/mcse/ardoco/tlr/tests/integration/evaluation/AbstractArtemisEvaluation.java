package edu.kit.kastel.mcse.ardoco.tlr.tests.integration.evaluation;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.eclipse.collections.api.set.sorted.MutableSortedSet;
import org.junit.jupiter.api.Assertions;

import edu.kit.kastel.mcse.ardoco.core.api.models.Metamodel;
import edu.kit.kastel.mcse.ardoco.core.api.output.ArdocoResult;
import edu.kit.kastel.mcse.ardoco.core.common.tuple.Pair;
import edu.kit.kastel.mcse.ardoco.core.execution.runner.ArdocoRunner;
import edu.kit.kastel.mcse.ardoco.metrics.ClassificationMetricsCalculator;
import edu.kit.kastel.mcse.ardoco.metrics.result.SingleClassificationResult;
import edu.kit.kastel.mcse.ardoco.core.api.stage.connectiongenerator.artemis.ArtemisConnectionState;
import edu.kit.kastel.mcse.ardoco.core.api.stage.connectiongenerator.artemis.ArtemisConnectionStates;
import edu.kit.kastel.mcse.ardoco.tlr.artemis.states.ArtemisConnectionStatesImpl;
import edu.kit.kastel.mcse.ardoco.tlr.artemis.strategies.ArtemisNerStrategy;
import edu.kit.kastel.mcse.ardoco.tlr.models.informants.LargeLanguageModel;
import edu.kit.kastel.mcse.ardoco.tlr.tests.approach.ArtemisEvaluationProject;

public abstract class AbstractArtemisEvaluation extends AbstractEvaluation {

    protected final ArtemisEvaluationProject project;
    protected final LargeLanguageModel llmForNer;

    protected AbstractArtemisEvaluation(ArtemisEvaluationProject project, LargeLanguageModel llmForNer) {
        this.project = Objects.requireNonNull(project);
        this.llmForNer = Objects.requireNonNull(llmForNer);
    }

    private static ArtemisConnectionState getArtemisConnectionState(ArdocoResult result, ArtemisNerStrategy strategy) {
        ArtemisConnectionStates states = result.dataRepository().getData(ArtemisConnectionStates.ID, ArtemisConnectionStatesImpl.class).orElseThrow();
        return states.getState(strategy.getTarget());
    }

    public SingleClassificationResult<String> runTraceLinkEvaluation() {
        ArdocoRunner runner = createArtemisRunner();
        ArdocoResult result = runner.run();
        Assertions.assertNotNull(result);

        ArtemisNerStrategy strategy = getStrategy();
        var goldStandard = project.getTlrTask().getExpectedTraceLinks();
        var evaluationResults = calculateEvaluationResults(result, goldStandard, strategy);

        var expectedResults = project.getExpectedResults();
        logExtendedResultsWithExpected(project.getName(), evaluationResults, expectedResults);
        compareResults(evaluationResults, expectedResults);

        return evaluationResults;
    }

    public SingleClassificationResult<String> calculateEvaluationResults(ArdocoResult result, List<Pair<Integer, String>> goldStandard,
            ArtemisNerStrategy strategy) {
        ArtemisConnectionState state = getArtemisConnectionState(result, strategy);
        System.out.println("unlinked: " + state.getUnlinkedNamedEntities());
        var traceLinksAsStrings = getTraceLinksAsStrings(state);
        var goldStandardAsStrings = enrollGoldStandard(goldStandard, result, strategy.getMetamodel()).stream()
                .map(pair -> pair.first() + " -> " + pair.second().toLowerCase())
                .collect(Collectors.toCollection(TreeSet::new));

        int confusionMatrixSum = getConfusionMatrixSum(result, strategy.getMetamodel());
        var calculator = ClassificationMetricsCalculator.getInstance();
        return calculator.calculateMetrics(traceLinksAsStrings, goldStandardAsStrings, confusionMatrixSum);
    }

    private int getConfusionMatrixSum(ArdocoResult result, Metamodel metamodel) {
        var text = result.getSimplePreprocessingData().getText();
        int sentences = text.getLines().size();
        int modelElements = result.getModelState(metamodel).getEndpoints().size();
        return sentences * modelElements;
    }

    protected abstract ArtemisNerStrategy getStrategy();

    protected abstract MutableSortedSet<String> getTraceLinksAsStrings(ArtemisConnectionState state);

    protected abstract ArdocoRunner createArtemisRunner();
}
