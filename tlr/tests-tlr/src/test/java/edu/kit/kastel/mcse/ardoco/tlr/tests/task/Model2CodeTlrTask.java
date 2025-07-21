/* Licensed under MIT 2025. */
package edu.kit.kastel.mcse.ardoco.tlr.tests.task;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import edu.kit.kastel.mcse.ardoco.core.api.model.ModelFormat;
import edu.kit.kastel.mcse.ardoco.core.common.tuple.Pair;
import edu.kit.kastel.mcse.ardoco.core.tests.eval.EvaluationHelper;
import edu.kit.kastel.mcse.ardoco.core.tests.eval.EvaluationProject;

public enum Model2CodeTlrTask {
    MEDIASTORE(EvaluationProject.MEDIASTORE, "/benchmark/mediastore/goldstandards/goldstandard_sam_2016-code_2016.csv"),//
    TEASTORE(EvaluationProject.TEASTORE, "/benchmark/teastore/goldstandards/goldstandard_sam_2020-code_2022.csv"),//
    TEAMMATES(EvaluationProject.TEAMMATES, "/benchmark/teammates/goldstandards/goldstandard_sam_2021-code_2023.csv"),//
    BIGBLUEBUTTON(EvaluationProject.BIGBLUEBUTTON, "/benchmark/bigbluebutton/goldstandards/goldstandard_sam_2021-code_2023.csv"),//
    JABREF(EvaluationProject.JABREF, "/benchmark/jabref/goldstandards/goldstandard_sam_2021-code_2023.csv");

    private final EvaluationProject project;
    private final String goldStandardPath;

    Model2CodeTlrTask(EvaluationProject project, String goldStandardPath) {
        this.project = project;
        this.goldStandardPath = goldStandardPath;
    }

    public File getArchitectureModelFile(ModelFormat modelFormat) {
        return project.getArchitectureModel(modelFormat);
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
     * The pairs in the list contain the architecture element id and the code element ID (path to the file/package).
     * If a ID ends with a slash, it is a package, otherwise it is a file.
     * <p>
     * <b>IMPORTANT</b> you may need to unroll the gold standard.
     *
     * @return a list of pairs where each pair contains the architecture element id and the code element ID
     */
    public List<Pair<String, String>> getExpectedTraceLinks() {
        // ae_id,ae_name,ce_ids
        File file = EvaluationHelper.loadFileFromResources(goldStandardPath);

        List<String> goldLinks;
        try {
            goldLinks = Files.readAllLines(file.toPath());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        goldLinks.removeFirst(); // remove header
        goldLinks.removeIf(String::isBlank);

        List<Pair<String, String>> expectedLinks = new ArrayList<>();
        for (String line : goldLinks) {
            String[] parts = line.split(",");
            if (parts.length != 3) {
                throw new IllegalArgumentException("Invalid gold standard format: " + line);
            }
            String architectureModelElementId = parts[0].trim();
            String codeModelElementId = parts[2].trim();
            expectedLinks.add(new Pair<>(architectureModelElementId, codeModelElementId));
        }
        return expectedLinks;
    }
}
