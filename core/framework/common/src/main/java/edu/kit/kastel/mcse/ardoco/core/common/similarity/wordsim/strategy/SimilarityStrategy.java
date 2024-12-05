/* Licensed under MIT 2023-2024. */
package edu.kit.kastel.mcse.ardoco.core.common.similarity.wordsim.strategy;

import java.util.List;

import edu.kit.kastel.mcse.ardoco.core.common.similarity.wordsim.ComparisonContext;
import edu.kit.kastel.mcse.ardoco.core.common.similarity.wordsim.WordSimMeasure;

public interface SimilarityStrategy {
    /**
     * Evaluates how similar the words from the given {@link ComparisonContext} are by combining the verdicts of the specified word similarity measures.
     *
     * @param ctx      the context containing the words
     * @param measures the measures to use
     * @return Returns similarity in range [0,1]
     */
    double getSimilarity(ComparisonContext ctx, List<WordSimMeasure> measures);
}
