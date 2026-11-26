/* Licensed under MIT 2026. */
package edu.kit.kastel.mcse.ardoco.id.tests.tasks;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import edu.kit.kastel.mcse.ardoco.core.common.tuple.Pair;
import edu.kit.kastel.mcse.ardoco.core.tests.eval.EvaluationHelper;
import edu.kit.kastel.mcse.ardoco.core.tests.eval.EvaluationProject;
import edu.kit.kastel.mcse.ardoco.core.tests.eval.ExpectedResults;

public enum ArchitecturalStylePatternInconsistencyDetectionTask {
    TEAMMATES( //
            EvaluationProject.TEAMMATES, //
            "/benchmark/teammates/goldstandards/goldstandard_architectural_style_pattern_inconsistencies_sad_2021-code_2023.csv", //
            new ExpectedResults(.420, .420, .420, .420, .420, .420)//
    ), //
    BEETS( //
            EvaluationProject.BEETS, //
            "/benchmark/beets/goldstandards/goldstandard_architectural_style_pattern_inconsistencies_sad_2013-code_2013.csv", //
            new ExpectedResults(.420, .420, .420, .420, .420, .420)//
    ), //
    ROD( //
            EvaluationProject.ROD, //
            "/benchmark/rod/goldstandards/goldstandard_architectural_style_pattern_inconsistencies_sad_2013-code_2013.csv", //
            new ExpectedResults(.420, .420, .420, .420, .420, .420)//
    ), //
    CORONAWARNAPP( //
            EvaluationProject.CORONAWARNAPP, //
            "/benchmark/coronawarnapp/goldstandards/goldstandard_architectural_style_pattern_inconsistencies_sad_2023-code_2023.csv", //
            new ExpectedResults(.420, .420, .420, .420, .420, .420)//
    );

    private final EvaluationProject evaluationProject;
    private final String goldStandardPath;
    private final ExpectedResults expectedResults;

    ArchitecturalStylePatternInconsistencyDetectionTask(EvaluationProject evaluationProject, String goldStandardPath, ExpectedResults expectedResults) {
        this.evaluationProject = evaluationProject;
        this.goldStandardPath = goldStandardPath;
        this.expectedResults = expectedResults;
    }

    public EvaluationProject getEvaluationProject() {
        return evaluationProject;
    }

    public File getTextFile() {
        return evaluationProject.getTextFile();
    }

    public File getCodeDirectory() {
        return evaluationProject.getCodeDirectory();
    }

    public ExpectedResults getExpectedResults() {
        return expectedResults;
    }

    public List<ArchitecturalStylePatternGoldStandardEntry> getGoldStandardEntries() {
        File file = EvaluationHelper.loadFileFromResources(goldStandardPath);

        List<String> goldLines;
        try {
            goldLines = Files.readAllLines(file.toPath());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }

        goldLines.removeFirst();
        goldLines.removeIf(String::isBlank);

        List<ArchitecturalStylePatternGoldStandardEntry> entries = new ArrayList<>();
        for (String line : goldLines) {
            String[] parts = line.split(",");
            if (parts.length < 3) {
                throw new IllegalArgumentException("Invalid architectural style/pattern gold standard line: " + line);
            }

            int sentenceNumber = Integer.parseInt(parts[0].trim());
            String architecturalStylePattern = parts[1].trim();
            boolean valid = Boolean.parseBoolean(parts[2].trim());

            entries.add(new ArchitecturalStylePatternGoldStandardEntry(sentenceNumber, architecturalStylePattern, valid));
        }

        return entries;
    }

    public List<Pair<Integer, String>> getExpectedInconsistencies() {
        return getGoldStandardEntries().stream()
                .filter(entry -> !entry.valid())
                .map(entry -> new Pair<>(entry.line(), entry.architecturalStylePattern()))
                .toList();
    }

    public List<Pair<Integer, String>> getExpectedValidOccurrences() {
        return getGoldStandardEntries().stream()
                .filter(ArchitecturalStylePatternGoldStandardEntry::valid)
                .map(entry -> new Pair<>(entry.line(), entry.architecturalStylePattern()))
                .toList();
    }

    public List<Pair<Integer, String>> getExpectedOccurrences() {
        return getGoldStandardEntries().stream().map(entry -> new Pair<>(entry.line(), entry.architecturalStylePattern())).toList();
    }

    public record ArchitecturalStylePatternGoldStandardEntry(int line, String architecturalStylePattern, boolean valid) {
    }
}
