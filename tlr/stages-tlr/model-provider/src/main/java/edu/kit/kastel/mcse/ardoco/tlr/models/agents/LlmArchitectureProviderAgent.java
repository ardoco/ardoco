/* Licensed under MIT 2024-2026. */
package edu.kit.kastel.mcse.ardoco.tlr.models.agents;

import java.util.List;

import edu.kit.kastel.mcse.ardoco.core.configuration.NoDefaultConfiguration;
import edu.kit.kastel.mcse.ardoco.core.data.DataRepository;
import edu.kit.kastel.mcse.ardoco.core.pipeline.agent.PipelineAgent;
import edu.kit.kastel.mcse.ardoco.tlr.models.informants.LargeLanguageModel;
import edu.kit.kastel.mcse.ardoco.tlr.models.informants.LlmArchitecturePrompt;
import edu.kit.kastel.mcse.ardoco.tlr.models.informants.LlmArchitectureProviderInformant;

// TODO: Check how to provide the default configuration: its informant creates a chat model of the LargeLanguageModel in the constructor.
@NoDefaultConfiguration
public class LlmArchitectureProviderAgent extends PipelineAgent {

    public LlmArchitectureProviderAgent(DataRepository dataRepository, LargeLanguageModel largeLanguageModel,
            LlmArchitecturePrompt documentationExtractionPrompt, LlmArchitecturePrompt codeExtractionPrompt, LlmArchitecturePrompt.Features codeFeatures,
            LlmArchitecturePrompt aggregationPrompt) {
        super(List.of(new LlmArchitectureProviderInformant(dataRepository, //
                largeLanguageModel, //
                documentationExtractionPrompt, //
                codeExtractionPrompt, //
                codeFeatures, //
                aggregationPrompt //
        )), LlmArchitectureProviderAgent.class.getSimpleName(), dataRepository);
    }
}
