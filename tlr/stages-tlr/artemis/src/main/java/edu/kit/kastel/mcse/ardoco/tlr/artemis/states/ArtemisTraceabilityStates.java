package edu.kit.kastel.mcse.ardoco.tlr.artemis.states;

import java.util.Map;

import edu.kit.kastel.mcse.ardoco.core.data.PipelineStepData;

public interface ArtemisTraceabilityStates extends PipelineStepData {

    String ID = "ArtemisTraceabilityStates";

    ArtemisTraceabilityState getState(ArtemisTarget target);

    Map<ArtemisTarget, ArtemisTraceabilityState> getStates();
}
