/* Licensed under MIT 2026. */
package edu.kit.kastel.mcse.ardoco.id.states;

import java.io.Serial;

import edu.kit.kastel.mcse.ardoco.core.api.stage.inconsistency.AbstractInconsistencyState;
import edu.kit.kastel.mcse.ardoco.core.api.stage.inconsistency.ArtemisInconsistencyState;
import edu.kit.kastel.mcse.ardoco.core.architecture.Deterministic;

@Deterministic
public class ArtemisInconsistencyStateImpl extends AbstractInconsistencyState implements ArtemisInconsistencyState {

    @Serial
    private static final long serialVersionUID = 1L;

    public ArtemisInconsistencyStateImpl() {
        super();
    }
}
