/* Licensed under MIT 2026. */
package edu.kit.kastel.mcse.ardoco.id.tests.tasks;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.util.List;
import java.util.Optional;

import edu.kit.kastel.mcse.ardoco.core.tests.eval.EvaluationHelper;
import edu.kit.kastel.mcse.ardoco.core.tests.eval.EvaluationProject;
import edu.kit.kastel.mcse.ardoco.core.tests.eval.ExpectedResults;
import edu.kit.kastel.mcse.ardoco.tlr.models.agents.CodeConfiguration;

public enum ClassArtemisInconsistencyTask implements ArtemisInconsistencyTask {
    TEAMMATES(EvaluationProject.TEAMMATES, //
            "/benchmark/teammates/goldstandards/goldstandard_class_team_inconsistencies.csv", //
            new ExpectedResults(.420, .420, .420, .420, .420, .420) //
    ), //

    SCONS(EvaluationProject.SCONS, //
            "/benchmark/scons/goldstandards/goldstandard_class_team_inconsistencies.csv", //
            new ExpectedResults(.420, .420, .420, .420, .420, .420) //
    ), //

    BEETS(EvaluationProject.BEETS, //
            "/benchmark/beets/goldstandards/goldstandard_class_team_inconsistencies.csv", //
            new ExpectedResults(.420, .420, .420, .420, .420, .420) //
    ), //

    ZENGARDEN(EvaluationProject.ZENGARDEN, //
            "/benchmark/zengarden/goldstandards/goldstandard_class_team_inconsistencies.csv", //
            new ExpectedResults(.420, .420, .420, .420, .420, .420) //
    );

    private final EvaluationProject project;
    private final String classTeamInconsistencyGoldStandardPath;
    private final ExpectedResults expectedResults;

    ClassArtemisInconsistencyTask(EvaluationProject project, String classTeamInconsistencyGoldStandardPath, ExpectedResults expectedResults) {
        this.project = project;
        this.classTeamInconsistencyGoldStandardPath = classTeamInconsistencyGoldStandardPath;
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
}
