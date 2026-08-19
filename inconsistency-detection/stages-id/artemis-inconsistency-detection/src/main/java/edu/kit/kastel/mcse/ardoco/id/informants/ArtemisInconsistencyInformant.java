package edu.kit.kastel.mcse.ardoco.id.informants;

import edu.kit.kastel.mcse.ardoco.core.api.stage.connectiongenerator.artemis.ArtemisConnectionState;
import edu.kit.kastel.mcse.ardoco.core.api.stage.connectiongenerator.artemis.ArtemisConnectionStates;
import edu.kit.kastel.mcse.ardoco.core.api.stage.connectiongenerator.artemis.ArtemisTarget;
import edu.kit.kastel.mcse.ardoco.core.api.stage.inconsistency.ArtemisInconsistencyState;
import edu.kit.kastel.mcse.ardoco.core.api.stage.inconsistency.ArtemisInconsistencyStates;
import edu.kit.kastel.mcse.ardoco.core.data.DataRepository;
import edu.kit.kastel.mcse.ardoco.core.pipeline.agent.Informant;
import edu.kit.kastel.mcse.ardoco.id.states.ArtemisInconsistencyStatesImpl;
import edu.kit.kastel.mcse.ardoco.tlr.artemis.states.ArtemisTraceabilityStatesImpl;
import edu.kit.kastel.mcse.ardoco.tlr.artemis.strategies.ArtemisNerStrategy;

public abstract class ArtemisInconsistencyInformant extends Informant {

    private final ArtemisNerStrategy strategy;

    protected ArtemisInconsistencyInformant(String id, DataRepository dataRepository, ArtemisNerStrategy strategy) {
        super(id, dataRepository);
        this.strategy = strategy;
    }

    protected ArtemisNerStrategy getStrategy() {
        return this.strategy;
    }

    protected ArtemisTarget getTarget() {
        return this.strategy.getTarget();
    }

    protected ArtemisConnectionState getArtemisTraceabilityState() { //TODO in the future: remove these here and put in ArdocoResult
        var states = getDataRepository().getData(ArtemisConnectionStates.ID, ArtemisTraceabilityStatesImpl.class).orElseThrow();
        return states.getState(getTarget());
    }

    protected ArtemisInconsistencyState getArtemisInconsistencyState() {
        var states = getDataRepository().getData(ArtemisInconsistencyStates.ID, ArtemisInconsistencyStatesImpl.class).orElseThrow();
        return states.getState(getTarget());
    }
}
