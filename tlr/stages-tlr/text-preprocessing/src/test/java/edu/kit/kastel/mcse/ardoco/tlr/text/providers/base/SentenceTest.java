/* Licensed under MIT 2022-2024. */
package edu.kit.kastel.mcse.ardoco.tlr.text.providers.base;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import edu.kit.kastel.mcse.ardoco.core.api.text.NlpInformant;
import edu.kit.kastel.mcse.ardoco.core.api.text.Sentence;

public abstract class SentenceTest {
    private static Sentence sentence;

    @BeforeEach
    void beforeEach() {
        var provider = getProvider();
        var text = provider.getAnnotatedText();
        sentence = text.getSentences().get(41);
    }

    protected abstract NlpInformant getProvider();

    @Test
    void getTextTest() {
        Assertions.assertEquals("The TeaStore is a test application.", sentence.getText());
    }

    @Test
    void getWordsTest() {
        var words = sentence.getWords();
        Assertions.assertEquals(7, words.size());
    }

    @Test
    void getSentenceNoTest() {
        Assertions.assertEquals(41, sentence.getSentenceNumber());
    }

    @Test
    void getPhrasesTest() {
        var phrases = sentence.getPhrases();
        Assertions.assertEquals(5, phrases.size());
    }
}
