/* Licensed under MIT 2022-2026. */
package edu.kit.kastel.mcse.ardoco.core.api.stage.recommendationgenerator;

import org.jspecify.annotations.Nullable;

import edu.kit.kastel.mcse.ardoco.core.api.models.Metamodel;
import edu.kit.kastel.mcse.ardoco.core.data.PipelineStepData;

/**
 * Represents the collection of recommendation states for different metamodels.
 */
public interface RecommendationStates extends PipelineStepData {
    /**
     * The unique identifier for recommendation states.
     */
    String ID = "RecommendationStates";

    /**
     * Returns the recommendation state for the given metamodel.
     *
     * @param metamodel the metamodel
     * @return the recommendation state
     */
    @Nullable
    RecommendationState getRecommendationState(Metamodel metamodel);
}
