/* Licensed under MIT 2025. */
package edu.kit.kastel.mcse.ardoco.id.tests.tasks;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.util.List;

import edu.kit.kastel.mcse.ardoco.core.api.model.ArchitectureComponentModel;
import edu.kit.kastel.mcse.ardoco.core.api.model.ModelFormat;
import edu.kit.kastel.mcse.ardoco.core.tests.eval.EvaluationHelper;
import edu.kit.kastel.mcse.ardoco.core.tests.eval.EvaluationProject;
import edu.kit.kastel.mcse.ardoco.core.tests.eval.ExpectedResults;
import edu.kit.kastel.mcse.ardoco.id.tests.eval.GoldStandard;

public enum InconsistencyDetectionTask {
    MEDIASTORE(EvaluationProject.MEDIASTORE, //
            "/benchmark/mediastore/goldstandards/goldstandard_sad_2016-sam_2016_UME.csv", //
            "/benchmark/mediastore/goldstandards/goldstandard_sad_2016-sam_2016.csv", //
            "/configurations/ms/filterlists_all.txt" // options: filterlists_none.txt, filterlists_onlyCommon.txt, filterlists_all.txt
            , new ExpectedResults(.127, .793, .22, .685, .227, .679) //
    ), //

    TEASTORE(EvaluationProject.TEASTORE, //
            "/benchmark/teastore/goldstandards/goldstandard_sad_2020-sam_2020_UME.csv", //
            "/benchmark/teastore/goldstandards/goldstandard_sad_2020-sam_2020.csv", //
            "/configurations/ts/filterlists_all.txt" // options: filterlists_none.txt, filterlists_onlyCommon.txt, filterlists_all.txt
            , new ExpectedResults(.95, .703, .808, .98, .808, .998) //
    ), //

    TEAMMATES(EvaluationProject.TEAMMATES, //
            "/benchmark/teammates/goldstandards/goldstandard_sad_2021-sam_2021_UME.csv", //
            "/benchmark/teammates/goldstandards/goldstandard_sad_2021-sam_2021.csv", //
            "/configurations/tm/filterlists_all.txt" // options: filterlists_none.txt, filterlists_onlyCommon.txt, filterlists_all.txt
            , new ExpectedResults(.147, .745, .245, .852, .287, .856) //
    ), //

    BIGBLUEBUTTON(EvaluationProject.BIGBLUEBUTTON, //
            "/benchmark/bigbluebutton/goldstandards/goldstandard_sad_2021-sam_2021_UME.csv", //
            "/benchmark/bigbluebutton/goldstandards/goldstandard_sad_2021-sam_2021.csv", //
            "/configurations/bbb/filterlists_all.txt" // options: filterlists_none.txt, filterlists_onlyCommon.txt, filterlists_all.txt
            , new ExpectedResults(.666, .461, .545, .96, .535, .988) //
    ), //

    JABREF(EvaluationProject.JABREF, //
            "/benchmark/jabref/goldstandards/goldstandard_sad_2021-sam_2021_UME.csv", //
            "/benchmark/jabref/goldstandards/goldstandard_sad_2021-sam_2021.csv", //
            "/configurations/jabref/filterlists_all.txt" // options: filterlists_none.txt, filterlists_onlyCommon.txt, filterlists_all.txt
            , new ExpectedResults(1.0, .444, .615, .871, .617, 1.0) //
    ); //

    private final EvaluationProject project;
    private final String unmentionedModelElementsGoldStandardPath;
    private final String documentation2ArchitectureModelGoldStandardPath;
    private final String filterListPath;
    private final ExpectedResults expectedMissingModelInconsistencyResults;

    InconsistencyDetectionTask(EvaluationProject project, String unmentionedModelElementsGoldStandardPath,
            String documentation2ArchitectureModelGoldStandardPath, String filterListPath, ExpectedResults expectedMissingModelInconsistencyResults) {
        this.project = project;
        this.unmentionedModelElementsGoldStandardPath = unmentionedModelElementsGoldStandardPath;
        this.documentation2ArchitectureModelGoldStandardPath = documentation2ArchitectureModelGoldStandardPath;
        this.filterListPath = filterListPath;
        this.expectedMissingModelInconsistencyResults = expectedMissingModelInconsistencyResults;
    }

    public File getTextFile() {
        return project.getTextFile();
    }

    public File getArchitectureModelFile(ModelFormat modelFormat) {
        return project.getArchitectureModel(modelFormat);
    }

    public File getFilterConfigurationFile() {
        return EvaluationHelper.loadFileFromResources(filterListPath);
    }

    public GoldStandard getGoldstandardForArchitectureModel(ArchitectureComponentModel model) {
        File file = EvaluationHelper.loadFileFromResources(documentation2ArchitectureModelGoldStandardPath);
        return new GoldStandard(file, model);
    }

    public List<String> getUnmentionedModelElementIds() {
        File unmentionedModelElementFile = EvaluationHelper.loadFileFromResources(unmentionedModelElementsGoldStandardPath);
        List<String> unmentionedModelElements;
        try {
            unmentionedModelElements = Files.readAllLines(unmentionedModelElementFile.toPath());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        unmentionedModelElements.removeFirst(); // remove header
        unmentionedModelElements.removeIf(String::isBlank);
        return unmentionedModelElements;
    }

    public ExpectedResults getExpectedMissingModelInconsistencyResults() {
        return expectedMissingModelInconsistencyResults;
    }
}
