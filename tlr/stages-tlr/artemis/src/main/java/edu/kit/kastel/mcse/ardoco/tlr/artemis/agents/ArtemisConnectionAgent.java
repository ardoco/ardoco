/* Licensed under MIT 2026. */
package edu.kit.kastel.mcse.ardoco.tlr.artemis.agents;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.kit.kastel.mcse.ardoco.core.data.DataRepository;
import edu.kit.kastel.mcse.ardoco.core.pipeline.agent.Informant;
import edu.kit.kastel.mcse.ardoco.core.pipeline.agent.PipelineAgent;
import edu.kit.kastel.mcse.ardoco.naer.model.NamedEntityType;
import edu.kit.kastel.mcse.ardoco.tlr.artemis.informants.ClassArtemisConnectionInformant;
import edu.kit.kastel.mcse.ardoco.tlr.artemis.informants.ComponentArtemisConnectionInformant;
import edu.kit.kastel.mcse.ardoco.tlr.artemis.informants.DatafileArtemisConnectionInformant;
import edu.kit.kastel.mcse.ardoco.tlr.artemis.strategies.ArtemisNerStrategy;

public class ArtemisConnectionAgent extends PipelineAgent {
    private static final Logger logger = LoggerFactory.getLogger(ArtemisConnectionAgent.class);

    public ArtemisConnectionAgent(DataRepository dataRepository, ArtemisNerStrategy strategy) {
        super(List.of(createInformant(dataRepository, strategy)), strategy.getId() + "ConnectionAgent", dataRepository);
    }

    private static Informant createInformant(DataRepository dataRepository, ArtemisNerStrategy strategy) {
        return switch (strategy.getNamedEntityType()) {
            case NamedEntityType.COMPONENT -> new ComponentArtemisConnectionInformant(dataRepository, strategy);
            case NamedEntityType.CLASS -> new ClassArtemisConnectionInformant(dataRepository, strategy);
            case NamedEntityType.DATAFILE -> new DatafileArtemisConnectionInformant(dataRepository, strategy);
            default -> {
                logger.error("Unexpected value: {}", strategy.getNamedEntityType());
                throw new IllegalStateException("Unexpected value: " + strategy.getNamedEntityType());
            }
        };
    }
}
