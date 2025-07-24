/* Licensed under MIT 2023-2025. */
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
import edu.kit.kastel.mcse.ardoco.tlr.connectiongenerator.ConnectionGenerator;
import edu.kit.kastel.mcse.ardoco.tlr.models.agents.ArCoTLModelProviderAgent;
import edu.kit.kastel.mcse.ardoco.tlr.models.agents.CodeConfiguration;
import edu.kit.kastel.mcse.ardoco.tlr.models.agents.LlmArchitectureProviderAgent;
import edu.kit.kastel.mcse.ardoco.tlr.models.informants.LargeLanguageModel;
import edu.kit.kastel.mcse.ardoco.tlr.models.informants.LlmArchitecturePrompt;
import edu.kit.kastel.mcse.ardoco.tlr.recommendationgenerator.RecommendationGenerator;
import edu.kit.kastel.mcse.ardoco.tlr.text.providers.TextPreprocessingAgent;
import edu.kit.kastel.mcse.ardoco.tlr.textextraction.TextExtraction;

public class TransarcAi extends ArDoCoRunner {

    public TransarcAi(String projectName) {
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

        arDoCo.addPipelineStep(TextPreprocessingAgent.get(additionalConfigs, dataRepository));

        ArCoTLModelProviderAgent arCoTLModelProviderAgent = ArCoTLModelProviderAgent.getArCoTLModelProviderAgent(dataRepository, additionalConfigs, null,
                codeConfiguration.withMetamodel(Metamodel.CODE_WITH_COMPILATION_UNITS_AND_PACKAGES));
        arDoCo.addPipelineStep(arCoTLModelProviderAgent);

        LlmArchitectureProviderAgent llmArchitectureProviderAgent = new LlmArchitectureProviderAgent(dataRepository, largeLanguageModel,
                documentationExtractionPrompt, codeExtractionPrompt, codeFeatures, aggregationPrompt);
        arDoCo.addPipelineStep(llmArchitectureProviderAgent);

        arDoCo.addPipelineStep(TextExtraction.get(additionalConfigs, dataRepository));
        arDoCo.addPipelineStep(RecommendationGenerator.get(additionalConfigs, dataRepository));
        arDoCo.addPipelineStep(ConnectionGenerator.get(additionalConfigs, dataRepository));

        arDoCo.addPipelineStep(SamCodeTraceabilityLinkRecovery.get(additionalConfigs, dataRepository));

        arDoCo.addPipelineStep(SadSamCodeTraceabilityLinkRecovery.get(additionalConfigs, dataRepository));
    }
}
