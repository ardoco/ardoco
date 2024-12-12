package edu.kit.kastel.mcse.ardoco.secdreqan;

import edu.kit.kastel.mcse.ardoco.core.api.models.ArchitectureModelType;
import edu.kit.kastel.mcse.ardoco.core.api.output.ArDoCoResult;
import edu.kit.kastel.mcse.ardoco.core.common.util.CommonUtilities;
import edu.kit.kastel.mcse.ardoco.core.common.util.DataRepositoryHelper;
import edu.kit.kastel.mcse.ardoco.core.data.DataRepository;
import edu.kit.kastel.mcse.ardoco.core.execution.runner.ArDoCoRunner;
import edu.kit.kastel.mcse.ardoco.tlr.connectiongenerator.ConnectionGenerator;
import edu.kit.kastel.mcse.ardoco.tlr.models.agents.ArCoTLModelProviderAgent;
import edu.kit.kastel.mcse.ardoco.tlr.models.agents.ArchitectureConfiguration;
import edu.kit.kastel.mcse.ardoco.tlr.recommendationgenerator.RecommendationGenerator;
import edu.kit.kastel.mcse.ardoco.tlr.text.providers.TextPreprocessingAgent;
import edu.kit.kastel.mcse.ardoco.tlr.textextraction.TextExtraction;

import java.io.File;
import java.util.List;
import java.util.SortedMap;

public class SecDReqAnRunner extends ArDoCoRunner {

    public SecDReqAnRunner(String projectName) {
        super(projectName);
    }

    public void setUp(String inputTextLocation, String inputArchitectureModelLocation, ArchitectureModelType architectureModelType,
            SortedMap<String, String> additionalConfigs, String outputDirectory) {
        setUp(new File(inputTextLocation), new File(inputArchitectureModelLocation), architectureModelType, additionalConfigs, new File(outputDirectory));
    }

    public void setUp(File inputText, File inputArchitectureModel, ArchitectureModelType architectureModelType, SortedMap<String, String> additionalConfigs,
            File outputDir) {
        defineRequirementsPipeline(inputText, inputArchitectureModel, architectureModelType, additionalConfigs);
        setOutputDirectory(outputDir);
        isSetUp = true;
    }

    public void setUp(File inputArchitectureModel, ArchitectureModelType architectureModelType, SortedMap<String, String> additionalConfigs, File outputDir) {
        defineArchitectureOnlyPipeline(inputArchitectureModel, architectureModelType, additionalConfigs);
        setOutputDirectory(outputDir);
        isSetUp = true;
    }

    public void setUp(File inputRequirement, DataRepository dataRepository, SortedMap<String, String> additionalConfigs, File outputDir) {
        defineRequirementsOnlyPipeline(CommonUtilities.readInputText(inputRequirement), dataRepository, additionalConfigs);

        setOutputDirectory(outputDir);
        isSetUp = true;
    }

    private void defineArchitectureOnlyPipeline(File inputArchitectureModel, ArchitectureModelType architectureModelType,
            SortedMap<String, String> additionalConfigs) {

        var dataRepository = this.getArDoCo().getDataRepository();

        var architectureConfiguration = new ArchitectureConfiguration(inputArchitectureModel, architectureModelType);
        ArCoTLModelProviderAgent arCoTLModelProviderAgent = //
                ArCoTLModelProviderAgent.getArCoTLModelProviderAgent(dataRepository, additionalConfigs, architectureConfiguration, null);
        this.getArDoCo().addPipelineStep(arCoTLModelProviderAgent);
    }

    private void defineRequirementsPipeline(File inputFile, File inputArchitectureModel, ArchitectureModelType architectureModelType,
            SortedMap<String, String> additionalConfigs) {
        var dataRepository = this.getArDoCo().getDataRepository();

        List<Requirement> requirements = RequirementsExtractor.extractRequirmentsFromJSON(inputFile);
        DataRepositoryHelper.putInputText(dataRepository, requirements.get(0).getText());

        var architectureConfiguration = new ArchitectureConfiguration(inputArchitectureModel, architectureModelType);
        ArCoTLModelProviderAgent arCoTLModelProviderAgent = //
                ArCoTLModelProviderAgent.getArCoTLModelProviderAgent(dataRepository, additionalConfigs, architectureConfiguration, null);
        this.getArDoCo().addPipelineStep(arCoTLModelProviderAgent);

        this.getArDoCo().addPipelineStep(TextPreprocessingAgent.get(additionalConfigs, dataRepository));
        this.getArDoCo().addPipelineStep(TextExtraction.get(additionalConfigs, dataRepository));
        this.getArDoCo().addPipelineStep(RecommendationGenerator.get(additionalConfigs, dataRepository));
        this.getArDoCo().addPipelineStep(ConnectionGenerator.get(additionalConfigs, dataRepository));
    }

    private void defineRequirementsOnlyPipeline(String requirement, DataRepository dataRepository, SortedMap<String, String> additionalConfigs) {

        this.getArDoCo().getDataRepository().addAllData(dataRepository);
        var ardocoData = this.getArDoCo().getDataRepository();
        DataRepositoryHelper.putInputText(ardocoData, requirement);

        this.getArDoCo().addPipelineStep(TextPreprocessingAgent.get(additionalConfigs, ardocoData));
        this.getArDoCo().addPipelineStep(TextExtraction.get(additionalConfigs, ardocoData));
        this.getArDoCo().addPipelineStep(RecommendationGenerator.get(additionalConfigs, ardocoData));
        this.getArDoCo().addPipelineStep(ConnectionGenerator.get(additionalConfigs, ardocoData));
    }

}
