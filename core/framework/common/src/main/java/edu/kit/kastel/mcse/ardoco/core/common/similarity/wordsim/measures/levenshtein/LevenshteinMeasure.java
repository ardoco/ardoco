/* Licensed under MIT 2022-2024. */
package edu.kit.kastel.mcse.ardoco.core.common.similarity.wordsim.measures.levenshtein;

import org.apache.commons.text.similarity.LevenshteinDistance;

import edu.kit.kastel.mcse.ardoco.core.common.similarity.wordsim.ComparisonContext;
import edu.kit.kastel.mcse.ardoco.core.common.similarity.wordsim.WordSimMeasure;
import edu.kit.kastel.mcse.ardoco.core.common.util.CommonTextToolsConfig;

/**
 * This word similarity measure uses the levenshtein distance (also sometimes called edit distance) algorithm to calculate word similarity. This measure is
 * configurable through three configuration options:
 *
 * <ul>
 * <li><b>maxDistance:</b> Word pairs with a levenshtein distance above this configuration value will not be considered
 * similar. Must be a non-negative integer.</li>
 * <li><b>minLength:</b> If one of the words is shorter than this configured value, an additional condition must be met
 * for the word pair to be considered similar. This condition being, that one word must contain the other. Must be a
 * non-negative integer.</li>
 * <li><b>threshold:</b> A number between 0 and 1 that serves as a word-dependent distance limit. The levenshtein
 * distance between the words must be lower than the threshold multiplied by the length of the shorter word.</li>
 * </ul>
 */
public class LevenshteinMeasure implements WordSimMeasure {

    private final LevenshteinDistance levenshteinDistance = new LevenshteinDistance();
    private final int minLength;
    private final int maxDistance;
    private final double threshold;

    /**
     * Constructs a new {@link LevenshteinMeasure} using the settings provided by {@link CommonTextToolsConfig}.
     */
    public LevenshteinMeasure() {
        this(CommonTextToolsConfig.LEVENSHTEIN_MIN_LENGTH, CommonTextToolsConfig.LEVENSHTEIN_MAX_DISTANCE, CommonTextToolsConfig.LEVENSHTEIN_THRESHOLD);
    }

    /**
     * Constructs a new {@link LevenshteinMeasure}. The necessary arguments for this constructor are explained {@link LevenshteinMeasure here}.
     *
     * @param minLength   the min length
     * @param maxDistance the max distance
     * @param threshold   the threshold
     */
    public LevenshteinMeasure(int minLength, int maxDistance, double threshold) {
        this.minLength = minLength;
        this.maxDistance = maxDistance;
        this.threshold = threshold;

        if (minLength < 0) {
            throw new IllegalArgumentException("minLength must be a non-negative integer: " + minLength);
        }

        if (maxDistance < 0) {
            throw new IllegalArgumentException("maxDistance must be a non-negative integer: " + maxDistance);
        }

        if (threshold < 0.0 || threshold > 1.0) {
            throw new IllegalArgumentException("threshold outside of valid range: " + threshold);
        }
    }

    @Override
    public boolean areWordsSimilar(ComparisonContext ctx) {
        // FIXME cast to lower case seems unwarranted given that this is delegated to WordSimUtils already
        String firstWord = ctx.firstTerm().toLowerCase();
        String secondWord = ctx.secondTerm().toLowerCase();

        int maxDynamicDistance = (int) Math.min(this.maxDistance, this.threshold * Math.min(firstWord.length(), secondWord.length()));
        int distance = this.levenshteinDistance.apply(firstWord, secondWord);

        if (firstWord.length() <= this.minLength) {
            return distance <= this.maxDistance && (secondWord.contains(firstWord) || firstWord.contains(secondWord));
        }
        return distance <= maxDynamicDistance;
    }

    @Override
    public double getSimilarity(ComparisonContext ctx) {
        // FIXME cast to lower case seems unwarranted given that this is delegated to WordSimUtils already
        String firstWord = ctx.firstTerm().toLowerCase();
        String secondWord = ctx.secondTerm().toLowerCase();
        int distance = this.levenshteinDistance.apply(firstWord, secondWord);
        return 1.0 - (distance / (double) Math.max(firstWord.length(), secondWord.length()));
    }

}
