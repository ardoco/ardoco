/* Licensed under MIT 2021-2024. */
package edu.kit.kastel.mcse.ardoco.tlr.connectiongenerator.agents;

import java.util.List;

import edu.kit.kastel.mcse.ardoco.tlr.connectiongenerator.informants.ReferenceInformant;
import edu.kit.kastel.mcse.ardoco.core.data.DataRepository;
import edu.kit.kastel.mcse.ardoco.core.pipeline.agent.PipelineAgent;

/**
 * The reference solver finds instances mentioned in the text extraction state as names. If it founds some similar names it creates recommendations.
 */
public class ReferenceAgent extends PipelineAgent {

    /**
     * Create the agent.
     *
     * @param dataRepository the {@link DataRepository}
     */
    public ReferenceAgent(DataRepository dataRepository) {
        super(List.of(new ReferenceInformant(dataRepository)), ReferenceAgent.class.getSimpleName(), dataRepository);
    }
}
