/* Licensed under MIT 2022-2024. */
package edu.kit.kastel.mcse.ardoco.id.tests.eval.baseline;

import java.util.List;

import edu.kit.kastel.mcse.ardoco.core.data.DataRepository;
import edu.kit.kastel.mcse.ardoco.core.pipeline.AbstractExecutionStage;

/**
 * Baseline approach for inconsistency detection
 */
public class InconsistencyBaseline extends AbstractExecutionStage {
    public InconsistencyBaseline(DataRepository dataRepository) {
        super(List.of(new InconsistencyBaselineAgent(dataRepository)), "InconsistencyBaseline", dataRepository);
    }

    @Override
    protected void initializeState() {
        // do nothing
    }
}
