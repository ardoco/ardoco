/* Licensed under MIT 2024-2025. */
package edu.kit.kastel.mcse.ardoco.core.data;

import edu.kit.kastel.mcse.ardoco.core.api.models.Metamodel;
import edu.kit.kastel.mcse.ardoco.core.api.stage.recommendationgenerator.RecommendationStates;
import edu.kit.kastel.mcse.ardoco.core.api.stage.textextraction.NounMapping;

public final class DataRepositorySyncer {
    private DataRepositorySyncer() {
        throw new IllegalAccessError("Utility class");
    }

    public static void onNounMappingDeletion(DataRepository dataRepository, NounMapping nounMapping, NounMapping replacement) {
        // We need to inform the recommendation state
        var recommendationStates = dataRepository.getData(RecommendationStates.ID, RecommendationStates.class);
        if (recommendationStates.isEmpty()) {
            return;
        }
        for (Metamodel mm : Metamodel.values()) {
            var recommendationState = recommendationStates.get().getRecommendationState(mm);
            recommendationState.onNounMappingDeletion(nounMapping, replacement);
        }
    }

}
