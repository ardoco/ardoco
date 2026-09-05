package edu.kit.kastel.mcse.ardoco.core.api.stage.connectiongenerator.artemis;

import java.util.Map;

import edu.kit.kastel.mcse.ardoco.core.data.PipelineStepData;

public interface ArtemisConnectionStates extends PipelineStepData {

    String ID = "ArtemisConnectionStates";

    ArtemisConnectionState getState(ArtemisTarget target);

    Map<ArtemisTarget, ArtemisConnectionState> getStates();
}
