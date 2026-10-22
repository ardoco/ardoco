/* Licensed under MIT 2026. */
package edu.kit.kastel.mcse.ardoco.id.agents;

import java.util.List;

import edu.kit.kastel.mcse.ardoco.core.data.DataRepository;
import edu.kit.kastel.mcse.ardoco.core.pipeline.agent.PipelineAgent;
import edu.kit.kastel.mcse.ardoco.id.informants.ArtemisTeamInconsistencyInformant;
import edu.kit.kastel.mcse.ardoco.tlr.artemis.strategies.ArtemisNerStrategy;

/**
 * Pipeline agent for detecting text entities that are absent from the model for a specific Artemis NER strategy.
 * <p>
 * The agent delegates the actual inconsistency detection to an {@link ArtemisTeamInconsistencyInformant}.
 * </p>
 */
public class ArtemisTeamInconsistencyAgent extends PipelineAgent {

    public ArtemisTeamInconsistencyAgent(DataRepository dataRepository, ArtemisNerStrategy strategy) {
        super(List.of(new ArtemisTeamInconsistencyInformant(dataRepository, strategy)), strategy.getId() + "TeamInconsistencyAgent", dataRepository);
    }
}
