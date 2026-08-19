package edu.kit.kastel.mcse.ardoco.id.states;

import java.io.Serial;

import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.list.MutableList;

import edu.kit.kastel.mcse.ardoco.core.api.stage.inconsistency.ArtemisInconsistencyState;
import edu.kit.kastel.mcse.ardoco.core.api.stage.inconsistency.Inconsistency;
import edu.kit.kastel.mcse.ardoco.core.architecture.Deterministic;
import edu.kit.kastel.mcse.ardoco.core.data.AbstractState;

@Deterministic
public class ArtemisInconsistencyStateImpl extends AbstractState implements ArtemisInconsistencyState {

    @Serial
    private static final long serialVersionUID = 1L;

    private final MutableList<Inconsistency> inconsistencies;

    public ArtemisInconsistencyStateImpl() {
        super();
        this.inconsistencies = Lists.mutable.empty();
    }

    @Override
    public boolean addInconsistency(Inconsistency inconsistency) {
        if (this.inconsistencies.contains(inconsistency)) {
            return false;
        }
        return this.inconsistencies.add(inconsistency);
    }

    @Override
    public ImmutableList<Inconsistency> getInconsistencies() {
        return this.inconsistencies.toImmutable();
    }

    @Override
    public <T extends Inconsistency> ImmutableList<T> getInconsistenciesOfType(Class<T> type) {
        return this.inconsistencies.select(type::isInstance).collect(type::cast).toImmutable();
    }
}
