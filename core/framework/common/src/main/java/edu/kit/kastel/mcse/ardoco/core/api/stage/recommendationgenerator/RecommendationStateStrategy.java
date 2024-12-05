/* Licensed under MIT 2024. */
package edu.kit.kastel.mcse.ardoco.core.api.stage.recommendationgenerator;

public interface RecommendationStateStrategy {
    boolean areRecommendedInstanceTypesSimilar(String typeA, String typeB);

    boolean areRecommendedInstanceNamesSimilar(String nameA, String nameB);
}
