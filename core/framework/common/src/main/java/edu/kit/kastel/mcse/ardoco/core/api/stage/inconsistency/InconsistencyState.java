/* Licensed under MIT 2021-2025. */
package edu.kit.kastel.mcse.ardoco.core.api.stage.inconsistency;

import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.list.MutableList;

import edu.kit.kastel.mcse.ardoco.core.api.stage.recommendationgenerator.RecommendedInstance;

/**
 * State holding data and information about inconsistencies.
 */
public interface InconsistencyState extends BaseInconsistencyState {

    /**
     * Add multiple recommended instances to this state.
     *
     * @param recommendedInstances the recommended instances to add
     * @return true if all were added successfully
     */
    default boolean addRecommendedInstances(ImmutableList<RecommendedInstance> recommendedInstances) {
        var success = true;
        for (var recommendedInstance : recommendedInstances) {
            success &= addRecommendedInstance(recommendedInstance);
        }
        return success;
    }

    /**
     * Add a single recommended instance to this state.
     *
     * @param recommendedInstance the recommended instance to add
     * @return true if added successfully
     */
    boolean addRecommendedInstance(RecommendedInstance recommendedInstance);

    /**
     * Returns the recommended instances.
     *
     * @return the recommended instances
     */
    MutableList<RecommendedInstance> getRecommendedInstances();

    /**
     * Sets the recommended instances.
     *
     * @param recommendedInstances the recommended instances to set
     */
    void setRecommendedInstances(ImmutableList<RecommendedInstance> recommendedInstances);
}
