package edu.kit.kastel.mcse.ardoco.id.agents;

import java.util.List;

import edu.kit.kastel.mcse.ardoco.core.data.DataRepository;
import edu.kit.kastel.mcse.ardoco.core.pipeline.agent.PipelineAgent;
import edu.kit.kastel.mcse.ardoco.id.informants.ArtemisMeatInconsistencyInformant;
import edu.kit.kastel.mcse.ardoco.tlr.artemis.strategies.ArtemisNerStrategy;

public class ArtemisMeatInconsistencyAgent extends PipelineAgent {

    public ArtemisMeatInconsistencyAgent(DataRepository dataRepository, ArtemisNerStrategy strategy) {
        super(List.of(new ArtemisMeatInconsistencyInformant(dataRepository, strategy)), strategy.getId() + "MeatInconsistencyAgent", dataRepository);
    }
}
