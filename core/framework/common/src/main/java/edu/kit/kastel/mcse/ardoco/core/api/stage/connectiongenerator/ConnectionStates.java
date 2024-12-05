/* Licensed under MIT 2022-2023. */
package edu.kit.kastel.mcse.ardoco.core.api.stage.connectiongenerator;

import edu.kit.kastel.mcse.ardoco.core.api.models.Metamodel;
import edu.kit.kastel.mcse.ardoco.core.data.PipelineStepData;

public interface ConnectionStates extends PipelineStepData {
    String ID = "ConnectionStates";

    ConnectionState getConnectionState(Metamodel mm);
}
