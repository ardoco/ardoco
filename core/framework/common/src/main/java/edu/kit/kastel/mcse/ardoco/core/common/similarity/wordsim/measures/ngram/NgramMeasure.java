/* Licensed under MIT 2022-2024. */
package edu.kit.kastel.mcse.ardoco.core.common.similarity.wordsim.measures.ngram;

import java.util.Objects;

import edu.kit.kastel.mcse.ardoco.core.common.similarity.wordsim.ComparisonContext;
import edu.kit.kastel.mcse.ardoco.core.common.similarity.wordsim.WordSimMeasure;
import edu.kit.kastel.mcse.ardoco.core.common.util.CommonTextToolsConfig;

/**
 * This word similarity measure uses the N-gram word distance function defined by Kondrak 2005.
 */
public class NgramMeasure implements WordSimMeasure {

    private static final char LUCENE_PREFIX_CHARACTER = '\n';

    /**
     * The variants of this algorithm
     */
    public enum Variant {
        /**
         * This variant matches the algorithm included in apache/lucene which is also positional but deviates from the original algorithm by using
         * {@link #LUCENE_PREFIX_CHARACTER} as the prefix character and changing the weight for the dN function.
         */
        LUCENE,
        /**
         * The positional variant as described in Kondrak 2005
         */
        POSITIONAL
    }

    private final Variant variant;
    private final int n;
    private final double similarityThreshold;

    /**
     * Constructs a new {@link NgramMeasure} using the settings provided by {@link edu.kit.kastel.mcse.ardoco.core.common.util.CommonTextToolsConfig}.
     */
    public NgramMeasure() {
        this(Variant.LUCENE, CommonTextToolsConfig.NGRAM_MEASURE_NGRAM_LENGTH, CommonTextToolsConfig.NGRAM_SIMILARITY_THRESHOLD);
    }

    /**
     * Constructs a new {@link NgramMeasure}.
     *
     * @param variant             the variant that should be used
     * @param n                   the length of the considered n-grams, must be a positive integer
     * @param similarityThreshold the threshold above which words are considered similar, between 0 and 1
     * @throws IllegalArgumentException if {@code n} or similarityThreshold are invalid
     */
    public NgramMeasure(Variant variant, int n, double similarityThreshold) throws IllegalArgumentException {
        this.variant = Objects.requireNonNull(variant);
        this.n = n;
        this.similarityThreshold = similarityThreshold;

        if (n <= 0) {
            throw new IllegalArgumentException("n must be a positive integer: " + n);
        }

        if (similarityThreshold < 0.0 || similarityThreshold > 1.0) {
            throw new IllegalArgumentException("similarityThreshold outside of valid range: " + similarityThreshold);
        }
    }

    @Override
    public boolean areWordsSimilar(ComparisonContext ctx) {
        Objects.requireNonNull(ctx);
        return this.getSimilarity(ctx) >= this.similarityThreshold;
    }

    @Override
    public double getSimilarity(ComparisonContext ctx) {
        double distance = this.calculateDistance(ctx.firstTerm(), ctx.secondTerm());

        double normalizedDistance = distance / Math.max(ctx.firstTerm().length(), ctx.secondTerm().length());

        return 1.0 - normalizedDistance;
    }

    /**
     * Calculates the distance between the two given strings.
     *
     * @param x the first string
     * @param y the second string
     * @return the distance
     */
    public double calculateDistance(String x, String y) {
        Objects.requireNonNull(x);
        Objects.requireNonNull(y);
        StringBuilder xBuilder = new StringBuilder(x);
        StringBuilder yBuilder = new StringBuilder(y);

        if (xBuilder.isEmpty() || yBuilder.isEmpty()) {
            return Math.max(xBuilder.length(), yBuilder.length());
        }

        int k = xBuilder.length();
        int l = yBuilder.length();
        double[][] d = new double[k + 1][l + 1];

        for (int u = 1; u <= this.n - 1; u++) {
            if (this.variant == Variant.LUCENE) {
                xBuilder = new StringBuilder().append(LUCENE_PREFIX_CHARACTER).append(xBuilder);
                yBuilder = new StringBuilder().append(LUCENE_PREFIX_CHARACTER).append(yBuilder);
            } else if (this.variant == Variant.POSITIONAL) {
                xBuilder = new StringBuilder().append(xBuilder.charAt(0)).append(xBuilder);
                yBuilder = new StringBuilder().append(yBuilder.charAt(0)).append(yBuilder);
            } else {
                throw new UnsupportedOperationException("unknown variant: " + this.variant);
            }
        }

        for (int i = 0; i <= k; i++) {
            d[i][0] = i;
        }

        for (int j = 1; j <= l; j++) {
            d[0][j] = j;
        }

        for (int i = 1; i <= k; i++) {
            for (int j = 1; j <= l; j++) {
                double dN = this.dN(this.n, i - 1, j - 1, xBuilder.toString(), yBuilder.toString());

                d[i][j] = this.min(d[i - 1][j] + 1.0, d[i][j - 1] + 1.0, d[i - 1][j - 1] + dN);
            }
        }

        return d[k][l];
    }

    private double dN(int n, int i, int j, String x, String y) {
        double sum = 0.0;
        double actualN = n;

        for (int u = 1; u <= n; u++) {
            double diff = this.d1(x.charAt(i + u - 1), y.charAt(j + u - 1));

            sum += diff;

            if (this.variant == Variant.LUCENE && diff == 0 && x.charAt(i + u - 1) == LUCENE_PREFIX_CHARACTER) {
                actualN -= 1.0; // Ignore prefix character in LUCENE mode
            }
        }

        return (1.0 / actualN) * sum;
    }

    private double d1(char xChar, char yChar) {
        return xChar == yChar ? 0.0 : 1.0;
    }

    private double min(double a, double b, double c) {
        return Math.min(a, Math.min(b, c));
    }

}
