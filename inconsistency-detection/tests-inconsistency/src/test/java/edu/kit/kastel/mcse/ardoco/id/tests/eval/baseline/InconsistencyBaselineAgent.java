/* Licensed under MIT 2022-2024. */
package edu.kit.kastel.mcse.ardoco.id.tests.eval.baseline;

import java.util.List;

import edu.kit.kastel.mcse.ardoco.core.data.DataRepository;
import edu.kit.kastel.mcse.ardoco.core.pipeline.agent.PipelineAgent;

/**
 * Agent for {@link InconsistencyBaseline}
 */
public class InconsistencyBaselineAgent extends PipelineAgent {
    protected InconsistencyBaselineAgent(DataRepository dataRepository) {
        super(List.of(new InconsistencyBaselineInformant(dataRepository)), InconsistencyBaselineAgent.class.getSimpleName(), dataRepository);
    }
}
