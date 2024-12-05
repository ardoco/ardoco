/* Licensed under MIT 2022-2024. */
package edu.kit.kastel.mcse.ardoco.core.common.similarity.wordsim.measures.equality;

import edu.kit.kastel.mcse.ardoco.core.common.similarity.wordsim.ComparisonContext;
import edu.kit.kastel.mcse.ardoco.core.common.similarity.wordsim.WordSimMeasure;

/**
 * This word similarity measure just checks whether the most appropriate string representations of the passed objects are equal (ignoring case).
 *
 */
public class EqualityMeasure implements WordSimMeasure {
    @Override
    public boolean areWordsSimilar(ComparisonContext ctx) {
        return ctx.firstTerm().equalsIgnoreCase(ctx.secondTerm());
    }

    @Override
    public double getSimilarity(ComparisonContext ctx) {
        return this.areWordsSimilar(ctx) ? 1 : 0;
    }
}
