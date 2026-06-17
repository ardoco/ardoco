/* Licensed under MIT 2025-2026. */
package edu.kit.kastel.mcse.ardoco.id.tests.integration;

import static edu.kit.kastel.mcse.ardoco.id.tests.integration.inconsistencyhelper.InconsistencyDetectionEvaluationUtil.createResultLogString;
import static edu.kit.kastel.mcse.ardoco.id.tests.integration.inconsistencyhelper.InconsistencyDetectionEvaluationUtil.logResults;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.factory.Sets;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.list.MutableList;
import org.eclipse.collections.api.set.MutableSet;
import org.eclipse.collections.api.set.sorted.ImmutableSortedSet;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.kit.kastel.mcse.ardoco.core.api.models.ArchitectureComponentModel;
import edu.kit.kastel.mcse.ardoco.core.api.models.Metamodel;
import edu.kit.kastel.mcse.ardoco.core.api.models.architecture.ArchitectureItem;
import edu.kit.kastel.mcse.ardoco.core.api.output.ArdocoResult;
import edu.kit.kastel.mcse.ardoco.core.api.stage.connectiongenerator.ner.NamedArchitectureEntity;
import edu.kit.kastel.mcse.ardoco.core.api.stage.connectiongenerator.ner.NamedArchitectureEntityOccurrence;
import edu.kit.kastel.mcse.ardoco.core.common.similarity.SimilarityUtils;
import edu.kit.kastel.mcse.ardoco.core.common.util.FilePrinter;
import edu.kit.kastel.mcse.ardoco.id.tests.integration.inconsistencyhelper.ArtemisHoldBackRunResultsProducer;
import edu.kit.kastel.mcse.ardoco.id.tests.tasks.InconsistencyDetectionTask;
import edu.kit.kastel.mcse.ardoco.id.types.TextEntityAbsentFromModelInconsistency;
import edu.kit.kastel.mcse.ardoco.metrics.ClassificationMetricsCalculator;
import edu.kit.kastel.mcse.ardoco.metrics.result.AggregationType;
import edu.kit.kastel.mcse.ardoco.metrics.result.SingleClassificationResult;
import edu.kit.kastel.mcse.ardoco.tlr.models.informants.LargeLanguageModel;

/**
 * Integration test that evaluates the inconsistency detection capabilities of ARDoCo using Artemis Approach Runs on the projects that are defined in the
 * {@link InconsistencyDetectionTask} enum.
 * <p>
 * The focus lies on detecting MEAT (Model Entity Absent from Text) and TEAM (Text Entity Absent from Model) inconsistencies.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class InconsistencyDetectionEvaluationArtemisIT {
    public static final String DIRECTORY_NAME = "artemis_eval_id";
    private static final Logger logger = LoggerFactory.getLogger(InconsistencyDetectionEvaluationArtemisIT.class);
    private static final String OUTPUT = "target/testout";
    private static final String LINE_SEPARATOR = System.lineSeparator();
    private static final LargeLanguageModel LLM = LargeLanguageModel.GPT_4_O;

    /**
     * Evaluates the MEAT (Model Entity Absent from Text) inconsistency detection task using the Artemis framework on all
     * {@link InconsistencyDetectionTask projects}. This method performs a base run of the analysis, computes the evaluation results, and logs the outcome.
     *
     * @param project Project that gets inserted automatically with the enum {@link InconsistencyDetectionTask}.
     */
    @DisplayName("Evaluating MEAT-Inconsistency Detection with Artemis")
    @ParameterizedTest(name = "Evaluating MEAT-Inconsistency for {0}")
    @EnumSource(InconsistencyDetectionTask.class)
    @Order(1)
    void modelEntityAbsentFromTextInconsistencyIT(InconsistencyDetectionTask project) {
        InconsistencyDetectionEvaluationArtemisIT.logger.info("Start evaluation of MEAT-inconsistency (Artemis) for {}", project.name());

        ArtemisHoldBackRunResultsProducer producer = new ArtemisHoldBackRunResultsProducer(LLM);
        ArdocoResult baseRun = producer.produceBaseRunResults(project);
        var results = this.calculateMeatEvaluationResults(project, baseRun);

        logger.info("MEAT-inconsistency (Artemis) results: {}", results);
        writeOutResults(project, results);
    }

    /**
     * Evaluates the TEAM (Text Entity Absent from Model) inconsistency detection task using the Artemis framework on all
     * {@link InconsistencyDetectionTask projects}. This method performs multiple runs of the analysis (each time holding back one model element to artificially
     * create a TEAM inconsistency), computes the evaluation results, and logs the outcome.
     *
     * @param project Project that gets inserted automatically with the enum {@link InconsistencyDetectionTask}.
     */
    @DisplayName("Evaluating TEAM-Inconsistency Detection with Artemis")
    @ParameterizedTest(name = "Evaluating TEAM-Inconsistency for {0}")
    @EnumSource(InconsistencyDetectionTask.class)
    @Order(2)
    void textEntityAbsentFromModelInconsistencyIT(InconsistencyDetectionTask project) {
        InconsistencyDetectionEvaluationArtemisIT.logger.info("Start evaluation of TEAM-inconsistency (Artemis) for {}", project.name());

        ArtemisHoldBackRunResultsProducer producer = new ArtemisHoldBackRunResultsProducer(LLM);
        Map<ArchitectureItem, ArdocoResult> runs = producer.produceHoldBackRunResults(project);

        var results = this.calculateTeamEvaluationResults(project, runs);

        var metrics = ClassificationMetricsCalculator.getInstance();
        var microAverage = metrics.calculateAverages(results, null).stream().filter(it -> it.getType() == AggregationType.MICRO_AVERAGE).findFirst().get();

        logResults(logger, project.name() + " (TEAM Artemis)", microAverage);
        this.writeOutResults(project, results, runs);
    }

    private List<SingleClassificationResult<Integer>> calculateTeamEvaluationResults(InconsistencyDetectionTask project,
            Map<ArchitectureItem, ArdocoResult> runs) {
        MutableList<SingleClassificationResult<Integer>> results = Lists.mutable.empty();

        var baseRun = runs.get(null);
        var model = (ArchitectureComponentModel) baseRun.getModelState(Metamodel.ARCHITECTURE_WITH_COMPONENTS);
        var goldStandard = project.getGoldstandardForArchitectureModel(model);

        for (var run : runs.entrySet()) {
            var heldBackElement = run.getKey();
            if (heldBackElement == null) {
                continue;
            }
            var result = run.getValue();

            MutableSet<Integer> expectedSentences = goldStandard.getSentencesWithElement(heldBackElement).toSet();

            var connectionState = result.getNerConnectionState(Metamodel.ARCHITECTURE_WITH_COMPONENTS);
            var namedArchitectureEntities = connectionState.getNamedArchitectureEntities();

            Set<Integer> detectedSentences = getDetectedSentences(namedArchitectureEntities, heldBackElement);

            var calculator = ClassificationMetricsCalculator.getInstance();
            var evaluationResult = calculator.calculateMetrics(detectedSentences, expectedSentences,
                    result.getSimplePreprocessingData().getText().getLines().size());
            results.add(evaluationResult);
        }

        return results;
    }

    private static Set<Integer> getDetectedSentences(ImmutableSortedSet<NamedArchitectureEntity> namedArchitectureEntities, ArchitectureItem heldBackElement) {
        var similarityUtils = SimilarityUtils.getInstance();

        var possibleHeldBackEntities = namedArchitectureEntities.stream().filter(e -> { //code hier ist vom NerConnectionInformant "geklaut"
            // Stage 1: Similarity Metrics
            if (similarityUtils.areWordsSimilar(e.getName(), heldBackElement.getName()) || similarityUtils.areWordsSimilar(heldBackElement.getName(),
                    e.getName())) {
                return true;
            }
            for (var alternativeName : e.getAlternativeNames()) {
                if (similarityUtils.areWordsSimilar(alternativeName, heldBackElement.getName()) || similarityUtils.areWordsSimilar(heldBackElement.getName(),
                        alternativeName)) {
                    return true;
                }
            }

            // Stage 2: Weak Similarity
            var nameParts = Lists.mutable.with(e.getName().split("\\s"));
            nameParts.addAll(Lists.mutable.with(e.getName().split("(?<!(^|[A-Z]))(?=[A-Z])|(?<!^)(?=[A-Z][a-z])")));
            if (similarityUtils.areWordsOfListsSimilar(nameParts.toImmutable(), heldBackElement.getNameParts()) || similarityUtils.areWordsOfListsSimilar(
                    heldBackElement.getNameParts(), nameParts.toImmutable())) {
                return true;
            }

            // TODO also use embedding similarity to get all rightful matches

            // TODO this should be deprecated if we use embeddings (currently we use this for: "kurento" and "Kurento Media Server"); can be deleted if embeddings work
            for (var part : e.getName().split("\\s")) {
                if (similarityUtils.areWordsSimilar(part, heldBackElement.getName()) || similarityUtils.areWordsSimilar(heldBackElement.getName(), part)) {
                    return true;
                }
            }

            return false;
        }).toList();

        List<NamedArchitectureEntityOccurrence> occurrences = new ArrayList<>();

        if (possibleHeldBackEntities.isEmpty()) {
            logger.warn("No match found for heldBackElement: {}", heldBackElement.getName());
        } else if (possibleHeldBackEntities.size() > 1) {
            logger.warn("Multiple matches found for heldBackElement: {} -> {}", heldBackElement.getName(), possibleHeldBackEntities);
        } else {
            occurrences = possibleHeldBackEntities.getFirst().getOccurrences();
        }

        return occurrences.stream().map(NamedArchitectureEntityOccurrence::getSentenceNumber).collect(Collectors.toSet());
    }

    private SingleClassificationResult<String> calculateMeatEvaluationResults(InconsistencyDetectionTask project, ArdocoResult result) {
        var unmentionedModelElements = project.getUnmentionedModelElementIds();
        var model = result.getModelState(Metamodel.ARCHITECTURE_WITH_COMPONENTS);
        var allModelIds = model.getEndpoints().stream().map(edu.kit.kastel.mcse.ardoco.core.api.entity.Entity::getId).collect(Collectors.toSet());

        // MEAT inconsistencies are model elements that are not mentioned in the text.
        // In our evaluation, we detect them by finding model elements that have no trace links.
        var linkedModelIds = result.getNerConnectionState(Metamodel.ARCHITECTURE_WITH_COMPONENTS)
                .getTraceLinks()
                .stream()
                .map(tl -> tl.getSecondEndpoint().getId())
                .collect(Collectors.toSet());

        MutableSet<String> detectedInconsistencies = Sets.mutable.fromStream(allModelIds.stream().filter(id -> !linkedModelIds.contains(id)));
        MutableSet<String> goldStandard = Sets.mutable.withAll(unmentionedModelElements);

        var calculator = ClassificationMetricsCalculator.getInstance();
        return calculator.calculateMetrics(detectedInconsistencies, goldStandard, result.getSimplePreprocessingData().getText().getLines().size());
    }

    private void writeOutResults(InconsistencyDetectionTask project, List<SingleClassificationResult<Integer>> results,
            Map<ArchitectureItem, ArdocoResult> runs) {
        var outputBuilder = createOutput(project, results, runs);

        Path outputPath = Path.of(OUTPUT);
        Path idEvalPath = outputPath.resolve(DIRECTORY_NAME);
        try {
            Files.createDirectories(outputPath);
            Files.createDirectories(idEvalPath);
        } catch (IOException e) {
            logger.warn("Could not create directories.", e);
        }

        String projectFileName = "TEAM_inconsistencies_" + project.name() + ".txt";
        var filename = idEvalPath.resolve(projectFileName).toFile().getAbsolutePath();
        FilePrinter.writeToFile(filename, outputBuilder.toString());
    }

    private void writeOutResults(InconsistencyDetectionTask project, SingleClassificationResult<String> results) {
        Path outputPath = Path.of(OUTPUT);
        Path idEvalPath = outputPath.resolve(DIRECTORY_NAME);
        try {
            Files.createDirectories(outputPath);
            Files.createDirectories(idEvalPath);
        } catch (IOException e) {
            logger.warn("Could not create directories.", e);
        }

        var outputBuilder = createStringBuilderWithHeader(project);
        outputBuilder.append(createResultLogString("MEAT inconsistencies", results));
        outputBuilder.append(LINE_SEPARATOR);
        outputBuilder.append("Number of True Positives: ").append(results.getTruePositives().size());
        outputBuilder.append(LINE_SEPARATOR);
        outputBuilder.append("Number of False Positives: ").append(results.getFalsePositives().size());
        outputBuilder.append(LINE_SEPARATOR);
        outputBuilder.append("Number of False Negatives: ").append(results.getFalseNegatives().size());

        String projectFileName = "MEAT_inconsistencies_" + project.name() + ".txt";
        var filename = idEvalPath.resolve(projectFileName).toFile().getAbsolutePath();
        FilePrinter.writeToFile(filename, outputBuilder.toString());
    }

    private static StringBuilder createOutput(InconsistencyDetectionTask project, List<SingleClassificationResult<Integer>> results,
            Map<ArchitectureItem, ArdocoResult> runs) {
        StringBuilder outputBuilder = createStringBuilderWithHeader(project);
        var resultCalculator = inspectResults(results, runs, outputBuilder);
        outputBuilder.append(getOverallResultsString(resultCalculator));
        return outputBuilder;
    }

    private static String getOverallResultsString(MutableList<SingleClassificationResult<Integer>> results) {
        StringBuilder outputBuilder = new StringBuilder();
        outputBuilder.append("###").append(LINE_SEPARATOR);
        var metrics = ClassificationMetricsCalculator.getInstance();
        var weightedAverageResults = metrics.calculateAverages(results, null)
                .stream()
                .filter(it -> it.getType() == AggregationType.WEIGHTED_AVERAGE)
                .findFirst()
                .get();
        var resultString = createResultLogString("### OVERALL RESULTS ###" + LINE_SEPARATOR + "Weighted" + " Average", weightedAverageResults);
        outputBuilder.append(resultString);
        outputBuilder.append(LINE_SEPARATOR);
        return outputBuilder.toString();
    }

    private static StringBuilder createStringBuilderWithHeader(InconsistencyDetectionTask project) {
        StringBuilder outputBuilder = new StringBuilder();
        outputBuilder.append("### ").append(project.name()).append(" ###");
        outputBuilder.append(LINE_SEPARATOR);
        return outputBuilder;
    }

    private static MutableList<SingleClassificationResult<Integer>> inspectResults(List<SingleClassificationResult<Integer>> results,
            Map<ArchitectureItem, ArdocoResult> runs, StringBuilder outputBuilder) {
        MutableList<SingleClassificationResult<Integer>> resultsWithWeight = Lists.mutable.empty();
        int counter = 0;
        for (var run : runs.entrySet()) {
            ArdocoResult arDoCoResult = run.getValue();
            ArchitectureItem instance = run.getKey();
            if (instance == null) {
                inspectBaseCase(outputBuilder, arDoCoResult);
            } else {
                outputBuilder.append("###").append(LINE_SEPARATOR);
                outputBuilder.append("Removed Instance: ").append(instance.getName());
                outputBuilder.append(LINE_SEPARATOR);
                var result = results.get(counter);
                counter++;
                var resultString = String.format(Locale.ENGLISH, "Precision: %.3f, Recall: %.3f, F1: %" + ".3f, Accuracy: %.3f, Phi Coef.: %.3f",
                        result.getPrecision(), result.getRecall(), result.getF1(), result.getAccuracy(), result.getPhiCoefficient());
                outputBuilder.append(resultString);
                inspectRun(outputBuilder, resultsWithWeight, result);
            }

            outputBuilder.append(LINE_SEPARATOR);
        }

        return resultsWithWeight;
    }

    private static void inspectRun(StringBuilder outputBuilder, MutableList<SingleClassificationResult<Integer>> allResults,
            SingleClassificationResult<Integer> result) {
        var truePositives = result.getTruePositives();
        appendResults(truePositives, "True Positives", outputBuilder);

        var falsePositives = result.getFalsePositives();
        appendResults(falsePositives, "False Positives", outputBuilder);

        var falseNegatives = result.getFalseNegatives();
        appendResults(falseNegatives, "False Negatives", outputBuilder);
        allResults.add(result);
    }

    private static void appendResults(Collection<Integer> resultList, String type, StringBuilder outputBuilder) {
        resultList = resultList.stream().sorted().collect(Collectors.toList());
        outputBuilder.append(LINE_SEPARATOR).append(type).append(": ").append(listToString(resultList));
    }

    private static void inspectBaseCase(StringBuilder outputBuilder, ArdocoResult data) {
        var initialInconsistencies = getInitialInconsistencies(data);
        outputBuilder.append("Initial Inconsistencies: ").append(initialInconsistencies.size());
        var initialInconsistenciesSentences = initialInconsistencies.collect(TextEntityAbsentFromModelInconsistency::sentence)
                .toSortedSet()
                .collect(Object::toString);
        outputBuilder.append(LINE_SEPARATOR).append(listToString(initialInconsistenciesSentences));
    }

    private static String listToString(Collection<?> truePositives) {
        return truePositives.stream().map(Object::toString).collect(Collectors.joining(", ", "[", "]"));
    }

    private static ImmutableList<TextEntityAbsentFromModelInconsistency> getInitialInconsistencies(ArdocoResult arDoCoResult) {
        var id = arDoCoResult.getMetamodels().getFirst();
        return arDoCoResult.getInconsistenciesOfTypeForModel(id, TextEntityAbsentFromModelInconsistency.class);
    }

}
