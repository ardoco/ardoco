/* Licensed under MIT 2025. */
package edu.kit.kastel.mcse.ardoco.tlr.tests.integration.evaluation;

import java.io.File;
import java.util.Collection;
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
import edu.kit.kastel.mcse.ardoco.core.api.stage.connectiongenerator.ner.NamedArchitectureEntityOccurrence;
import edu.kit.kastel.mcse.ardoco.core.api.stage.connectiongenerator.ner.NerConnectionState;
import edu.kit.kastel.mcse.ardoco.core.api.tracelink.TraceLink;
import edu.kit.kastel.mcse.ardoco.core.common.tuple.Pair;
import edu.kit.kastel.mcse.ardoco.core.execution.runner.ArDoCoRunner;
import edu.kit.kastel.mcse.ardoco.metrics.ClassificationMetricsCalculator;
import edu.kit.kastel.mcse.ardoco.metrics.result.SingleClassificationResult;
import edu.kit.kastel.mcse.ardoco.tlr.execution.Artemis;
import edu.kit.kastel.mcse.ardoco.tlr.models.agents.ArchitectureConfiguration;
import edu.kit.kastel.mcse.ardoco.tlr.tests.approach.ArtemisEvaluationProject;

public class ArtemisEvaluation extends AbstractEvaluation {

    protected final ArtemisEvaluationProject project;

    public ArtemisEvaluation(ArtemisEvaluationProject project) {
        this.project = Objects.requireNonNull(project);
    }

    public ArDoCoResult runTraceLinkEvaluation() {
        ArDoCoRunner artemis = createArtemis();
        ArDoCoResult result = artemis.run();
        Assertions.assertNotNull(result);

        var goldStandard = project.getTlrTask().getExpectedTraceLinks();
        var evaluationResults = this.calculateEvaluationResults(result, goldStandard);
        var expectedResults = project.getExpectedResults();

        logExtendedResultsWithExpected(project.name(), evaluationResults, expectedResults);
        compareResults(evaluationResults, expectedResults);
        return result;
    }

    private SingleClassificationResult<String> calculateEvaluationResults(ArDoCoResult result, List<Pair<Integer, String>> goldStandard) {
        var traceLinks = getArchitectureTraceLinks(result);
        var sadSamTlsAsStrings = traceLinks.collect(tl -> tl.getFirstEndpoint().getSentenceNumber() + " -> " + tl.getSecondEndpoint().getId()).toSortedSet();
        var goldStandardAsStrings = goldStandard.stream().map(pair -> pair.first() + " -> " + pair.second()).collect(Collectors.toCollection(TreeSet::new));

        int confusionMatrixSum = getConfusionMatrixSum(result);
        var calculator = ClassificationMetricsCalculator.getInstance();
        return calculator.calculateMetrics(sadSamTlsAsStrings, goldStandardAsStrings, confusionMatrixSum);
    }

    private ImmutableList<TraceLink<NamedArchitectureEntityOccurrence, ModelEntity>> getArchitectureTraceLinks(ArDoCoResult result) {
        MutableSet<TraceLink<NamedArchitectureEntityOccurrence, ModelEntity>> traceLinks = Sets.mutable.empty();
        NerConnectionState nerConnectionState = result.getNerConnectionState(Metamodel.ARCHITECTURE_WITH_COMPONENTS);
        Collection<TraceLink<NamedArchitectureEntityOccurrence, ModelEntity>> foundLinks = nerConnectionState.getTraceLinks().castToCollection();
        traceLinks.addAll(foundLinks);
        return traceLinks.toImmutableList();
    }

    private int getConfusionMatrixSum(ArDoCoResult result) {
        var text = result.getSimplePreprocessingData().getText();
        int sentences = text.getLines().size();
        int modelElements = result.getModelState(Metamodel.ARCHITECTURE_WITH_COMPONENTS).getEndpoints().size();
        return sentences * modelElements;
    }

    protected ArDoCoRunner createArtemis() {
        String projectName = project.name();
        ModelFormat architectureModelFormat = ModelFormat.PCM;
        ArchitectureConfiguration architectureModel = new ArchitectureConfiguration(project.getTlrTask().getArchitectureModelFile(architectureModelFormat),
                architectureModelFormat);
        File documentationFile = project.getTlrTask().getTextFile();
        File outputDirectory = new File("target", projectName + "-output");
        outputDirectory.mkdirs();

        Artemis artemis = new Artemis(projectName);
        artemis.setUp(documentationFile, architectureModel, SortedMaps.immutable.empty(), outputDirectory);
        return artemis;
    }

}
