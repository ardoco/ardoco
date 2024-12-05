/* Licensed under MIT 2022-2024. */
package edu.kit.kastel.mcse.ardoco.core.common.similarity.wordsim;

/**
 * A measure that can determine whether two words from a {@link ComparisonContext} are similar.
 */
public interface WordSimMeasure {

    /**
     * Evaluates whether the words from the given {@link ComparisonContext} are similar.
     *
     * @param ctx the context containing the words
     * @return Returns {@code true} if the words are similar.
     */
    boolean areWordsSimilar(ComparisonContext ctx);

    /**
     * Evaluates how similar the words from the given {@link ComparisonContext} are.
     *
     * @param ctx the context containing the words
     * @return Similarity in range [0,1]
     */
    double getSimilarity(ComparisonContext ctx);
}
