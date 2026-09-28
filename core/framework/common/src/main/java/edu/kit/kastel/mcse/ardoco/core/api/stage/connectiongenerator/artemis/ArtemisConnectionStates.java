package edu.kit.kastel.mcse.ardoco.core.api.stage.connectiongenerator.artemis;

import org.eclipse.collections.api.map.ImmutableMap;

import edu.kit.kastel.mcse.ardoco.core.architecture.Deterministic;
import edu.kit.kastel.mcse.ardoco.core.data.PipelineStepData;

@Deterministic
public interface ArtemisConnectionStates extends PipelineStepData {

    String ID = "ArtemisConnectionStates";

    ArtemisConnectionState getState(ArtemisTarget target);

    ImmutableMap<ArtemisTarget, ArtemisConnectionState> getStates();
}
