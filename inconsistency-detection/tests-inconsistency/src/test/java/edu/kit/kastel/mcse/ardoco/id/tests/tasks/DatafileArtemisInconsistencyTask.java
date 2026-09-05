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

public enum DatafileArtemisInconsistencyTask implements ArtemisInconsistencyTask {
    CORONAWARNAPP(EvaluationProject.CORONAWARNAPP, //
            "/benchmark/coronawarnapp/goldstandards/goldstandard_datafile_team_inconsistencies.csv", //
            new ExpectedResults(.420, .420, .420, .420, .420, .420) //
    ), //

    BIGBLUEBUTTON(EvaluationProject.BIGBLUEBUTTON, //
            "/benchmark/bigbluebutton/goldstandards/goldstandard_datafile_team_inconsistencies.csv", //
            new ExpectedResults(.420, .420, .420, .420, .420, .420) //
    ), //

    ROD(EvaluationProject.ROD, //
            "/benchmark/rod/goldstandards/goldstandard_datafile_team_inconsistencies.csv", //
            new ExpectedResults(.420, .420, .420, .420, .420, .420) //
    ), //

    TEAMMATES(EvaluationProject.TEAMMATES, //
            "/benchmark/teammates/goldstandards/goldstandard_datafile_team_inconsistencies.csv", //
            new ExpectedResults(.420, .420, .420, .420, .420, .420) //
    );

    private final EvaluationProject project;
    private final String datafileTeamInconsistencyGoldStandardPath;
    private final ExpectedResults expectedResults;

    DatafileArtemisInconsistencyTask(EvaluationProject project, String datafileTeamInconsistencyGoldStandardPath, ExpectedResults expectedResults) {
        this.project = project;
        this.datafileTeamInconsistencyGoldStandardPath = datafileTeamInconsistencyGoldStandardPath;
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

    @Override
    public ExpectedResults getExpectedResults() {
        return expectedResults;
    }

    @Override
    public Optional<CodeConfiguration> getCodeConfiguration() {
        return Optional.of(new CodeConfiguration(project.getCodeModelFromResources(), CodeConfiguration.CodeConfigurationType.ACM_FILE));
    }

    public List<Pair<Integer, String>> getExpectedTraceLinks() {
        File file = EvaluationHelper.loadFileFromResources(datafileTeamInconsistencyGoldStandardPath);

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
            String datafileName = parts[1].trim();

            expectedLinks.add(new Pair<>(sentenceId, datafileName));
        }

        return expectedLinks;
    }

}
