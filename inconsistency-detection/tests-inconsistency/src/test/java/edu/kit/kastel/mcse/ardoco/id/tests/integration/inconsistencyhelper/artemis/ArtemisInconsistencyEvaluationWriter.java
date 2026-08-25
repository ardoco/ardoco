package edu.kit.kastel.mcse.ardoco.id.tests.integration.inconsistencyhelper.artemis;

import static edu.kit.kastel.mcse.ardoco.id.tests.integration.inconsistencyhelper.InconsistencyDetectionEvaluationUtil.createResultLogString;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Comparator;
import java.util.Map;
import java.util.stream.Collectors;

import org.eclipse.collections.api.list.MutableList;

import edu.kit.kastel.mcse.ardoco.core.api.output.ArdocoResult;
import edu.kit.kastel.mcse.ardoco.core.common.util.FilePrinter;
import edu.kit.kastel.mcse.ardoco.id.tests.tasks.ArtemisInconsistencyTask;
import edu.kit.kastel.mcse.ardoco.metrics.ClassificationMetricsCalculator;
import edu.kit.kastel.mcse.ardoco.metrics.result.AggregationType;
import edu.kit.kastel.mcse.ardoco.metrics.result.SingleClassificationResult;

public final class ArtemisInconsistencyEvaluationWriter {
    public static final String DIRECTORY_NAME = "artemis_eval_id";

    private static final String OUTPUT = "target/testout";
    private static final String LINE_SEPARATOR = System.lineSeparator();

    private ArtemisInconsistencyEvaluationWriter() {
        throw new IllegalStateException("Utility class");
    }

    public static void writeMeatResult(ArtemisInconsistencyTask project, ArtemisInconsistencyApproach approach, SingleClassificationResult<String> result) {
        StringBuilder builder = createHeader(project, approach, "MEAT");
        builder.append(createResultLogString("MEAT inconsistencies", result));
        builder.append(LINE_SEPARATOR);
        appendConfusionSets(builder, result);

        String fileName = "MEAT_" + approach.name().toLowerCase() + "_" + project.getEvaluationProject().name() + ".txt";
        write(fileName, builder);
    }

    public static void writeTeamResult(ArtemisInconsistencyTask project, ArtemisInconsistencyApproach approach,
            MutableList<SingleClassificationResult<String>> results, Map<ArtemisEvaluationRun, ArdocoResult> runs) {
        StringBuilder builder = createHeader(project, approach, "TEAM");

        int resultIndex = 0;
        for (var run : runs.entrySet()) {
            ArtemisEvaluationRun evaluationRun = run.getKey();

            if (evaluationRun.isBaseRun()) {
                builder.append("Base run").append(LINE_SEPARATOR);
                builder.append("--------").append(LINE_SEPARATOR);
                builder.append("No model element was held back.").append(LINE_SEPARATOR).append(LINE_SEPARATOR);
                continue;
            }

            var result = results.get(resultIndex);
            resultIndex++;

            builder.append("Evaluation run: ").append(evaluationRun.displayName()).append(LINE_SEPARATOR);
            builder.append("ID: ").append(evaluationRun.id()).append(LINE_SEPARATOR);
            builder.append(String.format(java.util.Locale.ENGLISH, "Precision: %.3f, Recall: %.3f, F1: %.3f, Accuracy: %.3f, Phi: %.3f", result.getPrecision(),
                    result.getRecall(), result.getF1(), result.getAccuracy(), result.getPhiCoefficient()));
            builder.append(LINE_SEPARATOR);
            appendConfusionSets(builder, result);
            builder.append(LINE_SEPARATOR);
        }

        var microAverage = ClassificationMetricsCalculator.getInstance()
                .calculateAverages(results, null)
                .stream()
                .filter(it -> it.getType() == AggregationType.MICRO_AVERAGE)
                .findFirst()
                .orElseThrow();

        builder.append("### OVERALL RESULTS ###").append(LINE_SEPARATOR);
        builder.append(createResultLogString("Micro Average", microAverage));

        String fileName = "TEAM_" + approach.name().toLowerCase() + "_" + project.getEvaluationProject().name() + ".txt";
        write(fileName, builder);
    }

    private static StringBuilder createHeader(ArtemisInconsistencyTask project, ArtemisInconsistencyApproach approach, String kind) {
        StringBuilder builder = new StringBuilder();
        builder.append("### ")
                .append(project.getEvaluationProject().name())
                .append(" / ")
                .append(approach.getDisplayName())
                .append(" / ")
                .append(kind)
                .append(" ###");
        builder.append(LINE_SEPARATOR).append(LINE_SEPARATOR);
        return builder;
    }

    private static void appendConfusionSets(StringBuilder builder, SingleClassificationResult<?> result) {
        builder.append("True Positives: ").append(toSortedString(result.getTruePositives())).append(LINE_SEPARATOR);
        builder.append("False Positives: ").append(toSortedString(result.getFalsePositives())).append(LINE_SEPARATOR);
        builder.append("False Negatives: ").append(toSortedString(result.getFalseNegatives())).append(LINE_SEPARATOR);
        builder.append("Number of True Positives: ").append(result.getTruePositives().size()).append(LINE_SEPARATOR);
        builder.append("Number of False Positives: ").append(result.getFalsePositives().size()).append(LINE_SEPARATOR);
        builder.append("Number of False Negatives: ").append(result.getFalseNegatives().size()).append(LINE_SEPARATOR);
    }

    private static String toSortedString(Collection<?> values) {
        return values.stream().map(Object::toString).sorted(Comparator.naturalOrder()).collect(Collectors.joining(", ", "[", "]"));
    }

    private static void write(String fileName, StringBuilder builder) {
        Path directory = Path.of(OUTPUT, DIRECTORY_NAME);

        try {
            Files.createDirectories(directory);
        } catch (IOException e) {
            throw new IllegalStateException("Could not create output directory " + directory, e);
        }

        var outputFile = directory.resolve(fileName).toFile().getAbsolutePath();
        FilePrinter.writeToFile(outputFile, builder.toString());
    }
}
