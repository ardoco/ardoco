package edu.kit.kastel.mcse.ardoco.tlr.artemis.states;

import java.io.Serial;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

import edu.kit.kastel.mcse.ardoco.core.architecture.Deterministic;
import edu.kit.kastel.mcse.ardoco.core.data.AbstractState;

/**
 * Container state for all ArTEMiS traceability states of one pipeline run.
 */
@Deterministic
public class ArtemisTraceabilityStatesImpl extends AbstractState implements ArtemisTraceabilityStates {

    @Serial
    private static final long serialVersionUID = 1L;

    private final Map<ArtemisTarget, ArtemisTraceabilityState> states = new LinkedHashMap<>();

    public ArtemisTraceabilityStatesImpl() {
        super();
    }

    private ArtemisTraceabilityStatesImpl(Collection<ArtemisTarget> targets) {
        this();
        for (ArtemisTarget target : targets) {
            addState(target, new ArtemisTraceabilityStateImpl());
        }
    }

    public static ArtemisTraceabilityStatesImpl build(Collection<ArtemisTarget> targets) {
        return new ArtemisTraceabilityStatesImpl(targets);
    }

    @Override
    public ArtemisTraceabilityState getState(ArtemisTarget target) {
        Objects.requireNonNull(target);
        return this.states.computeIfAbsent(target, ignored -> new ArtemisTraceabilityStateImpl());
    }

    public void addState(ArtemisTarget target, ArtemisTraceabilityState state) {
        Objects.requireNonNull(target);
        Objects.requireNonNull(state);
        this.states.put(target, state);
    }

    @Override
    public Map<ArtemisTarget, ArtemisTraceabilityState> getStates() {
        return Map.copyOf(this.states);
    }
}
