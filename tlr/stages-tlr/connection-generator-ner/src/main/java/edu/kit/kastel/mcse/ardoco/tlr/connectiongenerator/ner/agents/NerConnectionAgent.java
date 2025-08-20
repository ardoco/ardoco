/* Licensed under MIT 2025. */
package edu.kit.kastel.mcse.ardoco.tlr.connectiongenerator.ner.agents;

import java.util.List;

import edu.kit.kastel.mcse.ardoco.core.data.DataRepository;
import edu.kit.kastel.mcse.ardoco.core.pipeline.agent.PipelineAgent;
import edu.kit.kastel.mcse.ardoco.tlr.connectiongenerator.ner.informants.NerConnectionInformant;

public class NerConnectionAgent extends PipelineAgent {

    /**
     * Creates a new NerConnectionAgent. Runs informants sequentially on the data repository.
     *
     * @param dataRepository the data repository
     */
    public NerConnectionAgent(DataRepository dataRepository) {
        super(List.of(new NerConnectionInformant(dataRepository)), NerConnectionAgent.class.getSimpleName(), dataRepository);
    }
}
