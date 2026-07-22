package edu.kit.kastel.mcse.ardoco.tlr.tests.integration.evaluation;

import java.util.List;
import java.util.Objects;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.set.sorted.MutableSortedSet;
import org.junit.jupiter.api.Assertions;

import edu.kit.kastel.mcse.ardoco.core.api.entity.ModelEntity;
import edu.kit.kastel.mcse.ardoco.core.api.models.Metamodel;
import edu.kit.kastel.mcse.ardoco.core.api.output.ArdocoResult;
import edu.kit.kastel.mcse.ardoco.core.api.stage.connectiongenerator.ner.NamedArchitectureEntityOccurrence;
import edu.kit.kastel.mcse.ardoco.core.api.tracelink.TraceLink;
import edu.kit.kastel.mcse.ardoco.core.common.tuple.Pair;
import edu.kit.kastel.mcse.ardoco.metrics.ClassificationMetricsCalculator;
import edu.kit.kastel.mcse.ardoco.metrics.result.SingleClassificationResult;
import edu.kit.kastel.mcse.ardoco.tlr.execution.AbstractArtemis;
import edu.kit.kastel.mcse.ardoco.tlr.models.informants.LargeLanguageModel;
import edu.kit.kastel.mcse.ardoco.tlr.tests.approach.ArtemisEvaluationProject;

public abstract class AbstractArtemisEvaluation extends AbstractEvaluation {
    protected final ArtemisEvaluationProject project;
    protected final LargeLanguageModel llmForNer;

    public AbstractArtemisEvaluation(ArtemisEvaluationProject project, LargeLanguageModel llmForNer) {
        this.project = Objects.requireNonNull(project);
        this.llmForNer = Objects.requireNonNull(llmForNer);
    }

    public SingleClassificationResult<String> runTraceLinkEvaluation() {
        AbstractArtemis artemis = createArtemis();
        ArdocoResult result = artemis.run();
        Assertions.assertNotNull(result);

        var goldStandard = project.getTlrTask().getExpectedTraceLinks();
        var evaluationResults = this.calculateEvaluationResults(result, goldStandard, artemis.getNerStrategy().getMetamodel());
        var expectedResults = project.getExpectedResults();

        logExtendedResultsWithExpected(project.getName(), evaluationResults, expectedResults);
        compareResults(evaluationResults, expectedResults);
        return evaluationResults;
    }

    public SingleClassificationResult<String> calculateEvaluationResults(ArdocoResult result, List<Pair<Integer, String>> goldStandard, Metamodel metamodel) {
        var traceLinks = result.getNerConnectionState(metamodel).getTraceLinks();
        var traceLinksAsStrings = getTraceLinksAsStrings(traceLinks);
        var goldStandardAsStrings = goldStandard.stream().map(pair -> pair.first() + " -> " + pair.second()).collect(Collectors.toCollection(TreeSet::new));

        int confusionMatrixSum = getConfusionMatrixSum(result, metamodel);
        var calculator = ClassificationMetricsCalculator.getInstance();
        return calculator.calculateMetrics(traceLinksAsStrings, goldStandardAsStrings, confusionMatrixSum);
    }

    public abstract MutableSortedSet<String> getTraceLinksAsStrings(ImmutableList<TraceLink<NamedArchitectureEntityOccurrence, ModelEntity>> traceLinks);

    private int getConfusionMatrixSum(ArdocoResult result, Metamodel metamodel) {
        var text = result.getSimplePreprocessingData().getText();
        int sentences = text.getLines().size();
        int modelElements = result.getModelState(metamodel).getEndpoints().size();
        return sentences * modelElements;
    }

    public abstract AbstractArtemis createArtemis();
}
