/* Licensed under MIT 2022-2023. */
package edu.kit.kastel.mcse.ardoco.core.common.similarity.wordsim.measures.ngram;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.apache.commons.text.similarity.LevenshteinDistance;
import org.junit.jupiter.api.Test;

import edu.kit.kastel.mcse.ardoco.core.common.similarity.wordsim.ComparisonContext;

class NgramMeasureTest {

    private static final List<String> RANDOM_WORDS = List.of("acidic", "identify", "downtown", "elbow", "remove", "itch", "dirt",
            "a b c d e f g h i j k l m n o p", "welcome", "insect", "smoke", "change");

    @Test
    void testUnigramDistance() {
        var measure = new NgramMeasure(NgramMeasure.Variant.POSITIONAL, 1, 1.0);
        var levenshteinDistance = new LevenshteinDistance();

        assertEquals(1.0, measure.calculateDistance("Hello", "Hella"), 0.01);

        for (String firstWord : RANDOM_WORDS) {
            for (String secondWord : RANDOM_WORDS) {
                double levenshteinResult = levenshteinDistance.apply(firstWord, secondWord);
                double ngramResult = measure.calculateDistance(firstWord, secondWord);

                assertEquals(levenshteinResult, ngramResult, 0.1);
            }
        }

        assertEquals(0.0, measure.calculateDistance("", ""));
        assertEquals("Hello".length(), measure.calculateDistance("Hello", ""));
        assertEquals("Hello".length(), measure.calculateDistance("", "Hello"));
    }

    @Test
    void testUnigramSimilarity() {
        var measure = new NgramMeasure(NgramMeasure.Variant.POSITIONAL, 1, 0.7);

        assertTrue(measure.areWordsSimilar(new ComparisonContext("Hello", "Hella"))); // 0.8
        assertFalse(measure.areWordsSimilar(new ComparisonContext("Hello", "Heal"))); // 0.6
    }

    @Test
    void testBigram() {
        var measure = new NgramMeasure(NgramMeasure.Variant.POSITIONAL, 2, 0.7);
        assertEquals(0.5, measure.calculateDistance("Hello", "Hella"), 0.01);

        assertTrue(measure.areWordsSimilar(new ComparisonContext("Hello", "Hella"))); // 0.8
        assertFalse(measure.areWordsSimilar(new ComparisonContext("Hello", "Heal"))); // 0.6

        assertEquals(0.0, measure.calculateDistance("", ""));
    }

    @Test
    void testTrigram() {
        var measure = new NgramMeasure(NgramMeasure.Variant.POSITIONAL, 3, 0.7);
        assertEquals(1.0 / 3.0, measure.calculateDistance("Hello", "Hella"), 0.01);

        assertTrue(measure.areWordsSimilar(new ComparisonContext("Hello", "Hella"))); // 0.8
        assertFalse(measure.areWordsSimilar(new ComparisonContext("Hello", "Heal"))); // 0.6

        assertEquals(0.0, measure.calculateDistance("", ""));
    }

    @Test
    void testConstructor() {
        assertThrows(IllegalArgumentException.class, () -> new NgramMeasure(NgramMeasure.Variant.LUCENE, -1, 0.5));
        assertThrows(IllegalArgumentException.class, () -> new NgramMeasure(NgramMeasure.Variant.LUCENE, 1, -0.1));
        assertThrows(IllegalArgumentException.class, () -> new NgramMeasure(NgramMeasure.Variant.LUCENE, 1, 1.1));
        assertThrows(NullPointerException.class, () -> new NgramMeasure(null, 1, 1.1));
        new NgramMeasure(NgramMeasure.Variant.LUCENE, 1, 0.0);
        new NgramMeasure(NgramMeasure.Variant.LUCENE, 1, 0.5);
        new NgramMeasure(NgramMeasure.Variant.LUCENE, 1, 1.0);
        new NgramMeasure(NgramMeasure.Variant.POSITIONAL, 1, 1.0);
        new NgramMeasure(NgramMeasure.Variant.POSITIONAL, 2, 1.0);
        new NgramMeasure(NgramMeasure.Variant.POSITIONAL, 12345, 1.0);
        new NgramMeasure(NgramMeasure.Variant.POSITIONAL, Integer.MAX_VALUE, 1.0);
    }

}
