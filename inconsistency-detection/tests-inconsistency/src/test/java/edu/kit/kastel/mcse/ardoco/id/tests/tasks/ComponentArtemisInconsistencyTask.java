/* Licensed under MIT 2026. */
package edu.kit.kastel.mcse.ardoco.id.tests.tasks;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.util.List;
import java.util.Optional;

import edu.kit.kastel.mcse.ardoco.core.api.models.ArchitectureComponentModel;
import edu.kit.kastel.mcse.ardoco.core.api.models.ModelFormat;
import edu.kit.kastel.mcse.ardoco.core.tests.eval.EvaluationHelper;
import edu.kit.kastel.mcse.ardoco.core.tests.eval.EvaluationProject;
import edu.kit.kastel.mcse.ardoco.core.tests.eval.ExpectedResults;
import edu.kit.kastel.mcse.ardoco.id.tests.eval.SentenceToArchitectureModelGoldStandard;
import edu.kit.kastel.mcse.ardoco.tlr.models.agents.ArchitectureConfiguration;

public enum ComponentArtemisInconsistencyTask implements ArtemisInconsistencyTask {
    MEDIASTORE(EvaluationProject.MEDIASTORE, //
            "/benchmark/mediastore/goldstandards/goldstandard_sad_2016-sam_2016_UME.csv", //
            "/benchmark/mediastore/goldstandards/goldstandard_sad_2016-sam_2016.csv", //
            new ExpectedResults(.127, .793, .22, .685, .227, .679) //
    ), //

    TEASTORE(EvaluationProject.TEASTORE, //
            "/benchmark/teastore/goldstandards/goldstandard_sad_2020-sam_2020_UME.csv", //
            "/benchmark/teastore/goldstandards/goldstandard_sad_2020-sam_2020.csv", //
            new ExpectedResults(.95, .703, .808, .98, .808, .998) //
    ), //

    TEAMMATES(EvaluationProject.TEAMMATES, //
            "/benchmark/teammates/goldstandards/goldstandard_sad_2021-sam_2021_UME.csv", //
            "/benchmark/teammates/goldstandards/goldstandard_sad_2021-sam_2021.csv", //
            new ExpectedResults(.147, .745, .245, .852, .287, .856) //
    ), //

    BIGBLUEBUTTON(EvaluationProject.BIGBLUEBUTTON, //
            "/benchmark/bigbluebutton/goldstandards/goldstandard_sad_2021-sam_2021_UME.csv", //
            "/benchmark/bigbluebutton/goldstandards/goldstandard_sad_2021-sam_2021.csv", //
            new ExpectedResults(.685, .403, .510, .954, .507, .988) //
    ), //

    JABREF(EvaluationProject.JABREF, //
            "/benchmark/jabref/goldstandards/goldstandard_sad_2021-sam_2021_UME.csv", //
            "/benchmark/jabref/goldstandards/goldstandard_sad_2021-sam_2021.csv", //
            new ExpectedResults(1.0, .444, .615, .871, .617, 1.0) //
    );

    private final EvaluationProject project;
    private final String unmentionedModelElementsGoldStandardPath;
    private final String documentation2ArchitectureModelGoldStandardPath;
    private final ExpectedResults expectedResults;

    ComponentArtemisInconsistencyTask(EvaluationProject project, String unmentionedModelElementsGoldStandardPath,
            String documentation2ArchitectureModelGoldStandardPath, ExpectedResults expectedResults) {
        this.project = project;
        this.unmentionedModelElementsGoldStandardPath = unmentionedModelElementsGoldStandardPath;
        this.documentation2ArchitectureModelGoldStandardPath = documentation2ArchitectureModelGoldStandardPath;
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

    public SentenceToArchitectureModelGoldStandard getGoldstandardForArchitectureModel(ArchitectureComponentModel model) {
        File file = EvaluationHelper.loadFileFromResources(documentation2ArchitectureModelGoldStandardPath);
        return new SentenceToArchitectureModelGoldStandard(file, model);
    }

    public List<String> getUnmentionedModelElementIds() {
        File unmentionedModelElementFile = EvaluationHelper.loadFileFromResources(unmentionedModelElementsGoldStandardPath);

        List<String> unmentionedModelElements;
        try {
            unmentionedModelElements = Files.readAllLines(unmentionedModelElementFile.toPath());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }

        unmentionedModelElements.removeFirst();
        unmentionedModelElements.removeIf(String::isBlank);
        return unmentionedModelElements;
    }

    @Override
    public ExpectedResults getExpectedResults() {
        return expectedResults;
    }

    @Override
    public Optional<ArchitectureConfiguration> getArchitectureConfiguration() {
        return Optional.of(new ArchitectureConfiguration(project.getArchitectureModel(ModelFormat.PCM), ModelFormat.PCM));
    }
}
