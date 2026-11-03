/* Licensed under MIT 2026. */
package edu.kit.kastel.mcse.ardoco.core.api.stage.inconsistency;

import java.io.Serial;

import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.list.MutableList;

import edu.kit.kastel.mcse.ardoco.core.data.AbstractState;

/**
 * Common base implementation for inconsistency states.
 */
public abstract class AbstractInconsistencyState extends AbstractState implements BaseInconsistencyState {

    @Serial
    private static final long serialVersionUID = 1L;

    private final MutableList<Inconsistency> inconsistencies;

    protected AbstractInconsistencyState() {
        super();
        this.inconsistencies = Lists.mutable.empty();
    }

    /**
     * Adds an inconsistency to this state. Does not add duplicate inconsistencies.
     *
     * @param inconsistency the inconsistency to add
     * @return true if added successfully
     */
    @Override
    public boolean addInconsistency(Inconsistency inconsistency) {
        if (this.inconsistencies.contains(inconsistency)) {
            return false;
        }
        return this.inconsistencies.add(inconsistency);
    }

    /**
     * Returns all inconsistencies held by this state.
     *
     * @return all inconsistencies
     */
    @Override
    public ImmutableList<Inconsistency> getInconsistencies() {
        return this.inconsistencies.toImmutable();
    }
}
