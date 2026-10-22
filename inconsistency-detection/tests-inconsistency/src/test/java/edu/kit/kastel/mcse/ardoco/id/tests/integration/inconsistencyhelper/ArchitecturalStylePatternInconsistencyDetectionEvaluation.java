/* Licensed under MIT 2026. */
package edu.kit.kastel.mcse.ardoco.id.tests.integration.inconsistencyhelper;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.factory.SortedMaps;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.list.MutableList;

import edu.kit.kastel.mcse.ardoco.core.api.output.ArdocoResult;
import edu.kit.kastel.mcse.ardoco.core.tests.eval.EvaluationHelper;
import edu.kit.kastel.mcse.ardoco.id.execution.runner.ArchitecturalStylePatternInconsistencyDetection;
import edu.kit.kastel.mcse.ardoco.id.states.ArchitecturalStylePatternInconsistency;
import edu.kit.kastel.mcse.ardoco.id.states.ArchitecturalStylePatternInconsistencyState;
import edu.kit.kastel.mcse.ardoco.id.states.ArchitecturalStylePatternInconsistencyStateImpl;
import edu.kit.kastel.mcse.ardoco.id.tests.tasks.ArchitecturalStylePatternInconsistencyDetectionTask;
import edu.kit.kastel.mcse.ardoco.metrics.ClassificationMetricsCalculator;
import edu.kit.kastel.mcse.ardoco.metrics.result.SingleClassificationResult;
import edu.kit.kastel.mcse.ardoco.tlr.models.agents.CodeConfiguration;
import edu.kit.kastel.mcse.ardoco.tlr.models.informants.LargeLanguageModel;

public class ArchitecturalStylePatternInconsistencyDetectionEvaluation {

    private final ArchitecturalStylePatternInconsistencyDetectionTask task;
    private final LargeLanguageModel llmForNer;
    private final LargeLanguageModel validationAgentLlm;

    public ArchitecturalStylePatternInconsistencyDetectionEvaluation(ArchitecturalStylePatternInconsistencyDetectionTask task, LargeLanguageModel llmForNer,
            LargeLanguageModel validationAgentLlm) {
        this.task = task;
        this.llmForNer = llmForNer;
        this.validationAgentLlm = validationAgentLlm;
    }

    private static String normalizeArchitecturalStylePattern(String name) {
        Objects.requireNonNull(name, "name must not be null");

        String normalized = name.trim().toLowerCase(Locale.ROOT);
        normalized = normalized.replace("_", "-");
        normalized = normalized.replaceAll("\\s+", " ");

        return switch (normalized) {
            case "model-view-controller", "model view controller", "mvc pattern", "model-view-controller pattern" -> "mvc";
            case "layered architecture", "layered architectural style", "layers", "layer architecture", "n-tier", "n tier" -> "layered architecture";
            case "microservice", "microservices architecture", "microservice architecture" -> "microservices";
            case "client server", "client-server architecture", "client server architecture" -> "client-server";
            case "pipe and filter", "pipes and filters", "pipe-and-filter architecture" -> "pipe-and-filter";
            case "event driven architecture", "event-driven", "event driven" -> "event-driven architecture";
            case "ports and adapters", "ports-and-adapters", "hexagonal" -> "hexagonal architecture";
            default -> normalized;
        };
    }

    public SingleClassificationResult<String> runInconsistencyDetectionEvaluation(CodeConfiguration.CodeConfigurationType configurationType) {
        ArdocoResult result = runArdoco(configurationType);
        return evaluateInconsistencyDetection(result);
    }

    public ImmutableList<SingleClassificationResult<String>> runInconsistencyDetectionExtendedEvaluation(
            CodeConfiguration.CodeConfigurationType configurationType) {
        MutableList<SingleClassificationResult<String>> results = Lists.mutable.empty();

        for (int runIndex = 0; runIndex < 5; runIndex++) {
            ArdocoResult result = runExtendedArdoco(configurationType, runIndex);
            var evaluationResult = evaluateExtendedInconsistencyDetection(result, runIndex);
            results.add(evaluationResult);
        }

        return results.toImmutable();
    }

    public SingleClassificationResult<String> runNerEvaluation() {
        ArdocoResult result = runArdoco();
        return evaluateNer(result);
    }

    public ArdocoResult runArdoco(CodeConfiguration.CodeConfigurationType configurationType) {
        String projectName = task.getEvaluationProject().name();
        File documentationFile = task.getTextFile();

        CodeConfiguration codeConfiguration = switch (configurationType) {
            case DIRECTORY -> new CodeConfiguration(task.getCodeDirectory(), CodeConfiguration.CodeConfigurationType.DIRECTORY);
            case ACM_FILE -> new CodeConfiguration(task.getEvaluationProject().getCodeModelFromResources(), CodeConfiguration.CodeConfigurationType.ACM_FILE);
            default -> throw new IllegalArgumentException("Unknown configuration type");
        };

        var runner = new ArchitecturalStylePatternInconsistencyDetection(projectName);
        runner.setUp(documentationFile, codeConfiguration, SortedMaps.immutable.empty(), llmForNer, validationAgentLlm);

        return new ArdocoResult(Objects.requireNonNull(runner.runWithoutSaving()));
    }

    private ArdocoResult runExtendedArdoco(CodeConfiguration.CodeConfigurationType configurationType, int runIndex) {
        String projectName = task.getEvaluationProject().name();
        File documentationFile = EvaluationHelper.loadFileFromResources("/architectural-style-pattern-text-extensions/" + projectName.toLowerCase(
                Locale.ROOT) + "/extension-" + runIndex + ".txt");

        CodeConfiguration codeConfiguration = switch (configurationType) {
            case DIRECTORY -> new CodeConfiguration(task.getCodeDirectory(), CodeConfiguration.CodeConfigurationType.DIRECTORY);
            case ACM_FILE -> new CodeConfiguration(task.getEvaluationProject().getCodeModelFromResources(), CodeConfiguration.CodeConfigurationType.ACM_FILE);
            default -> throw new IllegalArgumentException("Unknown configuration type");
        };

        var runner = new ArchitecturalStylePatternInconsistencyDetection(projectName);
        runner.setUp(documentationFile, codeConfiguration, SortedMaps.immutable.empty(), llmForNer, validationAgentLlm);

        return new ArdocoResult(Objects.requireNonNull(runner.runWithoutSaving()));
    }

    public ArdocoResult runArdoco() {
        return runArdoco(CodeConfiguration.CodeConfigurationType.ACM_FILE);
    }

    private SingleClassificationResult<String> evaluateInconsistencyDetection(ArdocoResult result) {
        Set<String> expected = getExpectedInvalidOccurrences();
        Set<String> actual = getActualInvalidOccurrences(result);

        return ClassificationMetricsCalculator.getInstance().calculateMetrics(actual, expected, null); // confusionMatrixSum=sentences x architecturalStylePatterns x 2 (not calculatable because #patterns is not fixed)
    }

    private SingleClassificationResult<String> evaluateExtendedInconsistencyDetection(ArdocoResult result, int runIndex) {
        Set<String> expected = getExpectedInvalidOccurrences();
        expected.addAll(getExpectedExtendedInvalidOccurrences(runIndex));
        Set<String> actual = getActualInvalidOccurrences(result);

        return ClassificationMetricsCalculator.getInstance().calculateMetrics(actual, expected, null); // confusionMatrixSum=sentences x architecturalStylePatterns x 2 (not calculatable because #patterns is not fixed)
    }

    private Set<String> getExpectedInvalidOccurrences() {
        return task.getGoldStandardEntries()
                .stream()
                .filter(entry -> !entry.valid())
                .map(entry -> entry.line() + "," + normalizeArchitecturalStylePattern(entry.architecturalStylePattern()) + ",false")
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private Set<String> getExpectedExtendedInvalidOccurrences(int runIndex) {
        File file = EvaluationHelper.loadFileFromResources("/architectural-style-pattern-text-extensions/" + task.getEvaluationProject()
                .name()
                .toLowerCase(Locale.ROOT) + "/goldstandard-extension-" + runIndex + ".csv");

        List<String> goldLines;
        try {
            goldLines = Files.readAllLines(file.toPath());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }

        goldLines.removeFirst();
        goldLines.removeIf(String::isBlank);

        return goldLines.stream().map(line -> {
            String[] parts = line.split(",");
            if (parts.length < 3) {
                throw new IllegalArgumentException("Invalid architectural style/pattern extension gold standard line: " + line);
            }

            int sentenceNumber = Integer.parseInt(parts[0].trim());
            String architecturalStylePattern = normalizeArchitecturalStylePattern(parts[1].trim());
            boolean valid = Boolean.parseBoolean(parts[2].trim());
            if (valid) {
                return null;
            }

            return sentenceNumber + "," + architecturalStylePattern + "," + valid;
        }).filter(Objects::nonNull).collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private Set<String> getActualInvalidOccurrences(ArdocoResult result) {
        ArchitecturalStylePatternInconsistencyState state = getArchitecturalStylePatternInconsistencyState(result);

        Set<String> actual = new LinkedHashSet<>();

        actual.addAll(state.getInconsistencies()
                .stream()
                .map(ArchitecturalStylePatternInconsistency::statement)
                .map(statement -> statement.sentenceNumber() + "," + statement.getArchitecturalStylePatternDisplayName() + ",false")
                .toList());

        return actual;
    }

    private SingleClassificationResult<String> evaluateNer(ArdocoResult result) {
        Set<String> expectedOccurrences = task.getExpectedOccurrences()
                .stream()
                .map(pair -> pair.first() + "," + normalizeArchitecturalStylePattern(pair.second()))
                .collect(Collectors.toCollection(LinkedHashSet::new));

        Set<String> actualOccurrences = getActualNerOccurrences(result);

        return ClassificationMetricsCalculator.getInstance().calculateMetrics(actualOccurrences, expectedOccurrences, null); // confusionMatrixSum=sentences x architecturalStylePatterns (not calculatable because #patterns is not fixed)
    }

    private Set<String> getActualNerOccurrences(ArdocoResult result) {
        ArchitecturalStylePatternInconsistencyState state = getArchitecturalStylePatternInconsistencyState(result);

        Set<String> actual = new LinkedHashSet<>();

        actual.addAll(state.getValidatedStatements()
                .stream()
                .map(statement -> statement.sentenceNumber() + "," + statement.getArchitecturalStylePatternDisplayName())
                .toList());

        actual.addAll(state.getInconsistencies()
                .stream()
                .map(ArchitecturalStylePatternInconsistency::statement)
                .map(statement -> statement.sentenceNumber() + "," + statement.getArchitecturalStylePatternDisplayName())
                .toList());

        return actual;
    }

    private ArchitecturalStylePatternInconsistencyState getArchitecturalStylePatternInconsistencyState(ArdocoResult result) {
        return result.dataRepository()
                .getData(ArchitecturalStylePatternInconsistencyState.ID, ArchitecturalStylePatternInconsistencyStateImpl.class)
                .orElseThrow(() -> new IllegalStateException("No ArchitecturalStylePatternInconsistencyState found."));
    }
}
