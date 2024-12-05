/* Licensed under MIT 2022-2024. */
package edu.kit.kastel.mcse.ardoco.tlr.recommendationgenerator;

import java.util.EnumMap;

import edu.kit.kastel.mcse.ardoco.core.api.models.Metamodel;
import edu.kit.kastel.mcse.ardoco.core.api.stage.recommendationgenerator.RecommendationStateStrategy;
import edu.kit.kastel.mcse.ardoco.core.api.stage.recommendationgenerator.RecommendationStates;

public class RecommendationStatesImpl implements RecommendationStates {
    private final EnumMap<Metamodel, RecommendationStateImpl> recommendationStates;

    private RecommendationStatesImpl() {
        this.recommendationStates = new EnumMap<>(Metamodel.class);
    }

    public static RecommendationStates build() {
        var recStates = new RecommendationStatesImpl();
        for (Metamodel mm : Metamodel.values()) {
            RecommendationStateStrategy rss = new DefaultRecommendationStateStrategy();
            recStates.recommendationStates.put(mm, new RecommendationStateImpl(rss));
        }
        return recStates;
    }

    @Override
    public RecommendationStateImpl getRecommendationState(Metamodel mm) {
        return this.recommendationStates.get(mm);
    }
}
