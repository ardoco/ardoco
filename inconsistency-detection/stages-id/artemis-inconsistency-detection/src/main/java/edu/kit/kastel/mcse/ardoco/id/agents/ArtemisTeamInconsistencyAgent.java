package edu.kit.kastel.mcse.ardoco.id.agents;

import java.util.List;

import edu.kit.kastel.mcse.ardoco.core.data.DataRepository;
import edu.kit.kastel.mcse.ardoco.core.pipeline.agent.PipelineAgent;
import edu.kit.kastel.mcse.ardoco.id.informants.ArtemisTeamInconsistencyInformant;
import edu.kit.kastel.mcse.ardoco.tlr.artemis.strategies.ArtemisNerStrategy;

public class ArtemisTeamInconsistencyAgent extends PipelineAgent {

    public ArtemisTeamInconsistencyAgent(DataRepository dataRepository, ArtemisNerStrategy strategy) {
        super(List.of(new ArtemisTeamInconsistencyInformant(dataRepository, strategy)), strategy.getId() + "TeamInconsistencyAgent", dataRepository);
    }
}
