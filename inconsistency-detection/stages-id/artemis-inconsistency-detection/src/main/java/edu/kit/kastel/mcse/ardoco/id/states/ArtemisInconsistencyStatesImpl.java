package edu.kit.kastel.mcse.ardoco.id.states;

import java.io.Serial;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

import edu.kit.kastel.mcse.ardoco.core.api.stage.connectiongenerator.artemis.ArtemisTarget;
import edu.kit.kastel.mcse.ardoco.core.api.stage.inconsistency.ArtemisInconsistencyState;
import edu.kit.kastel.mcse.ardoco.core.architecture.Deterministic;
import edu.kit.kastel.mcse.ardoco.core.data.AbstractState;

@Deterministic
public class ArtemisInconsistencyStatesImpl extends AbstractState
        implements edu.kit.kastel.mcse.ardoco.core.api.stage.inconsistency.ArtemisInconsistencyStates {

    @Serial
    private static final long serialVersionUID = 1L;

    private final Map<ArtemisTarget, ArtemisInconsistencyState> states = new LinkedHashMap<>();

    public ArtemisInconsistencyStatesImpl() {
        super();
    }

    private ArtemisInconsistencyStatesImpl(Collection<ArtemisTarget> targets) {
        this();
        for (var target : targets) {
            addState(target, new ArtemisInconsistencyStateImpl());
        }
    }

    public static ArtemisInconsistencyStatesImpl build(Collection<ArtemisTarget> targets) {
        return new ArtemisInconsistencyStatesImpl(targets);
    }

    @Override
    public ArtemisInconsistencyState getState(ArtemisTarget target) {
        Objects.requireNonNull(target);
        return this.states.computeIfAbsent(target, ignored -> new ArtemisInconsistencyStateImpl());
    }

    public void addState(ArtemisTarget target, ArtemisInconsistencyState state) {
        Objects.requireNonNull(target);
        Objects.requireNonNull(state);
        this.states.put(target, state);
    }

    @Override
    public Map<ArtemisTarget, ArtemisInconsistencyState> getStates() {
        return Map.copyOf(this.states);
    }
}
