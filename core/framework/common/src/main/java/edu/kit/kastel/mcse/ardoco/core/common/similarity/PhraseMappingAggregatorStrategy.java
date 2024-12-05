/* Licensed under MIT 2022-2024. */
package edu.kit.kastel.mcse.ardoco.core.common.similarity;

import static edu.kit.kastel.mcse.ardoco.core.common.similarity.SimilarityUtils.cosineSimilarity;
import static edu.kit.kastel.mcse.ardoco.core.common.similarity.SimilarityUtils.uniqueDot;

import java.util.function.ToDoubleBiFunction;

import edu.kit.kastel.mcse.ardoco.core.api.stage.textextraction.PhraseMapping;
import edu.kit.kastel.mcse.ardoco.core.architecture.Deterministic;

@Deterministic
public enum PhraseMappingAggregatorStrategy implements ToDoubleBiFunction<PhraseMapping, PhraseMapping> {
    MAX_SIMILARITY((a, b) -> uniqueDot(a.getPhrases().toImmutableList(), b.getPhrases().toImmutableList()).stream()
            .mapToDouble(p -> cosineSimilarity(p.first().getPhraseVector().toSortedMap(), p.second().getPhraseVector().toSortedMap()))
            .max()
            .orElse(Double.NaN)), //

    MIN_SIMILARITY((a, b) -> uniqueDot(a.getPhrases().toImmutableList(), b.getPhrases().toImmutableList()).stream()
            .mapToDouble(p -> cosineSimilarity(p.first().getPhraseVector().toSortedMap(), p.second().getPhraseVector().toSortedMap()))
            .min()
            .orElse(Double.NaN)), //

    AVG_SIMILARITY((a, b) -> uniqueDot(a.getPhrases().toImmutableList(), b.getPhrases().toImmutableList()).stream()
            .mapToDouble(p -> cosineSimilarity(p.first().getPhraseVector().toSortedMap(), p.second().getPhraseVector().toSortedMap()))
            .average()
            .orElse(Double.NaN));

    private final ToDoubleBiFunction<PhraseMapping, PhraseMapping> mapper;

    PhraseMappingAggregatorStrategy(ToDoubleBiFunction<PhraseMapping, PhraseMapping> mapper) {
        this.mapper = mapper;
    }

    @Override
    public double applyAsDouble(PhraseMapping phraseMapping, PhraseMapping phraseMapping2) {
        return this.mapper.applyAsDouble(phraseMapping, phraseMapping2);
    }
}
