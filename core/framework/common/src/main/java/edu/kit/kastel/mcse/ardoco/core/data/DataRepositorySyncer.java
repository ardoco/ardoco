/* Licensed under MIT 2024-2025. */
package edu.kit.kastel.mcse.ardoco.core.data;

import edu.kit.kastel.mcse.ardoco.core.api.models.Metamodel;
import edu.kit.kastel.mcse.ardoco.core.api.stage.recommendationgenerator.RecommendationState;
import edu.kit.kastel.mcse.ardoco.core.api.stage.recommendationgenerator.RecommendationStates;
import edu.kit.kastel.mcse.ardoco.core.api.stage.textextraction.NounMapping;
import edu.kit.kastel.mcse.ardoco.core.common.persistence.PersistenceBridge;

/**
 * Utility class for synchronizing changes in the data repository across different components and states.
 */
public final class DataRepositorySyncer {
    private DataRepositorySyncer() {
        throw new IllegalAccessError("Utility class");
    }

    /**
     * Handles the deletion of a noun mapping in the data repository, updating all relevant recommendation states.
     *
     * @param dataRepository the data repository
     * @param nounMapping    the noun mapping to delete
     * @param replacement    the replacement noun mapping, if any
     */
    public static void onNounMappingDeletion(DataRepository dataRepository, NounMapping nounMapping, NounMapping replacement) {
        // We need to inform the recommendation state
        var recommendationStates = dataRepository.getData(RecommendationStates.ID, RecommendationStates.class);
        if (recommendationStates.isEmpty()) {
            return;
        }
        for (Metamodel mm : Metamodel.values()) {
            var recommendationState = recommendationStates.get().getRecommendationState(mm);
            if (recommendationState != null) {
                recommendationState.onNounMappingDeletion(nounMapping, replacement);
            }
        }
    }

    /**
     * Re-saves recommended instances that reference the given noun mapping in Neo4j.
     * Used after noun-mapping merges so {@code HAS_NAME_MAPPING} / {@code HAS_TYPE_MAPPING} edges match in-memory state.
     *
     * @param dataRepository the data repository
     * @param nounMapping    the mapping that was just merged or updated
     */
    public static void repersistRecommendedInstancesReferencing(DataRepository dataRepository, NounMapping nounMapping) {
        if (!PersistenceBridge.shouldPersistRecommendations()) {
            return;
        }
        var recommendationStates = dataRepository.getData(RecommendationStates.ID, RecommendationStates.class);
        if (recommendationStates.isEmpty()) {
            return;
        }
        for (Metamodel mm : Metamodel.values()) {
            RecommendationState recommendationState = recommendationStates.get().getRecommendationState(mm);
            if (recommendationState != null) {
                recommendationState.repersistInstancesReferencing(nounMapping);
            }
        }
    }

}
