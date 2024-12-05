/* Licensed under MIT 2021-2023. */
package edu.kit.kastel.mcse.ardoco.tlr.recommendationgenerator.agents;

import java.util.List;

import edu.kit.kastel.mcse.ardoco.core.data.DataRepository;
import edu.kit.kastel.mcse.ardoco.core.pipeline.agent.PipelineAgent;
import edu.kit.kastel.mcse.ardoco.tlr.recommendationgenerator.informants.NameTypeInformant;

/**
 * The Class InitialRecommendationAgent runs all extractors of this stage.
 */
public class InitialRecommendationAgent extends PipelineAgent {
    public InitialRecommendationAgent(DataRepository dataRepository) {
        super(List.of(new NameTypeInformant(dataRepository)), InitialRecommendationAgent.class.getSimpleName(), dataRepository);
    }
}
