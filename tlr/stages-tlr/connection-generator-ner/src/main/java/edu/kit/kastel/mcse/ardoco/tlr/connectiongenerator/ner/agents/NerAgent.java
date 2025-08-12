/* Licensed under MIT 2025. */
package edu.kit.kastel.mcse.ardoco.tlr.connectiongenerator.ner.agents;

import java.util.List;

import edu.kit.kastel.mcse.ardoco.core.data.DataRepository;
import edu.kit.kastel.mcse.ardoco.core.pipeline.agent.PipelineAgent;
import edu.kit.kastel.mcse.ardoco.tlr.connectiongenerator.ner.informants.NerInformant;
import edu.kit.kastel.mcse.ardoco.tlr.models.informants.LargeLanguageModel;

public class NerAgent extends PipelineAgent {
    /**
     * Creates a new NerAgent. Runs informants sequentially on the data repository.
     *
     * @param dataRepository the data repository
     * @param llm            the large language model to use for named entity recognition
     */
    public NerAgent(DataRepository dataRepository, LargeLanguageModel llm) {
        super(List.of(new NerInformant(dataRepository, llm)), NerAgent.class.getSimpleName(), dataRepository);
    }
}
