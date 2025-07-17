/* Licensed under MIT 2022-2025. */
package edu.kit.kastel.mcse.ardoco.core.common.similarity.wordsim.strategy;

import java.util.List;

import edu.kit.kastel.mcse.ardoco.core.common.similarity.wordsim.ComparisonContext;
import edu.kit.kastel.mcse.ardoco.core.common.similarity.wordsim.WordSimMeasure;

/**
 * Comparison strategy: accepts a word pair as similar if the majority of measures accept it as similar.
 */
public class MajorityStrategy implements ComparisonStrategy {

    /**
     * Returns true if the majority of measures consider the words similar.
     *
     * @param comparisonContext the comparison context
     * @param measures          the measures to use
     * @return true if the majority of measures return true
     */
    @Override
    public boolean areWordsSimilar(ComparisonContext comparisonContext, List<WordSimMeasure> measures) {
        int acceptances = 0;

        for (WordSimMeasure measure : measures) {
            if (measure.areWordsSimilar(comparisonContext)) {
                acceptances++;
            }
        }

        return acceptances > (measures.size() / 2);
    }

}
