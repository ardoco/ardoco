/* Licensed under MIT 2023-2024. */
package edu.kit.kastel.mcse.ardoco.id.execution.runner;

import java.io.File;
import java.util.SortedMap;

import edu.kit.kastel.mcse.ardoco.core.api.models.ArchitectureModelType;
import edu.kit.kastel.mcse.ardoco.core.common.util.CommonUtilities;
import edu.kit.kastel.mcse.ardoco.core.common.util.DataRepositoryHelper;
import edu.kit.kastel.mcse.ardoco.core.execution.ArDoCo;
import edu.kit.kastel.mcse.ardoco.core.execution.runner.ArDoCoRunner;
import edu.kit.kastel.mcse.ardoco.id.InconsistencyChecker;
import edu.kit.kastel.mcse.ardoco.tlr.connectiongenerator.ConnectionGenerator;
import edu.kit.kastel.mcse.ardoco.tlr.models.agents.ArCoTLModelProviderAgent;
import edu.kit.kastel.mcse.ardoco.tlr.models.agents.ArchitectureConfiguration;
import edu.kit.kastel.mcse.ardoco.tlr.recommendationgenerator.RecommendationGenerator;
import edu.kit.kastel.mcse.ardoco.tlr.text.providers.TextPreprocessingAgent;
import edu.kit.kastel.mcse.ardoco.tlr.textextraction.TextExtraction;

public class ArDoCoForInconsistencyDetection extends ArDoCoRunner {
    public ArDoCoForInconsistencyDetection(String projectName) {
        super(projectName);
    }

    public void setUp(File inputText, File inputModelArchitecture, ArchitectureModelType inputArchitectureModelType,
            SortedMap<String, String> additionalConfigs, File outputDir) {
        definePipeline(inputText, inputModelArchitecture, inputArchitectureModelType, additionalConfigs);
        setOutputDirectory(outputDir);
        isSetUp = true;
    }

    public void setUp(String inputTextLocation, String inputArchitectureModelLocation, ArchitectureModelType architectureModelType,
            SortedMap<String, String> additionalConfigs, String outputDirectory) {
        setUp(new File(inputTextLocation), new File(inputArchitectureModelLocation), architectureModelType, additionalConfigs, new File(outputDirectory));
    }

    /**
     * This method sets up the pipeline for ArDoCo.
     *
     * @param inputText              The input text file
     * @param inputArchitectureModel the input architecture file
     * @param architectureModelType  the type of the architecture (e.g., PCM, UML)
     * @param additionalConfigs      the additional configs
     */
    private void definePipeline(File inputText, File inputArchitectureModel, ArchitectureModelType architectureModelType,
            SortedMap<String, String> additionalConfigs) {
        ArDoCo arDoCo = getArDoCo();
        var dataRepository = arDoCo.getDataRepository();
        var text = CommonUtilities.readInputText(inputText);
        if (text.isBlank()) {
            throw new IllegalArgumentException("Cannot deal with empty input text. Maybe there was an error reading the file.");
        }
        DataRepositoryHelper.putInputText(dataRepository, text);

        arDoCo.addPipelineStep(TextPreprocessingAgent.get(additionalConfigs, dataRepository));
        var architectureConfiguration = new ArchitectureConfiguration(inputArchitectureModel, architectureModelType);
        ArCoTLModelProviderAgent arCoTLModelProviderAgent = ArCoTLModelProviderAgent.getArCoTLModelProviderAgent(dataRepository, additionalConfigs,
                architectureConfiguration, null);
        arDoCo.addPipelineStep(arCoTLModelProviderAgent);
        arDoCo.addPipelineStep(TextExtraction.get(additionalConfigs, dataRepository));
        arDoCo.addPipelineStep(RecommendationGenerator.get(additionalConfigs, dataRepository));
        arDoCo.addPipelineStep(ConnectionGenerator.get(additionalConfigs, dataRepository));
        arDoCo.addPipelineStep(InconsistencyChecker.get(additionalConfigs, dataRepository));
    }
}
