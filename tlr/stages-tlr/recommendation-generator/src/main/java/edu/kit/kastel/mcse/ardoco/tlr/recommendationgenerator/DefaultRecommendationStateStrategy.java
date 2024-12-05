/* Licensed under MIT 2024. */
package edu.kit.kastel.mcse.ardoco.tlr.recommendationgenerator;

import edu.kit.kastel.mcse.ardoco.core.api.stage.recommendationgenerator.RecommendationStateStrategy;
import edu.kit.kastel.mcse.ardoco.core.common.similarity.SimilarityUtils;

public class DefaultRecommendationStateStrategy implements RecommendationStateStrategy {

    @Override
    public boolean areRecommendedInstanceTypesSimilar(String typeA, String typeB) {
        return SimilarityUtils.getInstance().areWordsSimilar(typeA, typeB);
    }

    @Override
    public boolean areRecommendedInstanceNamesSimilar(String nameA, String nameB) {
        return SimilarityUtils.getInstance().areWordsSimilar(nameA, nameB);
    }
}
