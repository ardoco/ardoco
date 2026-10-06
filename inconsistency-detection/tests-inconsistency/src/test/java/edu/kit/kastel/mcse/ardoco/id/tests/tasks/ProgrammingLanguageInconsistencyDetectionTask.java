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

public enum ProgrammingLanguageInconsistencyDetectionTask {
    TEAMMATES( //
            EvaluationProject.TEAMMATES, //
            "/benchmark/teammates/goldstandards/goldstandard_programming_language_inconsistencies_sad_2021-code_2023.csv", //
            new ExpectedResults(.420, .420, .420, .420, .420, .420) //
    ), //
    SCONS( //
            EvaluationProject.SCONS, //
            "/benchmark/scons/goldstandards/goldstandard_programming_language_inconsistencies_sad_2024-code_2024.csv", //
            new ExpectedResults(.420, .420, .420, .420, .420, .420) //
    ), //
    ROD( //
            EvaluationProject.ROD, //
            "/benchmark/rod/goldstandards/goldstandard_programming_language_inconsistencies_sad_2024-code_2024.csv", //
            new ExpectedResults(.420, .420, .420, .420, .420, .420) //
    ), //
    ZENGARDEN( //
            EvaluationProject.ZENGARDEN, //
            "/benchmark/zengarden/goldstandards/goldstandard_programming_language_inconsistencies_sad_2014-code_2014.csv", //
            new ExpectedResults(.420, .420, .420, .420, .420, .420) //
    );

    private final EvaluationProject evaluationProject;
    private final String goldStandardPath;
    private final ExpectedResults expectedResults;

    ProgrammingLanguageInconsistencyDetectionTask(EvaluationProject evaluationProject, String goldStandardPath, ExpectedResults expectedResults) {
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

    public List<ProgrammingLanguageGoldStandardEntry> getGoldStandardEntries() {
        File file = EvaluationHelper.loadFileFromResources(goldStandardPath);

        List<String> goldLines;
        try {
            goldLines = Files.readAllLines(file.toPath());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }

        goldLines.removeFirst();
        goldLines.removeIf(String::isBlank);

        List<ProgrammingLanguageGoldStandardEntry> entries = new ArrayList<>();
        for (String line : goldLines) {
            String[] parts = line.split(",");
            if (parts.length < 3) {
                throw new IllegalArgumentException("Invalid programming language gold standard line: " + line);
            }

            int sentenceNumber = Integer.parseInt(parts[0].trim());
            String programmingLanguage = parts[1].trim();
            boolean valid = Boolean.parseBoolean(parts[2].trim());

            entries.add(new ProgrammingLanguageGoldStandardEntry(sentenceNumber, programmingLanguage, valid));
        }

        return entries;
    }

    public List<Pair<Integer, String>> getExpectedInconsistencies() { //==getExpectedInvalidOccurrences
        return getGoldStandardEntries().stream().filter(entry -> !entry.valid()).map(entry -> new Pair<>(entry.line(), entry.programmingLanguage())).toList();
    }

    public List<Pair<Integer, String>> getExpectedValidOccurrences() {
        return getGoldStandardEntries().stream()
                .filter(ProgrammingLanguageGoldStandardEntry::valid)
                .map(entry -> new Pair<>(entry.line(), entry.programmingLanguage()))
                .toList();
    }

    public List<Pair<Integer, String>> getExpectedOccurrences() {
        return getGoldStandardEntries().stream().map(entry -> new Pair<>(entry.line(), entry.programmingLanguage())).toList();
    }

    public record ProgrammingLanguageGoldStandardEntry(int line, String programmingLanguage, boolean valid) {
    }
}
