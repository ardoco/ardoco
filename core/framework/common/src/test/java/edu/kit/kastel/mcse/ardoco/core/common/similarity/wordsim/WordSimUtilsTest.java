/* Licensed under MIT 2023-2024. */
package edu.kit.kastel.mcse.ardoco.core.common.similarity.wordsim;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class WordSimUtilsTest {

    @Test
    void getSimilarity() {
        var wordSimUtils = new WordSimUtils();
        assertEquals(1, wordSimUtils.getSimilarity("", ""));
        assertEquals(1, wordSimUtils.getSimilarity("lorem", "lorem"));
        assertEquals(1, wordSimUtils.getSimilarity("lorem ipsum", "lorem ipsum"));
        assertEquals(1, wordSimUtils.getSimilarity("lOrEm IpSuM", "lorem ipsum", true));
    }
}
