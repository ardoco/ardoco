/* Licensed under MIT 2026. */
package edu.kit.kastel.mcse.ardoco.id.agents;

import java.util.List;

import edu.kit.kastel.mcse.ardoco.core.configuration.NoDefaultConfiguration;
import edu.kit.kastel.mcse.ardoco.core.data.DataRepository;
import edu.kit.kastel.mcse.ardoco.core.pipeline.agent.PipelineAgent;
import edu.kit.kastel.mcse.ardoco.id.informants.ArtemisMeatInconsistencyInformant;
import edu.kit.kastel.mcse.ardoco.tlr.artemis.strategies.ArtemisNerStrategy;

/**
 * Pipeline agent for detecting model entities that are absent from the text for a specific Artemis NER strategy.
 * <p>
 * The agent delegates the actual inconsistency detection to an {@link ArtemisMeatInconsistencyInformant}.
 * </p>
 */
// TODO: Check how to provide the default configuration: its informants depend on the ArtemisNerStrategy passed to the constructor.
@NoDefaultConfiguration
public class ArtemisMeatInconsistencyAgent extends PipelineAgent {

    public ArtemisMeatInconsistencyAgent(DataRepository dataRepository, ArtemisNerStrategy strategy) {
        super(List.of(new ArtemisMeatInconsistencyInformant(dataRepository, strategy)), strategy.getId() + "MeatInconsistencyAgent", dataRepository);
    }
}
