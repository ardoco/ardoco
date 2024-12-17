/* Licensed under MIT 2024. */
package edu.kit.kastel.mcse.ardoco.tlr.models.agents;

import java.util.List;

import edu.kit.kastel.mcse.ardoco.core.data.DataRepository;
import edu.kit.kastel.mcse.ardoco.core.pipeline.agent.PipelineAgent;
import edu.kit.kastel.mcse.ardoco.tlr.models.informants.LLMArchitecturePrompt;
import edu.kit.kastel.mcse.ardoco.tlr.models.informants.LLMArchitectureProviderInformant;
import edu.kit.kastel.mcse.ardoco.tlr.models.informants.LargeLanguageModel;

public class LLMArchitectureProviderAgent extends PipelineAgent {

    public LLMArchitectureProviderAgent(DataRepository dataRepository, LargeLanguageModel largeLanguageModel,
            LLMArchitecturePrompt documentationExtractionPrompt, LLMArchitecturePrompt codeExtractionPrompt, LLMArchitecturePrompt.Features codeFeatures,
            LLMArchitecturePrompt aggregationPrompt) {
        super(List.of(new LLMArchitectureProviderInformant(dataRepository, largeLanguageModel, documentationExtractionPrompt, codeExtractionPrompt,
                codeFeatures, aggregationPrompt)), LLMArchitectureProviderAgent.class.getSimpleName(), dataRepository);
    }
}
