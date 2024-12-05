/* Licensed under MIT 2021-2023. */
package edu.kit.kastel.mcse.ardoco.core.api.stage.inconsistency;

import java.util.List;

import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.list.MutableList;

import edu.kit.kastel.mcse.ardoco.core.api.stage.recommendationgenerator.RecommendedInstance;
import edu.kit.kastel.mcse.ardoco.core.configuration.IConfigurable;

/**
 * Inconsistency state holding data and information about inconsistency.
 * 
 */
public interface InconsistencyState extends IConfigurable {

    /**
     * Returns a list of inconsistencies held by this state
     *
     * @return list of inconsistencies
     */
    ImmutableList<Inconsistency> getInconsistencies();

    /**
     * Add an Inconsistency to this state
     *
     * @param inconsistency the inconsistency to add
     * @return true if added successfully
     */
    boolean addInconsistency(Inconsistency inconsistency);

    /**
     * Remove an Inconsistency from this state
     *
     * @param inconsistency the inconsistency to remove
     * @return true if removed successfully
     */
    boolean removeInconsistency(Inconsistency inconsistency);

    default boolean addRecommendedInstances(List<RecommendedInstance> recommendedInstances) {
        var success = true;
        for (var recommendedInstance : recommendedInstances) {
            success &= addRecommendedInstance(recommendedInstance);
        }
        return success;
    }

    boolean addRecommendedInstance(RecommendedInstance recommendedInstance);

    default boolean removeRecommendedInstances(List<RecommendedInstance> recommendedInstances) {
        var success = true;
        for (var recommendedInstance : recommendedInstances) {
            success &= removeRecommendedInstance(recommendedInstance);
        }
        return success;
    }

    boolean removeRecommendedInstance(RecommendedInstance recommendedInstance);

    /**
     * Sets the recommended Instances
     * 
     * @param recommendedInstances the recommendedInstances to set
     */
    void setRecommendedInstances(List<RecommendedInstance> recommendedInstances);

    /**
     * Returns the recommended Instances
     * 
     * @return the recommendedInstances
     */
    MutableList<RecommendedInstance> getRecommendedInstances();

}
