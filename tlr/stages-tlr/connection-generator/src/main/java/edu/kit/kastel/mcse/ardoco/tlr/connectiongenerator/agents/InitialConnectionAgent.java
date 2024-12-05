/* Licensed under MIT 2021-2023. */
package edu.kit.kastel.mcse.ardoco.tlr.connectiongenerator.agents;

import java.util.List;

import edu.kit.kastel.mcse.ardoco.tlr.connectiongenerator.informants.ExtractionDependentOccurrenceInformant;
import edu.kit.kastel.mcse.ardoco.tlr.connectiongenerator.informants.NameTypeConnectionInformant;
import edu.kit.kastel.mcse.ardoco.core.data.DataRepository;
import edu.kit.kastel.mcse.ardoco.core.pipeline.agent.PipelineAgent;

/**
 * The agent that executes the extractors of this stage.
 */
public class InitialConnectionAgent extends PipelineAgent {

    /**
     * Create the agent.
     *
     * @param dataRepository the {@link DataRepository}
     */
    public InitialConnectionAgent(DataRepository dataRepository) {
        super(List.of(new NameTypeConnectionInformant(dataRepository), new ExtractionDependentOccurrenceInformant(dataRepository)), InitialConnectionAgent.class
                .getSimpleName(), dataRepository);
    }
}
