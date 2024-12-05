/* Licensed under MIT 2021-2023. */
package edu.kit.kastel.mcse.ardoco.tlr.recommendationgenerator.agents;

import java.util.List;

import edu.kit.kastel.mcse.ardoco.core.data.DataRepository;
import edu.kit.kastel.mcse.ardoco.core.pipeline.agent.PipelineAgent;
import edu.kit.kastel.mcse.ardoco.tlr.recommendationgenerator.informants.CompoundRecommendationInformant;

public final class PhraseRecommendationAgent extends PipelineAgent {

    public PhraseRecommendationAgent(DataRepository dataRepository) {
        super(List.of(new CompoundRecommendationInformant(dataRepository)), PhraseRecommendationAgent.class.getSimpleName(), dataRepository);
    }
}
