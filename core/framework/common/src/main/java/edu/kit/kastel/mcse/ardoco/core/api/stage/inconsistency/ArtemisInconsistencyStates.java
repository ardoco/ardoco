package edu.kit.kastel.mcse.ardoco.core.api.stage.inconsistency;

import java.util.Map;

import edu.kit.kastel.mcse.ardoco.core.api.stage.connectiongenerator.artemis.ArtemisTarget;
import edu.kit.kastel.mcse.ardoco.core.data.PipelineStepData;

public interface ArtemisInconsistencyStates extends PipelineStepData {

    String ID = "ArtemisInconsistencyStates";

    ArtemisInconsistencyState getState(ArtemisTarget target);

    Map<ArtemisTarget, ArtemisInconsistencyState> getStates();
}
