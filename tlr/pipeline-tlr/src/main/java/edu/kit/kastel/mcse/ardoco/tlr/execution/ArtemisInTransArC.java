/* Licensed under MIT 2025. */
package edu.kit.kastel.mcse.ardoco.tlr.execution;

import java.io.File;

import org.eclipse.collections.api.map.sorted.ImmutableSortedMap;

import edu.kit.kastel.mcse.ardoco.core.api.models.Metamodel;
import edu.kit.kastel.mcse.ardoco.core.common.util.CommonUtilities;
import edu.kit.kastel.mcse.ardoco.core.common.util.DataRepositoryHelper;
import edu.kit.kastel.mcse.ardoco.core.execution.ArDoCo;
import edu.kit.kastel.mcse.ardoco.core.execution.runner.ArDoCoRunner;
import edu.kit.kastel.mcse.ardoco.tlr.codetraceability.SadSamCodeTraceabilityLinkRecovery;
import edu.kit.kastel.mcse.ardoco.tlr.codetraceability.SamCodeTraceabilityLinkRecovery;
import edu.kit.kastel.mcse.ardoco.tlr.connectiongenerator.ner.NerConnectionGenerator;
import edu.kit.kastel.mcse.ardoco.tlr.models.agents.ArchitectureConfiguration;
import edu.kit.kastel.mcse.ardoco.tlr.models.agents.CodeConfiguration;
import edu.kit.kastel.mcse.ardoco.tlr.models.agents.ModelProviderAgent;
import edu.kit.kastel.mcse.ardoco.tlr.models.informants.LargeLanguageModel;
import edu.kit.kastel.mcse.ardoco.tlr.text.providers.SimpleTextPreprocessingAgent;

/**
 * ArtemisInTransArC integrates ArTEMiS into TransArC. TransArC links SAD → SAM → code (SWATTR for SAD–SAM, ArCoTL for SAM–code). This variant replaces the
 * SAD–SAM step with ArTEMiS' NER-based matching while keeping SAM–code and transitive SAD–code recovery unchanged.
 */
public class ArtemisInTransArC extends ArDoCoRunner {

    public ArtemisInTransArC(String projectName) {
        super(projectName);
    }

    public void setUp(File inputText, ArchitectureConfiguration architectureConfiguration, CodeConfiguration codeConfiguration,
            ImmutableSortedMap<String, String> additionalConfigs, File outputDir, LargeLanguageModel llmForNer) {
        if (architectureConfiguration.metamodel() != null || codeConfiguration.metamodel() != null) {
            throw new IllegalArgumentException("Metamodel shall not be set in configurations. The runner defines the metamodels.");
        }
        definePipeline(inputText, architectureConfiguration, codeConfiguration, additionalConfigs, llmForNer);
        setOutputDirectory(outputDir);
        isSetUp = true;
    }

    private void definePipeline(File inputText, ArchitectureConfiguration architectureConfiguration, CodeConfiguration codeConfiguration,
            ImmutableSortedMap<String, String> additionalConfigs, LargeLanguageModel llmForNer) {
        ArDoCo arDoCo = this.getArDoCo();
        var dataRepository = arDoCo.getDataRepository();
        String text = CommonUtilities.readInputText(inputText);
        if (text.isBlank()) {
            throw new IllegalArgumentException("Cannot deal with empty input text. Maybe there was an error reading the file.");
        }
        DataRepositoryHelper.putInputText(dataRepository, text);
        this.getArDoCo().addPipelineStep(SimpleTextPreprocessingAgent.get(additionalConfigs, dataRepository));

        ArchitectureConfiguration architectureConfigurationWithMetamodel = architectureConfiguration.withMetamodel(Metamodel.ARCHITECTURE_WITH_COMPONENTS);
        CodeConfiguration codeConfigurationWithMetamodel = codeConfiguration.withMetamodel(Metamodel.CODE_WITH_COMPILATION_UNITS);
        ModelProviderAgent modelProviderAgent = ModelProviderAgent.getModelProviderAgent(dataRepository, additionalConfigs,
                architectureConfigurationWithMetamodel, codeConfigurationWithMetamodel);
        this.getArDoCo().addPipelineStep(modelProviderAgent);

        NerConnectionGenerator nerConnectionGenerator = NerConnectionGenerator.get(additionalConfigs, dataRepository, llmForNer);
        this.getArDoCo().addPipelineStep(nerConnectionGenerator);

        arDoCo.addPipelineStep(SamCodeTraceabilityLinkRecovery.get(additionalConfigs, dataRepository));
        arDoCo.addPipelineStep(SadSamCodeTraceabilityLinkRecovery.get(additionalConfigs, dataRepository));
    }
}
