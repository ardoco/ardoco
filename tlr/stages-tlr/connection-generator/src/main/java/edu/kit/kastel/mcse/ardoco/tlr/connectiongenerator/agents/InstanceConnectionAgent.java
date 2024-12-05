/* Licensed under MIT 2021-2023. */
package edu.kit.kastel.mcse.ardoco.tlr.connectiongenerator.agents;

import java.util.List;

import edu.kit.kastel.mcse.ardoco.tlr.connectiongenerator.informants.InstantConnectionInformant;
import edu.kit.kastel.mcse.ardoco.core.data.DataRepository;
import edu.kit.kastel.mcse.ardoco.core.pipeline.agent.PipelineAgent;

/**
 * This connector finds names of model instance in recommended instances.
 */
public class InstanceConnectionAgent extends PipelineAgent {

    /**
     * Create the agent.
     *
     * @param dataRepository the {@link DataRepository}
     */
    public InstanceConnectionAgent(DataRepository dataRepository) {
        super(List.of(new InstantConnectionInformant(dataRepository)), InstanceConnectionAgent.class.getSimpleName(), dataRepository);
    }
}
