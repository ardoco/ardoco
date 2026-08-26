/* Licensed under MIT 2026. */
package edu.kit.kastel.mcse.ardoco.id.tests.tasks;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import edu.kit.kastel.mcse.ardoco.core.common.tuple.Pair;
import edu.kit.kastel.mcse.ardoco.core.tests.eval.EvaluationHelper;
import edu.kit.kastel.mcse.ardoco.core.tests.eval.EvaluationProject;
import edu.kit.kastel.mcse.ardoco.core.tests.eval.ExpectedResults;
import edu.kit.kastel.mcse.ardoco.tlr.models.agents.CodeConfiguration;

public enum ClassArtemisInconsistencyTask implements ArtemisInconsistencyTask {
    TEAMMATES(EvaluationProject.TEAMMATES, //
            "/benchmark/teammates/goldstandards/goldstandard_class_team_inconsistencies.csv", //
            "/benchmark/teammates/goldstandards/goldstandard_classes_sad_2021-code_2023.csv", //
            new ExpectedResults(.420, .420, .420, .420, .420, .420) //
    ), //

    SCONS(EvaluationProject.SCONS, //
            "/benchmark/scons/goldstandards/goldstandard_class_team_inconsistencies.csv", //
            "/benchmark/scons/goldstandards/goldstandard_classes_sad_2024-code_2024.csv", //
            new ExpectedResults(.420, .420, .420, .420, .420, .420) //
    ), //

    BEETS(EvaluationProject.BEETS, //
            "/benchmark/beets/goldstandards/goldstandard_class_team_inconsistencies.csv", //
            "/benchmark/beets/goldstandards/goldstandard_classes_sad_2013-code_2013.csv", //
            new ExpectedResults(.420, .420, .420, .420, .420, .420) //
    ), //

    ZENGARDEN(EvaluationProject.ZENGARDEN, //
            "/benchmark/zengarden/goldstandards/goldstandard_class_team_inconsistencies.csv", //
            "/benchmark/zengarden/goldstandards/goldstandard_classes_sad_2014-code_2014.csv", //
            new ExpectedResults(.420, .420, .420, .420, .420, .420) //
    );

    private final EvaluationProject project;
    private final String classTeamInconsistencyGoldStandardPath;
    private final String traceLinksGoldStandardPath;
    private final ExpectedResults expectedResults;

    ClassArtemisInconsistencyTask(EvaluationProject project, String classTeamInconsistencyGoldStandardPath, String traceLinksGoldStandardPath,
            ExpectedResults expectedResults) {
        this.project = project;
        this.classTeamInconsistencyGoldStandardPath = classTeamInconsistencyGoldStandardPath;
        this.traceLinksGoldStandardPath = traceLinksGoldStandardPath;
        this.expectedResults = expectedResults;
    }

    @Override
    public EvaluationProject getEvaluationProject() {
        return project;
    }

    @Override
    public File getTextFile() {
        return project.getTextFile();
    }

    public List<String> getClassTeamInconsistencies() {
        File file = EvaluationHelper.loadFileFromResources(classTeamInconsistencyGoldStandardPath);

        List<String> lines;
        try {
            lines = Files.readAllLines(file.toPath());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }

        lines.removeFirst();
        lines.removeIf(String::isBlank);

        return lines;
    }

    @Override
    public ExpectedResults getExpectedResults() {
        return expectedResults;
    }

    @Override
    public Optional<CodeConfiguration> getCodeConfiguration() {
        return Optional.of(new CodeConfiguration(project.getCodeModelFromResources(), CodeConfiguration.CodeConfigurationType.ACM_FILE));
    }

    public List<Pair<Integer, String>> getExpectedTraceLinks() {
        File file = EvaluationHelper.loadFileFromResources(traceLinksGoldStandardPath);

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
            if (modelElementId.contains(
                    ".")) { //in the goldstandard we sometimes have the classname like this: "Environment.Base" but the real name of the class in this case would be "Base"
                modelElementId = modelElementId.substring(modelElementId.lastIndexOf(".") + 1);
            }
            expectedLinks.add(new Pair<>(sentenceId, modelElementId));
        }

        return expectedLinks;
    }
}
