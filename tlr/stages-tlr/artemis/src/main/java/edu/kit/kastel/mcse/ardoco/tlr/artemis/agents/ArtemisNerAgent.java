package edu.kit.kastel.mcse.ardoco.tlr.artemis.agents;

import java.util.List;

import edu.kit.kastel.mcse.ardoco.core.data.DataRepository;
import edu.kit.kastel.mcse.ardoco.core.pipeline.agent.PipelineAgent;
import edu.kit.kastel.mcse.ardoco.tlr.artemis.informants.ArtemisNerInformant;
import edu.kit.kastel.mcse.ardoco.tlr.artemis.strategies.ArtemisNerStrategy;
import edu.kit.kastel.mcse.ardoco.tlr.models.informants.LargeLanguageModel;

public class ArtemisNerAgent extends PipelineAgent {

    public ArtemisNerAgent(DataRepository dataRepository, LargeLanguageModel llm, ArtemisNerStrategy strategy) {
        super(List.of(new ArtemisNerInformant(dataRepository, llm, strategy)), strategy.getId() + "NerAgent", dataRepository);
    }
}
