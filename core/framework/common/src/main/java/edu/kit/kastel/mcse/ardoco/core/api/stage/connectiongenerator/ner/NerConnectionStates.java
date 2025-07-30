package edu.kit.kastel.mcse.ardoco.core.api.stage.connectiongenerator.ner;

import edu.kit.kastel.mcse.ardoco.core.api.models.Metamodel;
import edu.kit.kastel.mcse.ardoco.core.data.PipelineStepData;

/**
 * State interface for managing multiple connection states for NER approaches by metamodel.
 */
public interface NerConnectionStates extends PipelineStepData {
    /**
     * The ID for this state.
     */
    String ID = NerConnectionStates.class.getSimpleName();

    /**
     * Returns the connection state for the given metamodel.
     *
     * @param metamodel the metamodel
     * @return the connection state
     */
    NerConnectionState getNerConnectionState(Metamodel metamodel);
}
