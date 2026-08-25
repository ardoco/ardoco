/* Licensed under MIT 2025. */
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

public enum ClassSadCodeTlrTask implements TlrTask { //TODO delete this and TlrTask here... it belongs in tests-tlr - but the dependencies dont work...
    TEAMMATES(EvaluationProject.TEAMMATES, "/benchmark/teammates/goldstandards/goldstandard_classes_sad_2021-code_2023.csv"), SCONS(EvaluationProject.SCONS,
            "/benchmark/scons/goldstandards/goldstandard_classes_sad_2024-code_2024.csv"),//
    BEETS(EvaluationProject.BEETS, "/benchmark/beets/goldstandards/goldstandard_classes_sad_2013-code_2013.csv"),//
    ZENGARDEN(EvaluationProject.ZENGARDEN, "/benchmark/zengarden/goldstandards/goldstandard_classes_sad_2014-code_2014.csv");

    private final EvaluationProject project;
    private final String goldStandardPath;

    ClassSadCodeTlrTask(EvaluationProject project, String goldStandardPath) {
        this.project = project;
        this.goldStandardPath = goldStandardPath;
    }

    /**
     * Get the expected trace links from the gold standard file.
     * <p>
     * The pairs in the list contain the sentence number (starting at 1) and the code element ID.
     *
     * @return a list of pairs where each pair contains the sentence number and the code element ID
     */
    @Override
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
            if (modelElementId.contains(".")){ //in the goldstandard we sometimes have the classname like this: "Environment.Base" but the real name of the class in this case would be "Base"
                modelElementId = modelElementId.substring(modelElementId.lastIndexOf(".") + 1);
            }
            expectedLinks.add(new Pair<>(sentenceId, modelElementId));
        }

        return expectedLinks;
    }

    @Override
    public EvaluationProject getEvaluationProject() {
        return project;
    }
}
