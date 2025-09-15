/* Licensed under MIT 2024-2025. */
package edu.kit.kastel.mcse.ardoco.tlr.models.agents;

import java.util.List;

import edu.kit.kastel.mcse.ardoco.core.data.DataRepository;
import edu.kit.kastel.mcse.ardoco.core.pipeline.agent.PipelineAgent;
import edu.kit.kastel.mcse.ardoco.tlr.models.informants.LargeLanguageModel;
import edu.kit.kastel.mcse.ardoco.tlr.models.informants.LlmArchitecturePrompt;
import edu.kit.kastel.mcse.ardoco.tlr.models.informants.LlmArchitectureProviderInformant;

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
