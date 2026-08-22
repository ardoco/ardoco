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
import edu.kit.kastel.mcse.ardoco.tlr.artemis.states.ArtemisTraceabilityStatesImpl;
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

    private static ArtemisConnectionState getArtemisTraceabilityState(ArdocoResult result, ArtemisNerStrategy strategy) {
        ArtemisConnectionStates states = result.dataRepository().getData(ArtemisConnectionStates.ID, ArtemisTraceabilityStatesImpl.class).orElseThrow();
        return states.getState(strategy.getTarget());
    }

    public SingleClassificationResult<String> runTraceLinkEvaluation() {
        ArdocoRunner runner = createArtemisRunner();
        ArdocoResult result = runner.run();
        Assertions.assertNotNull(result);

        ArtemisNerStrategy strategy = getStrategy();
        var goldStandard = project.getTlrTask().getExpectedTraceLinks();
        var evaluationResults = calculateEvaluationResults(result, goldStandard, strategy);

        /*System.out.println("-----------------"); //for debugging...
        System.out.println("FN:---------------\n" + evaluationResults.getFalseNegatives()
                .stream()
                .sorted(Comparator.comparingInt(s -> Integer.parseInt(s.substring(0, s.indexOf(" -> ")))))
                .collect(Collectors.joining(",\n")));
        System.out.println("FP:---------------\n" + evaluationResults.getFalsePositives()
                .stream()
                .sorted(Comparator.comparingInt(s -> Integer.parseInt(s.substring(0, s.indexOf(" -> ")))))
                .collect(Collectors.joining(",\n")));*/
        /*System.out.println("FN:---------------\n" + evaluationResults.getFalseNegatives().stream().map(s -> {
            Matcher m = Pattern.compile("->\\s*(\\S+)").matcher(s);
            return m.find()
                    ? m.replaceFirst("-> " + runner.getArdoco().getDataRepository().getData("ModelStatesData", ModelStates.class).get().getModel(Metamodel.ARCHITECTURE_WITH_COMPONENTS).getEndpoints().stream().dropWhile(e -> !e.getId().equalsIgnoreCase(m.group(1))).findFirst().get().getName())
                    : s;
        }).sorted(Comparator.comparingInt(s -> Integer.parseInt(s.substring(0, s.indexOf(" -> "))))).collect(Collectors.joining(",\n")));
        System.out.println("FP:---------------\n" + evaluationResults.getFalsePositives().stream().map(s -> {
            Matcher m = Pattern.compile("->\\s*(\\S+)").matcher(s);
            return m.find()
                    ? m.replaceFirst("-> " + runner.getArdoco().getDataRepository().getData("ModelStatesData", ModelStates.class).get().getModel(Metamodel.ARCHITECTURE_WITH_COMPONENTS).getEndpoints().stream().dropWhile(e -> !e.getId().equalsIgnoreCase(m.group(1))).findFirst().get().getName())
                    : s;
        }).sorted(Comparator.comparingInt(s -> Integer.parseInt(s.substring(0, s.indexOf(" -> "))))).collect(Collectors.joining(",\n")));*/

        var expectedResults = project.getExpectedResults();
        logExtendedResultsWithExpected(project.getName(), evaluationResults, expectedResults);
        compareResults(evaluationResults, expectedResults);

        return evaluationResults;
    }

    public SingleClassificationResult<String> calculateEvaluationResults(ArdocoResult result, List<Pair<Integer, String>> goldStandard,
            ArtemisNerStrategy strategy) {
        ArtemisConnectionState state = getArtemisTraceabilityState(result, strategy);
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
