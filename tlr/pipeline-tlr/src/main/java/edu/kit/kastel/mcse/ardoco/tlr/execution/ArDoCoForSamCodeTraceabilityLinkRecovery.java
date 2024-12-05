/* Licensed under MIT 2023-2024. */
package edu.kit.kastel.mcse.ardoco.tlr.execution;

import java.io.File;
import java.util.SortedMap;

import edu.kit.kastel.mcse.ardoco.core.api.models.ArchitectureModelType;
import edu.kit.kastel.mcse.ardoco.core.execution.ArDoCo;
import edu.kit.kastel.mcse.ardoco.core.execution.runner.ArDoCoRunner;
import edu.kit.kastel.mcse.ardoco.tlr.codetraceability.SamCodeTraceabilityLinkRecovery;
import edu.kit.kastel.mcse.ardoco.tlr.models.agents.ArCoTLModelProviderAgent;
import edu.kit.kastel.mcse.ardoco.tlr.models.agents.ArchitectureConfiguration;

public class ArDoCoForSamCodeTraceabilityLinkRecovery extends ArDoCoRunner {

    public ArDoCoForSamCodeTraceabilityLinkRecovery(String projectName) {
        super(projectName);
    }

    public void setUp(File inputArchitectureModel, ArchitectureModelType architectureModelType, File inputCode, SortedMap<String, String> additionalConfigs,
            File outputDir) {
        definePipeline(inputArchitectureModel, architectureModelType, inputCode, additionalConfigs);
        setOutputDirectory(outputDir);
        isSetUp = true;
    }

    private void definePipeline(File inputArchitectureModel, ArchitectureModelType architectureModelType, File inputCode,
            SortedMap<String, String> additionalConfigs) {
        ArDoCo arDoCo = this.getArDoCo();
        var dataRepository = arDoCo.getDataRepository();

        var codeConfiguration = ArCoTLModelProviderAgent.getCodeConfiguration(inputCode);
        var architectureConfiguration = new ArchitectureConfiguration(inputArchitectureModel, architectureModelType);

        ArCoTLModelProviderAgent arCoTLModelProviderAgent = ArCoTLModelProviderAgent.getArCoTLModelProviderAgent(dataRepository, additionalConfigs,
                architectureConfiguration, codeConfiguration);
        arDoCo.addPipelineStep(arCoTLModelProviderAgent);
        arDoCo.addPipelineStep(SamCodeTraceabilityLinkRecovery.get(additionalConfigs, dataRepository));
    }
}
