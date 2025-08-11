/* Licensed under MIT 2023-2025. */
package edu.kit.kastel.mcse.ardoco.tlr.execution;

import java.io.File;

import org.eclipse.collections.api.map.sorted.ImmutableSortedMap;

import edu.kit.kastel.mcse.ardoco.core.api.models.Metamodel;
import edu.kit.kastel.mcse.ardoco.core.common.util.CommonUtilities;
import edu.kit.kastel.mcse.ardoco.core.common.util.DataRepositoryHelper;
import edu.kit.kastel.mcse.ardoco.core.common.util.Environment;
import edu.kit.kastel.mcse.ardoco.core.execution.ArDoCo;
import edu.kit.kastel.mcse.ardoco.core.execution.runner.ArDoCoRunner;
import edu.kit.kastel.mcse.ardoco.tlr.codetraceability.SadSamCodeTraceabilityLinkRecovery;
import edu.kit.kastel.mcse.ardoco.tlr.codetraceability.SamCodeTraceabilityLinkRecovery;
import edu.kit.kastel.mcse.ardoco.tlr.connectiongenerator.ner.NerConnectionGenerator;
import edu.kit.kastel.mcse.ardoco.tlr.connectiongenerator.ner.llm.LlmSettings;
import edu.kit.kastel.mcse.ardoco.tlr.connectiongenerator.ner.llm.ModelProvider;
import edu.kit.kastel.mcse.ardoco.tlr.models.agents.CodeConfiguration;
import edu.kit.kastel.mcse.ardoco.tlr.models.agents.LlmArchitectureProviderAgent;
import edu.kit.kastel.mcse.ardoco.tlr.models.agents.ModelProviderAgent;
import edu.kit.kastel.mcse.ardoco.tlr.models.informants.LargeLanguageModel;
import edu.kit.kastel.mcse.ardoco.tlr.models.informants.LlmArchitecturePrompt;
import edu.kit.kastel.mcse.ardoco.tlr.text.providers.SimpleTextPreprocessingAgent;

/**
 * ExArch extends the TransArC idea by using an LLM to generate a simple architecture mode (SAM). In this approach, instead of requiring a hand-made SAM, a
 * large language model (such as GPT-4) is prompted to extract or invent the main component names from the SAD (and optionally from code). These names serve as
 * a minimal architecture model (i.e. a list of components). Then, as in TransArC, these LLM-derived components are matched to code. The goal is to bridge the
 * SAD–code gap without manual modeling.
 */
public class ArtemisInExArch extends ArDoCoRunner {

    public ArtemisInExArch(String projectName) {
        super(projectName);
    }

    public void setUp(File inputText, CodeConfiguration codeConfiguration, ImmutableSortedMap<String, String> additionalConfigs, File outputDir,
            LargeLanguageModel largeLanguageModel, LlmArchitecturePrompt documentationExtractionPrompt, LlmArchitecturePrompt codeExtractionPrompt,
            LlmArchitecturePrompt.Features codeFeatures, LlmArchitecturePrompt aggregationPrompt) {
        if (codeConfiguration.metamodel() != null) {
            throw new IllegalArgumentException("Metamodel shall not be set in configurations. The runner defines the metamodels.");
        }
        definePipeline(inputText, codeConfiguration, additionalConfigs, largeLanguageModel, documentationExtractionPrompt, codeExtractionPrompt, codeFeatures,
                aggregationPrompt);
        setOutputDirectory(outputDir);
        isSetUp = true;
    }

    private void definePipeline(File inputText, CodeConfiguration codeConfiguration, ImmutableSortedMap<String, String> additionalConfigs,
            LargeLanguageModel largeLanguageModel, LlmArchitecturePrompt documentationExtractionPrompt, LlmArchitecturePrompt codeExtractionPrompt,
            LlmArchitecturePrompt.Features codeFeatures, LlmArchitecturePrompt aggregationPrompt) {
        ArDoCo arDoCo = this.getArDoCo();
        var dataRepository = arDoCo.getDataRepository();

        var text = CommonUtilities.readInputText(inputText);
        if (text.isBlank()) {
            throw new IllegalArgumentException("Cannot deal with empty input text. Maybe there was an error reading the file.");
        }
        DataRepositoryHelper.putInputText(dataRepository, text);
        arDoCo.addPipelineStep(SimpleTextPreprocessingAgent.get(additionalConfigs, dataRepository));

        ModelProviderAgent arCoTLModelProviderAgent = ModelProviderAgent.getModelProviderAgent(dataRepository, additionalConfigs, null, codeConfiguration
                .withMetamodel(Metamodel.CODE_WITH_COMPILATION_UNITS));
        arDoCo.addPipelineStep(arCoTLModelProviderAgent);

        LlmArchitectureProviderAgent llmArchitectureProviderAgent = new LlmArchitectureProviderAgent(dataRepository, largeLanguageModel,
                documentationExtractionPrompt, codeExtractionPrompt, codeFeatures, aggregationPrompt);
        arDoCo.addPipelineStep(llmArchitectureProviderAgent);

        NerConnectionGenerator nerConnectionGenerator = NerConnectionGenerator.get(additionalConfigs, dataRepository);
        LlmSettings llmSettings = getLlmSettings();
        nerConnectionGenerator.setLlmSettings(llmSettings);
        this.getArDoCo().addPipelineStep(nerConnectionGenerator);

        arDoCo.addPipelineStep(SamCodeTraceabilityLinkRecovery.get(additionalConfigs, dataRepository));

        arDoCo.addPipelineStep(SadSamCodeTraceabilityLinkRecovery.get(additionalConfigs, dataRepository));
    }

    private static LlmSettings getLlmSettings() {
        String modelName = Environment.getEnv("MODEL_NAME_NER");
        if (modelName == null)
            modelName = "gpt-4.1";
        double temperature = 0.0;
        return new LlmSettings.Builder().modelProvider(ModelProvider.OPEN_AI).modelName(modelName).temperature(temperature).timeout(120).build();
    }
}
