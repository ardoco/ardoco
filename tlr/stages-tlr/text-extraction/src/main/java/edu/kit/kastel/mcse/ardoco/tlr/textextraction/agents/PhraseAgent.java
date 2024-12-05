/* Licensed under MIT 2022-2023. */
package edu.kit.kastel.mcse.ardoco.tlr.textextraction.agents;

import java.util.List;

import edu.kit.kastel.mcse.ardoco.core.api.stage.textextraction.NounMapping;
import edu.kit.kastel.mcse.ardoco.core.data.DataRepository;
import edu.kit.kastel.mcse.ardoco.core.pipeline.agent.PipelineAgent;
import edu.kit.kastel.mcse.ardoco.tlr.textextraction.informants.CompoundAgentInformant;

/**
 * Agent that is responsible for looking at phrases and extracting {@link NounMapping}s from compound nouns etc.
 */
public class PhraseAgent extends PipelineAgent {

    /**
     * Instantiates a new initial text agent.
     *
     * @param dataRepository the {@link DataRepository}
     */
    public PhraseAgent(DataRepository dataRepository) {
        super(List.of(new CompoundAgentInformant(dataRepository)), PhraseAgent.class.getSimpleName(), dataRepository);
    }

}
