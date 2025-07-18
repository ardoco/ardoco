/* Licensed under MIT 2025. */
package edu.kit.kastel.mcse.ardoco.tlr.tests.task;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import edu.kit.kastel.mcse.ardoco.core.api.models.ModelFormat;
import edu.kit.kastel.mcse.ardoco.core.common.tuple.Pair;
import edu.kit.kastel.mcse.ardoco.core.tests.evaluation.EvaluationHelper;
import edu.kit.kastel.mcse.ardoco.core.tests.evaluation.EvaluationProject;

public enum Documentation2ArchitectureModelTlr {

    MEDIASTORE(EvaluationProject.MEDIASTORE, "/benchmark/mediastore/goldstandards/goldstandard_sad_2016-sam_2016.csv"),//
    TEASTORE(EvaluationProject.TEASTORE, "/benchmark/teastore/goldstandards/goldstandard_sad_2020-sam_2020.csv"),//
    TEAMMATES(EvaluationProject.TEAMMATES, "/benchmark/teammates/goldstandards/goldstandard_sad_2021-sam_2021.csv"),//
    BIGBLUEBUTTON(EvaluationProject.BIGBLUEBUTTON, "/benchmark/bigbluebutton/goldstandards/goldstandard_sad_2021-sam_2021.csv"),//
    JABREF(EvaluationProject.JABREF, "/benchmark/jabref/goldstandards/goldstandard_sad_2021-sam_2021.csv");

    private final EvaluationProject project;
    private final String goldStandardPath;

    Documentation2ArchitectureModelTlr(EvaluationProject project, String goldStandardPath) {
        this.project = project;
        this.goldStandardPath = goldStandardPath;
    }

    public File getTextFile() {
        return project.getTextFile();
    }

    public File getArchitectureModelFile(ModelFormat modelFormat) {
        return project.getArchitectureModel(modelFormat);
    }

    /**
     * Get the expected trace links from the gold standard file.
     * <p>
     * The pairs in the list contain the sentence number (starting at 1) and the model element ID.
     * 
     * @return a list of pairs where each pair contains the sentence number and the model element ID
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
            String modelElementId = parts[0].trim();
            int sentenceId = Integer.parseInt(parts[1].trim());
            expectedLinks.add(new Pair<>(sentenceId, modelElementId));
        }
        return expectedLinks;
    }

}
