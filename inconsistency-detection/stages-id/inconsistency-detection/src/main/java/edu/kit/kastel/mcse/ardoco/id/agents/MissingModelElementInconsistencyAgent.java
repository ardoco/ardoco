/* Licensed under MIT 2021-2024. */
package edu.kit.kastel.mcse.ardoco.id.agents;

import java.util.List;

import edu.kit.kastel.mcse.ardoco.core.data.DataRepository;
import edu.kit.kastel.mcse.ardoco.core.pipeline.agent.PipelineAgent;
import edu.kit.kastel.mcse.ardoco.id.informants.MissingModelElementInconsistencyInformant;

public class MissingModelElementInconsistencyAgent extends PipelineAgent {
    public MissingModelElementInconsistencyAgent(DataRepository dataRepository) {
        super(List.of(new MissingModelElementInconsistencyInformant(dataRepository)), MissingModelElementInconsistencyAgent.class.getSimpleName(),
                dataRepository);
    }
}
