/* Licensed under MIT 2026. */
package edu.kit.kastel.mcse.ardoco.core.api.stage.inconsistency;

import org.eclipse.collections.api.map.ImmutableMap;

import edu.kit.kastel.mcse.ardoco.core.api.stage.connectiongenerator.artemis.ArtemisTarget;
import edu.kit.kastel.mcse.ardoco.core.architecture.Deterministic;
import edu.kit.kastel.mcse.ardoco.core.data.PipelineStepData;

@Deterministic
public interface ArtemisInconsistencyStates extends PipelineStepData {

    String ID = "ArtemisInconsistencyStates";

    ArtemisInconsistencyState getState(ArtemisTarget target);

    ImmutableMap<ArtemisTarget, ArtemisInconsistencyState> getStates();
}
