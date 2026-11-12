/* Licensed under MIT 2026. */
package edu.kit.kastel.mcse.ardoco.core.api.stage.inconsistency;

import org.eclipse.collections.api.list.ImmutableList;

import edu.kit.kastel.mcse.ardoco.core.configuration.IConfigurable;

/**
 * Common state interface for all inconsistency states.
 */
public interface BaseInconsistencyState extends IConfigurable {

    /**
     * Adds an inconsistency to this state.
     *
     * @param inconsistency the inconsistency to add
     * @return true if added successfully
     */
    boolean addInconsistency(Inconsistency inconsistency);

    /**
     * Returns all inconsistencies held by this state.
     *
     * @return all inconsistencies
     */
    ImmutableList<Inconsistency> getInconsistencies();

    /**
     * Returns all inconsistencies of the given type.
     *
     * @param type the inconsistency type
     * @param <T>  the inconsistency type
     * @return all inconsistencies of the given type
     */
    default <T extends Inconsistency> ImmutableList<T> getInconsistenciesOfType(Class<T> type) {
        return getInconsistencies().select(type::isInstance).collect(type::cast).toImmutable();
    }
}
