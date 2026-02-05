/* Licensed under MIT 2022-2026. */
package edu.kit.kastel.mcse.ardoco.id.agents;

import java.util.List;

import edu.kit.kastel.mcse.ardoco.core.data.DataRepository;
import edu.kit.kastel.mcse.ardoco.core.pipeline.agent.PipelineAgent;
import edu.kit.kastel.mcse.ardoco.id.informants.ModelEntityAbsentFromTextInconsistencyInformant;

/**
 * This agent analyses the model to find elements within the model that are not documented in the text. For this, it uses the
 * {@link ModelEntityAbsentFromTextInconsistencyInformant}. See it for more information about configuration options.
 */
public class ModelEntityAbsentFromTextInconsistencyAgent extends PipelineAgent {

    public ModelEntityAbsentFromTextInconsistencyAgent(DataRepository dataRepository) {
        super(List.of(new ModelEntityAbsentFromTextInconsistencyInformant(dataRepository)), ModelEntityAbsentFromTextInconsistencyAgent.class.getSimpleName(),
                dataRepository);
    }
}
