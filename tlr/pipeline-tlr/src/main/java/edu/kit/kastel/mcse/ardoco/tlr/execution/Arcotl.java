/* Licensed under MIT 2023-2025. */
package edu.kit.kastel.mcse.ardoco.tlr.execution;

import java.io.File;

import org.eclipse.collections.api.map.sorted.ImmutableSortedMap;

import edu.kit.kastel.mcse.ardoco.core.api.models.Metamodel;
import edu.kit.kastel.mcse.ardoco.core.execution.ArDoCo;
import edu.kit.kastel.mcse.ardoco.core.execution.runner.ArDoCoRunner;
import edu.kit.kastel.mcse.ardoco.tlr.codetraceability.SamCodeTraceabilityLinkRecovery;
import edu.kit.kastel.mcse.ardoco.tlr.models.agents.ArchitectureConfiguration;
import edu.kit.kastel.mcse.ardoco.tlr.models.agents.CodeConfiguration;
import edu.kit.kastel.mcse.ardoco.tlr.models.agents.ModelProviderAgent;

/**
 * ArCoTL (Architecture-Code Trace Links) focuses on linking a given architecture model (SAM) to the source code. It assumes you have a formal model of the
 * system’s components and interfaces, and wants to find the corresponding code. ArCoTL transforms both the architecture model and the code into intermediate
 * representations (e.g. simplified graphs) and then applies various heuristics to match elements These heuristics include standalone rules and dependent rules
 * (which consider relationships) plus filters to refine the links.
 */
public class Arcotl extends ArDoCoRunner {

    public Arcotl(String projectName) {
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

        ModelProviderAgent modelProviderAgent = ModelProviderAgent.getArCoTLModelProviderAgent(dataRepository, additionalConfigs, //
                architectureConfiguration.withMetamodel(Metamodel.ARCHITECTURE_WITH_COMPONENTS_AND_INTERFACES), //
                codeConfiguration.withMetamodel(Metamodel.CODE_WITH_COMPILATION_UNITS) //
        );
        arDoCo.addPipelineStep(modelProviderAgent);
        arDoCo.addPipelineStep(SamCodeTraceabilityLinkRecovery.get(additionalConfigs, dataRepository));
    }
}
