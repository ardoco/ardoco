/* Licensed under MIT 2021-2025. */
package edu.kit.kastel.mcse.ardoco.id.tests.integration;

import static edu.kit.kastel.mcse.ardoco.id.tests.integration.inconsistencyhelper.InconsistencyDetectionEvaluationUtil.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.factory.Maps;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.list.MutableList;
import org.eclipse.collections.api.tuple.Pair;
import org.eclipse.collections.impl.tuple.Tuples;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.kit.kastel.mcse.ardoco.core.api.model.ArchitectureComponentModel;
import edu.kit.kastel.mcse.ardoco.core.api.model.Metamodel;
import edu.kit.kastel.mcse.ardoco.core.api.model.ModelFormat;
import edu.kit.kastel.mcse.ardoco.core.api.model.architecture.ArchitectureItem;
import edu.kit.kastel.mcse.ardoco.core.api.output.ArDoCoResult;
import edu.kit.kastel.mcse.ardoco.core.api.stage.inconsistency.ModelInconsistency;
import edu.kit.kastel.mcse.ardoco.core.common.util.FilePrinter;
import edu.kit.kastel.mcse.ardoco.core.tests.eval.ExpectedResults;
import edu.kit.kastel.mcse.ardoco.id.tests.integration.inconsistencyhelper.HoldBackRunResultsProducer;
import edu.kit.kastel.mcse.ardoco.id.tests.tasks.InconsistencyDetectionTask;
import edu.kit.kastel.mcse.ardoco.id.types.MissingModelInstanceInconsistency;
import edu.kit.kastel.mcse.ardoco.metrics.ClassificationMetricsCalculator;
import edu.kit.kastel.mcse.ardoco.metrics.result.AggregatedClassificationResult;
import edu.kit.kastel.mcse.ardoco.metrics.result.AggregationType;
import edu.kit.kastel.mcse.ardoco.metrics.result.SingleClassificationResult;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.architecture.pcm.PcmExtractor;

/**
 * Integration test that evaluates the inconsistency detection capabilities of ArDoCo. Runs on the projects that are defined in the
 * {@link InconsistencyDetectionTask} enum.
 * <p>
 * Currently, the focus lies on detecting elements that are mentioned in the text but are not represented in the model. For this, we run an evaluation that
 * holds back (removes) one element from the model. This way, we know that there is a missing element and the trace links to this element (in the gold standard)
 * are the spots of inconsistency then. We run this multiple times so each element was held back once.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class InconsistencyDetectionEvaluationIT {
    public static final String DIRECTORY_NAME = "ardoco_eval_id";
    private static final Logger logger = LoggerFactory.getLogger(InconsistencyDetectionEvaluationIT.class);
    private static final String OUTPUT = "target/testout";
    private static final String LINE_SEPARATOR = System.lineSeparator();
    private static final Map<InconsistencyDetectionTask, ArDoCoResult> arDoCoResults = new EnumMap<>(InconsistencyDetectionTask.class);

    /**
     * Tests the inconsistency detection for missing model elements on all {@link InconsistencyDetectionTask projects}.
     * <p>
     * NOTE: if you only want to test a specific project, you can simply set up the EnumSource. For more details, see
     * <a href="https://www.baeldung.com/parameterized-tests-junit-5#3-enum">here</a>
     * Example: add ", names = { "BIGBLUEBUTTON" }" to EnumSource However, make sure to revert this before you commit and push!
     *
     * @param project Project that gets inserted automatically with the enum {@link InconsistencyDetectionTask}.
     */
    @DisplayName("Evaluating MME-Inconsistency Detection")
    @ParameterizedTest(name = "Evaluating MME-Inconsistency for {0}")
    @EnumSource(InconsistencyDetectionTask.class)
    @Order(1)
    void missingModelElementInconsistencyIT(InconsistencyDetectionTask project) {
        this.runMissingModelElementInconsistencyEval(project);
    }

    protected void runMissingModelElementInconsistencyEval(InconsistencyDetectionTask project) {
        InconsistencyDetectionEvaluationIT.logger.info("Start evaluation of MME-inconsistency for {}", project.name());
        Map<ArchitectureItem, ArDoCoResult> runs = this.produceRuns(project);

        var results = this.calculateEvaluationResults(project, runs);

        var metrics = ClassificationMetricsCalculator.getInstance();
        var microAverage = metrics.calculateAverages(results, null).stream().filter(it -> it.getType() == AggregationType.MICRO_AVERAGE).findFirst().get();

        this.logResultsMissingModelInconsistency(project, microAverage, project.getExpectedMissingModelInconsistencyResults());
        this.checkResults(microAverage, project.getExpectedMissingModelInconsistencyResults());

        this.writeOutResults(project, results, runs);
    }

    /**
     * Tests the baseline approach that reports a missing model element inconsistency for each sentence that is not traced to a model element. This test is
     * enabled by providing the environment variable "testBaseline" with any value.
     *
     * @param project Project that gets inserted automatically with the enum {@link InconsistencyDetectionTask}.
     */
    @EnabledIfEnvironmentVariable(named = "testBaseline", matches = ".*")
    @DisplayName("Evaluating MME-Inconsistency Detection Baseline")
    @ParameterizedTest(name = "Evaluating Baseline for {0}")
    @EnumSource(InconsistencyDetectionTask.class)
    @Order(5)
    void missingModelElementInconsistencyBaselineIT(InconsistencyDetectionTask project) {
        this.runMissingModelElementInconsistencyBaselineEval(project);
    }

    protected void runMissingModelElementInconsistencyBaselineEval(InconsistencyDetectionTask project) {
        InconsistencyDetectionEvaluationIT.logger.info("Start evaluation of MME-inconsistency baseline for {}", project.name());

        HoldBackRunResultsProducer holdBackRunResultsProducer = new HoldBackRunResultsProducer();
        Map<ArchitectureItem, ArDoCoResult> runs = holdBackRunResultsProducer.produceHoldBackRunResults(project, true);

        Assertions.assertTrue(runs != null && !runs.isEmpty());

        var results = this.calculateEvaluationResults(project, runs);

        var metrics = ClassificationMetricsCalculator.getInstance();

        var weightedResults = metrics.calculateAverages(results, null)
                .stream()
                .filter(it -> it.getType() == AggregationType.WEIGHTED_AVERAGE)
                .findFirst()
                .get();

        if (InconsistencyDetectionEvaluationIT.logger.isInfoEnabled()) {
            String name = project.name() + " missing model inconsistency";
            logResults(InconsistencyDetectionEvaluationIT.logger, name, weightedResults);
        }
    }

    /**
     * Tests the inconsistency detection for undocumented model elements on all {@link InconsistencyDetectionTask projects}.
     *
     * @param project Project that gets inserted automatically with the enum {@link InconsistencyDetectionTask}.
     */
    @DisplayName("Evaluate Inconsistency Analyses For MissingTextForModelElementInconsistencies")
    @ParameterizedTest(name = "Evaluating UME-inconsistency for {0}")
    @EnumSource(InconsistencyDetectionTask.class)
    @Order(10)
    void missingTextInconsistencyIT(InconsistencyDetectionTask project) {
        this.runMissingTextInconsistencyEval(project);
    }

    private void runMissingTextInconsistencyEval(InconsistencyDetectionTask project) {
        var projectResults = InconsistencyDetectionEvaluationIT.arDoCoResults.get(project);
        if (projectResults == null) {
            this.produceRuns(project);
            projectResults = InconsistencyDetectionEvaluationIT.arDoCoResults.get(project);
        }
        Assertions.assertNotNull(projectResults, "No results found.");

        List<String> expectedInconsistentModelElements = project.getUnmentionedModelElementIds();
        var inconsistentModelElements = projectResults.getAllModelInconsistencies().collect(ModelInconsistency::getModelInstanceUid).toList();
        var results = compareInconsistencies(projectResults, inconsistentModelElements.toImmutable(), Lists.immutable.withAll(
                expectedInconsistentModelElements));

        String name = project.name() + " missing text inconsistency";
        logExplicitResults(InconsistencyDetectionEvaluationIT.logger, name, results);
        this.writeOutResults(project, results);
    }

    private Map<ArchitectureItem, ArDoCoResult> produceRuns(InconsistencyDetectionTask project) {
        HoldBackRunResultsProducer holdBackRunResultsProducer = this.getHoldBackRunResultsProducer();

        Map<ArchitectureItem, ArDoCoResult> runs = holdBackRunResultsProducer.produceHoldBackRunResults(project, false);

        ArDoCoResult baseArDoCoResult = runs.get(null);
        InconsistencyDetectionEvaluationIT.saveOutput(project, baseArDoCoResult);
        InconsistencyDetectionEvaluationIT.arDoCoResults.put(project, baseArDoCoResult);
        return runs;
    }

    protected HoldBackRunResultsProducer getHoldBackRunResultsProducer() {
        return new HoldBackRunResultsProducer();
    }

    private MutableList<SingleClassificationResult<String>> calculateEvaluationResults(InconsistencyDetectionTask project,
            Map<ArchitectureItem, ArDoCoResult> runs) {

        Map<ArchitectureItem, SingleClassificationResult<String>> results = Maps.mutable.empty();

        for (var run : runs.entrySet()) {
            ArchitectureItem modelInstance = run.getKey();
            ArDoCoResult arDoCoResult = run.getValue();
            var runEvalResults = this.evaluateRun(project, modelInstance, arDoCoResult);
            if (runEvalResults != null) {
                results.put(modelInstance, runEvalResults);
            } else {
                InconsistencyDetectionEvaluationIT.logger.error("Evaluation results for {} are null.", modelInstance);
            }
        }
        return Lists.mutable.ofAll(results.values());
    }

    private SingleClassificationResult<String> evaluateRun(InconsistencyDetectionTask project, ArchitectureItem removedElement, ArDoCoResult arDoCoResult) {
        var metamodel = arDoCoResult.getMetamodels().getFirst();

        ImmutableList<MissingModelInstanceInconsistency> inconsistencies = arDoCoResult.getInconsistenciesOfTypeForModel(metamodel,
                MissingModelInstanceInconsistency.class);
        if (removedElement == null) {
            // base case
            return null;
        }

        var goldStandard = project.getGoldstandardForArchitectureModel(InconsistencyDetectionEvaluationIT.getComponentModel(project));
        var expectedLines = goldStandard.getSentencesWithElement(removedElement).distinct().collect(Object::toString);
        var actualSentences = inconsistencies.collect(MissingModelInstanceInconsistency::sentence).distinct().collect(Object::toString);

        return InconsistencyDetectionEvaluationIT.calculateEvaluationResults(arDoCoResult, expectedLines, actualSentences);
    }

    private void logResultsMissingModelInconsistency(InconsistencyDetectionTask project, AggregatedClassificationResult weightedAverageResult,
            ExpectedResults expectedResults) {
        if (InconsistencyDetectionEvaluationIT.logger.isInfoEnabled()) {
            String name = project.name() + " missing model inconsistency";
            logExtendedResultsWithExpected(InconsistencyDetectionEvaluationIT.logger, this, name, weightedAverageResult, expectedResults);
        }
    }

    private void checkResults(AggregatedClassificationResult results, ExpectedResults expectedResults) {
        Assertions.assertAll(//
                () -> Assertions.assertTrue(results.getPrecision() >= expectedResults.precision(), //
                        "Precision " + results.getPrecision() + " is below the expected minimum value " + expectedResults.precision()), //
                () -> Assertions.assertTrue(results.getRecall() >= expectedResults.recall(), //
                        "Recall " + results.getRecall() + " is below the expected minimum value " + expectedResults.recall()), //
                () -> Assertions.assertTrue(results.getF1() >= expectedResults.f1(), //
                        "F1 " + results.getF1() + " is below the expected minimum value " + expectedResults.f1()),//
                () -> Assertions.assertTrue(results.getAccuracy() >= expectedResults.accuracy(), //
                        "Accuracy " + results.getAccuracy() + " is below the expected minimum value " + expectedResults.accuracy()), //
                () -> Assertions.assertTrue(results.getPhiCoefficient() >= expectedResults.phiCoefficient(), //
                        "Phi coefficient " + results.getPhiCoefficient() + " is below the expected " + "minimum value " + expectedResults.phiCoefficient())//
        );
    }

    private static SingleClassificationResult<String> calculateEvaluationResults(ArDoCoResult arDoCoResult, ImmutableList<String> expectedLines,
            ImmutableList<String> actualSentences) {
        return compareInconsistencies(arDoCoResult, actualSentences, expectedLines);
    }

    private static ArchitectureComponentModel getComponentModel(InconsistencyDetectionTask project) {
        return new ArchitectureComponentModel(new PcmExtractor(project.getArchitectureModelFile(ModelFormat.PCM).getAbsolutePath(),
                Metamodel.ARCHITECTURE_WITH_COMPONENTS).extractModel());
    }

    private static void saveOutput(InconsistencyDetectionTask project, ArDoCoResult arDoCoResult) {
        Objects.requireNonNull(project);
        Objects.requireNonNull(arDoCoResult);

        String projectName = project.name();
        var outputDir = Path.of(InconsistencyDetectionEvaluationIT.OUTPUT);
        var filename = projectName + ".txt";

        var outputFileTLR = outputDir.resolve("traceLinks_" + filename).toFile();
        FilePrinter.writeTraceabilityLinkRecoveryOutput(outputFileTLR, arDoCoResult);
        var outputFileID = outputDir.resolve("inconsistencyDetection_" + filename).toFile();
        FilePrinter.writeInconsistencyOutput(outputFileID, arDoCoResult);
    }

    private static Pair<StringBuilder, StringBuilder> createOutput(InconsistencyDetectionTask project, List<SingleClassificationResult<String>> results,
            Map<ArchitectureItem, ArDoCoResult> runs) {
        StringBuilder outputBuilder = InconsistencyDetectionEvaluationIT.createStringBuilderWithHeader(project);
        var resultCalculatorStringBuilderPair = InconsistencyDetectionEvaluationIT.inspectResults(results, runs, outputBuilder);
        var resultCalculator = resultCalculatorStringBuilderPair.getOne();
        outputBuilder.append(InconsistencyDetectionEvaluationIT.getOverallResultsString(resultCalculator));
        var detailedOutputBuilder = resultCalculatorStringBuilderPair.getTwo();
        return Tuples.pair(outputBuilder, detailedOutputBuilder);
    }

    private static String getOverallResultsString(MutableList<SingleClassificationResult<String>> results) {
        StringBuilder outputBuilder = new StringBuilder();
        outputBuilder.append("###").append(InconsistencyDetectionEvaluationIT.LINE_SEPARATOR);
        var metrics = ClassificationMetricsCalculator.getInstance();
        var weightedAverageResults = metrics.calculateAverages(results, null)
                .stream()
                .filter(it -> it.getType() == AggregationType.WEIGHTED_AVERAGE)
                .findFirst()
                .get();
        var resultString = createResultLogString("### OVERALL RESULTS ###" + InconsistencyDetectionEvaluationIT.LINE_SEPARATOR + "Weighted" + " Average",
                weightedAverageResults);
        outputBuilder.append(resultString);
        outputBuilder.append(InconsistencyDetectionEvaluationIT.LINE_SEPARATOR);
        return outputBuilder.toString();
    }

    private static StringBuilder createStringBuilderWithHeader(InconsistencyDetectionTask project) {
        StringBuilder outputBuilder = new StringBuilder();
        outputBuilder.append("### ").append(project.name()).append(" ###");
        outputBuilder.append(InconsistencyDetectionEvaluationIT.LINE_SEPARATOR);
        return outputBuilder;
    }

    private static Pair<MutableList<SingleClassificationResult<String>>, StringBuilder> inspectResults(List<SingleClassificationResult<String>> results,
            Map<ArchitectureItem, ArDoCoResult> runs, StringBuilder outputBuilder) {
        var detailedOutputBuilder = new StringBuilder();
        MutableList<SingleClassificationResult<String>> resultsWithWeight = Lists.mutable.empty();
        int counter = 0;
        for (var run : runs.entrySet()) {
            ArDoCoResult arDoCoResult = run.getValue();
            ArchitectureItem instance = run.getKey();
            if (instance == null) {
                InconsistencyDetectionEvaluationIT.inspectBaseCase(outputBuilder, arDoCoResult);
            } else {
                outputBuilder.append("###").append(InconsistencyDetectionEvaluationIT.LINE_SEPARATOR);
                detailedOutputBuilder.append("###").append(InconsistencyDetectionEvaluationIT.LINE_SEPARATOR);
                outputBuilder.append("Removed Instance: ").append(instance.getName());
                detailedOutputBuilder.append("Removed Instance: ").append(instance.getName());
                outputBuilder.append(InconsistencyDetectionEvaluationIT.LINE_SEPARATOR);
                detailedOutputBuilder.append(InconsistencyDetectionEvaluationIT.LINE_SEPARATOR);
                var result = results.get(counter);
                counter++;
                var resultString = String.format(Locale.ENGLISH, "Precision: %.3f, Recall: %.3f, F1: %" + ".3f, Accuracy: %.3f, Phi Coef.: %.3f", result
                        .getPrecision(), result.getRecall(), result.getF1(), result.getAccuracy(), result.getPhiCoefficient());
                outputBuilder.append(resultString);
                detailedOutputBuilder.append(resultString);
                InconsistencyDetectionEvaluationIT.inspectRun(outputBuilder, detailedOutputBuilder, resultsWithWeight, arDoCoResult, result);
            }

            outputBuilder.append(InconsistencyDetectionEvaluationIT.LINE_SEPARATOR);
        }

        return Tuples.pair(resultsWithWeight, detailedOutputBuilder);
    }

    private static void inspectRun(StringBuilder outputBuilder, StringBuilder detailedOutputBuilder, MutableList<SingleClassificationResult<String>> allResults,
            ArDoCoResult arDoCoResult, SingleClassificationResult<String> result) {
        var truePositives = result.getTruePositives();
        InconsistencyDetectionEvaluationIT.appendResults(truePositives, detailedOutputBuilder, "True Positives", arDoCoResult, outputBuilder);

        var falsePositives = result.getFalsePositives();
        InconsistencyDetectionEvaluationIT.appendResults(falsePositives, detailedOutputBuilder, "False Positives", arDoCoResult, outputBuilder);

        var falseNegatives = result.getFalseNegatives();
        InconsistencyDetectionEvaluationIT.appendResults(falseNegatives, detailedOutputBuilder, "False Negatives", arDoCoResult, outputBuilder);
        allResults.add(result);
    }

    private static void appendResults(Collection<String> resultList, StringBuilder detailedOutputBuilder, String type, ArDoCoResult arDoCoResult,
            StringBuilder outputBuilder) {
        resultList = InconsistencyDetectionEvaluationIT.sortIntegerStrings(resultList);
        detailedOutputBuilder.append(InconsistencyDetectionEvaluationIT.LINE_SEPARATOR).append("== ").append(type).append(" ==");
        detailedOutputBuilder.append(InconsistencyDetectionEvaluationIT.LINE_SEPARATOR)
                .append(InconsistencyDetectionEvaluationIT.createDetailedOutputString(arDoCoResult, resultList));
        outputBuilder.append(InconsistencyDetectionEvaluationIT.LINE_SEPARATOR)
                .append(type)
                .append(": ")
                .append(InconsistencyDetectionEvaluationIT.listToString(resultList));
    }

    private static void inspectBaseCase(StringBuilder outputBuilder, ArDoCoResult data) {
        var initialInconsistencies = InconsistencyDetectionEvaluationIT.getInitialInconsistencies(data);
        outputBuilder.append("Initial Inconsistencies: ").append(initialInconsistencies.size());
        var initialInconsistenciesSentences = initialInconsistencies.collect(MissingModelInstanceInconsistency::sentence)
                .toSortedSet()
                .collect(Object::toString);
        outputBuilder.append(InconsistencyDetectionEvaluationIT.LINE_SEPARATOR)
                .append(InconsistencyDetectionEvaluationIT.listToString(initialInconsistenciesSentences));
    }

    private static String createDetailedOutputString(ArDoCoResult result, Collection<String> sentenceNumbers) {
        var outputBuilder = new StringBuilder();

        if (sentenceNumbers.isEmpty()) {
            return outputBuilder.append("None").append(InconsistencyDetectionEvaluationIT.LINE_SEPARATOR).toString();
        }

        for (var inconsistentSentence : result.getInconsistentSentences()) {
            int sentenceNumber = inconsistentSentence.sentence().getSentenceNumberForOutput();
            var sentenceNumberString = Integer.toString(sentenceNumber);
            if (sentenceNumbers.contains(sentenceNumberString)) {
                outputBuilder.append(inconsistentSentence.getInfoString());
                outputBuilder.append(InconsistencyDetectionEvaluationIT.LINE_SEPARATOR);
            }
        }

        return outputBuilder.toString();
    }

    private static List<String> sortIntegerStrings(Collection<String> list) {
        return list.stream().map(Integer::parseInt).sorted().map(Object::toString).toList();
    }

    private static String listToString(Collection<?> truePositives) {
        return truePositives.stream().map(Object::toString).collect(Collectors.joining(", ", "[", "]"));
    }

    private static ImmutableList<MissingModelInstanceInconsistency> getInitialInconsistencies(ArDoCoResult arDoCoResult) {
        var id = arDoCoResult.getMetamodels().getFirst();
        return arDoCoResult.getInconsistenciesOfTypeForModel(id, MissingModelInstanceInconsistency.class);
    }

    private void writeOutResults(InconsistencyDetectionTask project, List<SingleClassificationResult<String>> results,
            Map<ArchitectureItem, ArDoCoResult> runs) {
        var outputs = InconsistencyDetectionEvaluationIT.createOutput(project, results, runs);
        var outputBuilder = outputs.getOne();
        var detailedOutputBuilder = outputs.getTwo();

        Path outputPath = Path.of(InconsistencyDetectionEvaluationIT.OUTPUT);
        Path idEvalPath = outputPath.resolve(InconsistencyDetectionEvaluationIT.DIRECTORY_NAME);
        try {
            Files.createDirectories(outputPath);
            Files.createDirectories(idEvalPath);
        } catch (IOException e) {
            InconsistencyDetectionEvaluationIT.logger.warn("Could not create directories.", e);
        }

        String projectFileName = "inconsistencies_" + project.name() + ".txt";
        var filename = idEvalPath.resolve(projectFileName).toFile().getAbsolutePath();
        FilePrinter.writeToFile(filename, outputBuilder.toString());

        String detailedProjectFileName = "detailed_" + projectFileName;
        var detailedFilename = idEvalPath.resolve(detailedProjectFileName).toFile().getAbsolutePath();
        FilePrinter.writeToFile(detailedFilename, detailedOutputBuilder.toString());
    }

    private void writeOutResults(InconsistencyDetectionTask project, SingleClassificationResult<String> results) {
        Path outputPath = Path.of(InconsistencyDetectionEvaluationIT.OUTPUT);
        Path idEvalPath = outputPath.resolve(InconsistencyDetectionEvaluationIT.DIRECTORY_NAME);
        try {
            Files.createDirectories(outputPath);
            Files.createDirectories(idEvalPath);
        } catch (IOException e) {
            InconsistencyDetectionEvaluationIT.logger.warn("Could not create directories.", e);
        }

        var outputBuilder = InconsistencyDetectionEvaluationIT.createStringBuilderWithHeader(project);
        outputBuilder.append(createResultLogString("Inconsistent Model Elements", results));
        outputBuilder.append(InconsistencyDetectionEvaluationIT.LINE_SEPARATOR);
        outputBuilder.append("Number of True Positives: ").append(results.getTruePositives().size());
        outputBuilder.append(InconsistencyDetectionEvaluationIT.LINE_SEPARATOR);
        outputBuilder.append("Number of False Positives: ").append(results.getFalsePositives().size());
        outputBuilder.append(InconsistencyDetectionEvaluationIT.LINE_SEPARATOR);
        outputBuilder.append("Number of False Negatives: ").append(results.getFalseNegatives().size());

        String projectFileName = "inconsistentModelElements_" + project.name() + ".txt";
        var filename = idEvalPath.resolve(projectFileName).toFile().getAbsolutePath();
        FilePrinter.writeToFile(filename, outputBuilder.toString());
    }
}
