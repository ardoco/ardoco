package edu.kit.kastel.mcse.ardoco.secdreqan;

import edu.kit.kastel.mcse.ardoco.core.execution.ConfigurationHelper;
import edu.kit.kastel.mcse.ardoco.core.tests.eval.ProjectHelper;
import edu.kit.kastel.mcse.ardoco.core.tests.eval.results.ExpectedResults;

import org.eclipse.collections.impl.factory.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.SortedMap;

public enum MultiGoldStandardProject{


    EVEREST(//
            "EV", //
                    "/benchmark/everest/model_2016/pcm/ev.repository", //
                    "/benchmark/everest/requirements/", //
                    "/configurations/ev/filterlists_all.txt", // options: filterlists_none.txt, filterlists_onlyCommon.txt, filterlists_all.txt
                    "/benchmark/everest/goldstandards/", //
                    new ExpectedResults(.0, .0, .0, .0, .0, .0) //
    ); //


    private static final Logger logger = LoggerFactory.getLogger(MultiGoldStandardProject.class);
    private final String alias;
    private final String model;
    private final String textFileDirectory;
    private final String configurationsFile;
    private final String traceabilityLinkRecoveryGoldstandardDirectory;
    private final ExpectedResults expectedTraceLinkResults;

    MultiGoldStandardProject(String alias, String model, String textFileDirectory, String configurationsFile, String goldStandardDirectory,
            ExpectedResults expectedTraceLinkResults) {
        this.alias = alias;
        this.model = model;
        this.textFileDirectory = textFileDirectory;
        this.configurationsFile = configurationsFile;
        this.traceabilityLinkRecoveryGoldstandardDirectory = goldStandardDirectory;
        this.expectedTraceLinkResults = expectedTraceLinkResults;
    }

    public String getAlias() {
        return alias;
    }

    public File getModelFile() {
        return ProjectHelper.loadFileFromResources(model);
    }

    public List<File> getRequirements(){return ProjectHelper.loadFilesFromFolder(textFileDirectory);}

    public SortedMap<String, String> getAdditionalConfigurations() {
        return ConfigurationHelper.loadAdditionalConfigs(ProjectHelper.loadFileFromResources(configurationsFile));
    }

    public File getAdditionalConfigurationsFile() {
        return ProjectHelper.loadFileFromResources(this.configurationsFile);
    }

    public List<File> getTlRGoldStandardFiles(){
        return ProjectHelper.loadFilesFromFolder(traceabilityLinkRecoveryGoldstandardDirectory);
    }

    public ExpectedResults getExpectedTraceLinkResults() {
        return expectedTraceLinkResults;
    }

    public List<String> getRequirementIdentifier() {
        File folder = new File(this.textFileDirectory);
        return Arrays.stream(Objects.requireNonNull(folder.listFiles())).map(File::getName).toList();
    }

    public List<RequirementTLREvaluationObject> getTlRGoldStandards() {

        List<RequirementTLREvaluationObject> requirementTLRGoldstandards = new ArrayList<>();
        List<File> tlrGoldStandardFiles = ProjectHelper.loadFilesFromFolder(this.traceabilityLinkRecoveryGoldstandardDirectory);

        for(File goldstandardFile : tlrGoldStandardFiles){

            var path = Paths.get(goldstandardFile.toURI());
            List<String> goldLinks = Lists.mutable.empty();
            try {
                goldLinks = Files.readAllLines(path);
            } catch (IOException e) {
                logger.error(e.getMessage(), e);
            }
            goldLinks.removeFirst();
            goldLinks.removeIf(String::isBlank);
            requirementTLRGoldstandards.add(new RequirementTLREvaluationObject(goldstandardFile.getName(), Lists.immutable.ofAll(goldLinks)));
        }

        return requirementTLRGoldstandards;

    }
}
