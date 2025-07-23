/* Licensed under MIT 2025. */
package edu.kit.kastel.mcse.ardoco.tlr.tests.integration.evaluation;

import java.io.File;
import java.util.List;
import java.util.Objects;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.eclipse.collections.api.factory.Sets;
import org.eclipse.collections.api.factory.SortedMaps;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.set.MutableSet;
import org.junit.jupiter.api.Assertions;

import edu.kit.kastel.mcse.ardoco.core.api.entity.ModelEntity;
import edu.kit.kastel.mcse.ardoco.core.api.models.Metamodel;
import edu.kit.kastel.mcse.ardoco.core.api.models.ModelFormat;
import edu.kit.kastel.mcse.ardoco.core.api.output.ArDoCoResult;
import edu.kit.kastel.mcse.ardoco.core.api.text.SentenceEntity;
import edu.kit.kastel.mcse.ardoco.core.api.tracelink.TraceLink;
import edu.kit.kastel.mcse.ardoco.core.common.tuple.Pair;
import edu.kit.kastel.mcse.ardoco.core.execution.runner.ArDoCoRunner;
import edu.kit.kastel.mcse.ardoco.metrics.ClassificationMetricsCalculator;
import edu.kit.kastel.mcse.ardoco.metrics.result.SingleClassificationResult;
import edu.kit.kastel.mcse.ardoco.tlr.execution.Swattr;
import edu.kit.kastel.mcse.ardoco.tlr.models.agents.ArchitectureConfiguration;
import edu.kit.kastel.mcse.ardoco.tlr.tests.approach.SwattrEvaluationProject;

public class SwattrEvaluation extends AbstractEvaluation {

    private final SwattrEvaluationProject project;

    public SwattrEvaluation(SwattrEvaluationProject project) {
        this.project = Objects.requireNonNull(project);
    }

    public ArDoCoResult runTraceLinkEvaluation() {
        ArDoCoRunner swattr = createSwattrRunner();
        ArDoCoResult result = swattr.run();
        Assertions.assertNotNull(result);

        var goldStandard = project.getTlrTask().getExpectedTraceLinks();
        var evaluationResults = this.calculateEvaluationResults(result, goldStandard);
        var expectedResults = project.getExpectedResults();

        logExtendedResultsWithExpected(project.name(), evaluationResults, expectedResults);
        compareResults(evaluationResults, expectedResults);
        return result;

    }

    private SingleClassificationResult<String> calculateEvaluationResults(ArDoCoResult result, List<Pair<Integer, String>> goldStandard) {
        var sadSamTlsAsStrings = getArchitectureTraceLinks(result).collect(tl -> tl.getFirstEndpoint().getSentence().getSentenceNumber() + 1 + " -> " + tl
                .getSecondEndpoint()
                .getId()).toSortedSet();
        var goldStandardAsStrings = goldStandard.stream().map(pair -> pair.first() + " -> " + pair.second()).collect(Collectors.toCollection(TreeSet::new));

        int confusionMatrixSum = getConfusionMatrixSum(result);
        var calculator = ClassificationMetricsCalculator.getInstance();
        return calculator.calculateMetrics(sadSamTlsAsStrings, goldStandardAsStrings, confusionMatrixSum);
    }

    private ImmutableList<TraceLink<SentenceEntity, ModelEntity>> getArchitectureTraceLinks(ArDoCoResult result) {
        MutableSet<TraceLink<SentenceEntity, ModelEntity>> traceLinks = Sets.mutable.empty();
        traceLinks.addAll(result.getConnectionState(Metamodel.ARCHITECTURE_WITH_COMPONENTS).getTraceLinks().castToCollection());
        return traceLinks.toImmutableList();
    }

    private int getConfusionMatrixSum(ArDoCoResult result) {
        int sentences = result.getText().getSentences().size();
        int modelElements = result.getModelState(Metamodel.ARCHITECTURE_WITH_COMPONENTS).getEndpoints().size();
        return sentences * modelElements;
    }

    private ArDoCoRunner createSwattrRunner() {
        String projectName = project.name();
        ModelFormat architectureModelFormat = ModelFormat.PCM;
        ArchitectureConfiguration architectureModel = new ArchitectureConfiguration(project.getTlrTask().getArchitectureModelFile(architectureModelFormat),
                architectureModelFormat);
        File documentationFile = project.getTlrTask().getTextFile();
        File outputDirectory = new File("target", projectName + "-output");
        outputDirectory.mkdirs();

        Swattr swattr = new Swattr(projectName);
        swattr.setUp(documentationFile, architectureModel, SortedMaps.immutable.empty(), outputDirectory);
        return swattr;
    }

}
