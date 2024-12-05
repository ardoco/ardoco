/* Licensed under MIT 2022-2023. */
package edu.kit.kastel.mcse.ardoco.core.api.stage.inconsistency;

import edu.kit.kastel.mcse.ardoco.core.api.models.Metamodel;
import edu.kit.kastel.mcse.ardoco.core.data.PipelineStepData;

public interface InconsistencyStates extends PipelineStepData {
    String ID = "InconsistencyStates";

    InconsistencyState getInconsistencyState(Metamodel mm);
}
