package edu.kit.kastel.mcse.ardoco.tlr.connectiongenerator.ner.agents;

import java.util.List;

import edu.kit.kastel.mcse.ardoco.core.data.DataRepository;
import edu.kit.kastel.mcse.ardoco.core.pipeline.agent.PipelineAgent;
import edu.kit.kastel.mcse.ardoco.tlr.connectiongenerator.ner.informants.NerInformant;

public class NerAgent extends PipelineAgent {
    /**
     * Creates a new NerAgent. Runs informants sequentially on the data repository.
     *
     * @param dataRepository the data repository
     */
    protected NerAgent(DataRepository dataRepository) {
        super(List.of(new NerInformant(dataRepository)), NerAgent.class.getSimpleName(), dataRepository);
    }
}
