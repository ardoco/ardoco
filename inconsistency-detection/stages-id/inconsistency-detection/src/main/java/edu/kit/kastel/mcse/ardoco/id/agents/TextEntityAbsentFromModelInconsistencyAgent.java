/* Licensed under MIT 2021-2026. */
package edu.kit.kastel.mcse.ardoco.id.agents;

import java.util.List;

import edu.kit.kastel.mcse.ardoco.core.data.DataRepository;
import edu.kit.kastel.mcse.ardoco.core.pipeline.agent.PipelineAgent;
import edu.kit.kastel.mcse.ardoco.id.informants.TextEntityAbsentFromModelInconsistencyInformant;

public class TextEntityAbsentFromModelInconsistencyAgent extends PipelineAgent {
    public TextEntityAbsentFromModelInconsistencyAgent(DataRepository dataRepository) {
        super(List.of(new TextEntityAbsentFromModelInconsistencyInformant(dataRepository)), TextEntityAbsentFromModelInconsistencyAgent.class.getSimpleName(),
                dataRepository);
    }
}
