/* Licensed under MIT 2023-2025. */
package edu.kit.kastel.mcse.ardoco.tlr.execution;

import java.io.File;

import org.eclipse.collections.api.map.sorted.ImmutableSortedMap;

import edu.kit.kastel.mcse.ardoco.core.api.models.Metamodel;
import edu.kit.kastel.mcse.ardoco.core.execution.ArDoCo;
import edu.kit.kastel.mcse.ardoco.core.execution.runner.ArDoCoRunner;
import edu.kit.kastel.mcse.ardoco.tlr.codetraceability.SamCodeTraceabilityLinkRecovery;
import edu.kit.kastel.mcse.ardoco.tlr.models.agents.ArCoTLModelProviderAgent;
import edu.kit.kastel.mcse.ardoco.tlr.models.agents.ArchitectureConfiguration;
import edu.kit.kastel.mcse.ardoco.tlr.models.agents.CodeConfiguration;

public class ArCoTL extends ArDoCoRunner {

    public ArCoTL(String projectName) {
        super(projectName);
    }

    public void setUp(ArchitectureConfiguration architectureConfiguration, CodeConfiguration codeConfiguration,
            ImmutableSortedMap<String, String> additionalConfigs, File outputDir) {
        if (architectureConfiguration.metamodel() != null || codeConfiguration.metamodel() != null) {
            throw new IllegalArgumentException("Metamodel shall not be set in configurations. The runner define the metamodels.");
        }

        definePipeline(architectureConfiguration, codeConfiguration, additionalConfigs);
        setOutputDirectory(outputDir);
        isSetUp = true;
    }

    private void definePipeline(ArchitectureConfiguration architectureConfiguration, CodeConfiguration codeConfiguration,
            ImmutableSortedMap<String, String> additionalConfigs) {
        ArDoCo arDoCo = this.getArDoCo();
        var dataRepository = arDoCo.getDataRepository();

        ArCoTLModelProviderAgent arCoTLModelProviderAgent = ArCoTLModelProviderAgent.getArCoTLModelProviderAgent(dataRepository, additionalConfigs, //
                architectureConfiguration.withMetamodel(Metamodel.ARCHITECTURE_WITH_COMPONENTS_AND_INTERFACES), //
                codeConfiguration.withMetamodel(Metamodel.CODE_WITH_COMPILATION_UNITS) //
        );
        arDoCo.addPipelineStep(arCoTLModelProviderAgent);
        arDoCo.addPipelineStep(SamCodeTraceabilityLinkRecovery.get(additionalConfigs, dataRepository));
    }
}
