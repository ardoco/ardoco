/* Licensed under MIT 2025. */
package edu.kit.kastel.mcse.ardoco.tlr.tests.task;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import edu.kit.kastel.mcse.ardoco.core.common.tuple.Pair;
import edu.kit.kastel.mcse.ardoco.core.tests.eval.EvaluationHelper;
import edu.kit.kastel.mcse.ardoco.core.tests.eval.EvaluationProject;

public enum Documentation2CodeTlrTask {
    MEDIASTORE(EvaluationProject.MEDIASTORE, "/benchmark/mediastore/goldstandards/goldstandard_sad_2016-code_2016.csv"),//
    TEASTORE(EvaluationProject.TEASTORE, "/benchmark/teastore/goldstandards/goldstandard_sad_2020-code_2022.csv"),//
    TEAMMATES(EvaluationProject.TEAMMATES, "/benchmark/teammates/goldstandards/goldstandard_sad_2021-code_2023.csv"),//
    BIGBLUEBUTTON(EvaluationProject.BIGBLUEBUTTON, "/benchmark/bigbluebutton/goldstandards/goldstandard_sad_2021-code_2023.csv"),//
    JABREF(EvaluationProject.JABREF, "/benchmark/jabref/goldstandards/goldstandard_sad_2021-code_2023.csv");

    private final EvaluationProject project;
    private final String goldStandardPath;

    Documentation2CodeTlrTask(EvaluationProject project, String goldStandardPath) {
        this.project = project;
        this.goldStandardPath = goldStandardPath;
    }

    public File getTextFile() {
        return project.getTextFile();
    }

    public File getCodeModelFromResources() {
        return project.getCodeModelFromResources();
    }

    public File getCodeDirectory() {
        return project.getCodeDirectory();
    }

    public File getCodeDirectoryWithoutCloning() {
        return project.getCodeDirectoryWithoutCloning();
    }

    /**
     * Get the expected trace links from the gold standard file.
     * <p>
     * The pairs in the list contain the sentence number (starting at 1) and the code element ID (path to the file/package).
     * If a ID ends with a slash, it is a package, otherwise it is a file.
     * <p>
     * <b>IMPORTANT</b> you may need to unroll the gold standard.
     *
     * @return a list of pairs where each pair contains the sentence number and the code element ID
     */
    public List<Pair<Integer, String>> getExpectedTraceLinks() {
        File file = EvaluationHelper.loadFileFromResources(goldStandardPath);

        List<String> goldLinks;
        try {
            goldLinks = Files.readAllLines(file.toPath());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        goldLinks.removeFirst(); // remove header
        goldLinks.removeIf(String::isBlank);

        List<Pair<Integer, String>> expectedLinks = new ArrayList<>();
        for (String line : goldLinks) {
            String[] parts = line.split(",");
            if (parts.length < 2) {
                throw new IllegalArgumentException("Invalid gold standard format: " + line);
            }
            int sentenceId = Integer.parseInt(parts[0].trim());
            String modelElementId = parts[1].trim();
            expectedLinks.add(new Pair<>(sentenceId, modelElementId));
        }

        return expectedLinks;
    }
}
