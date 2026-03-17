/* Licensed under MIT 2026. */
package io.github.ardoco.core.neo4jschema.util.models;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.eclipse.collections.api.list.ImmutableList;

import edu.kit.kastel.mcse.ardoco.core.api.text.DependencyTag;
import edu.kit.kastel.mcse.ardoco.core.api.text.Phrase;
import edu.kit.kastel.mcse.ardoco.core.api.text.Sentence;
import edu.kit.kastel.mcse.ardoco.core.api.text.Text;
import edu.kit.kastel.mcse.ardoco.core.api.text.Word;

import java.util.List;
import java.util.Set;

public class TextEqualityHelper {

    /**
     * Deep compares two Text objects using JUnit Assertions. Checks Sentences, Words (including dependencies), and Phrases.
     *
     * @param expected The original text (expected)
     * @param actual   The restored text (actual)
     */
    public static void assertTextsEqual(Text expected, Text actual) {
        if (expected == actual)
            return;
        assertNotNull(expected, "Expected Text is null");
        assertNotNull(actual, "Actual Text is null");

        assertEquals(expected.getSentences().size(), actual.getSentences().size(), "Sentence count mismatch");

        for (int i = 0; i < expected.getSentences().size(); i++) {
            assertSentencesEqual(expected.getSentences().get(i), actual.getSentences().get(i), i);
        }
    }

    private static void assertSentencesEqual(Sentence expected, Sentence actual, int index) {
        assertEquals(expected.getSentenceNumber(), actual.getSentenceNumber(), () -> "Sentence number mismatch at index " + index);
        assertEquals(expected.getText(), actual.getText(), () -> "Sentence text mismatch at index " + index);
        assertEquals(expected.getWords().size(), actual.getWords().size(), () -> "Word count mismatch in sentence " + expected.getSentenceNumber());

        for (int i = 0; i < expected.getWords().size(); i++) {
            assertWordsEqual(expected.getWords().get(i), actual.getWords().get(i), "Word");
        }

        for (int i = 0; i < expected.getWords().size(); i++) {
            assertWordsEqual(expected.getWords().get(i).getNextWord(), expected.getWords().get(i).getNextWord(), "Next word");
            assertWordsEqual(expected.getWords().get(i).getPreWord(), expected.getWords().get(i).getPreWord(), "Previous word");
        }



        assertEquals(expected.getPhrases().size(), actual.getPhrases().size(), () -> "Phrase count mismatch in sentence " + expected.getSentenceNumber());

        for (int i = 0; i < expected.getPhrases().size(); i++) {
            assertPhrasesEqual(expected.getPhrases().get(i), actual.getPhrases().get(i));
        }
    }

    private static void assertWordsEqual(Word expected, Word actual, String wordContext) {
        if (expected == actual)
            return;

        if (expected == null || actual == null) {
            assertEquals(expected, actual, wordContext + " - One of the words is null while the other is not");
            return; // Both are null, considered equal
        }

        String context = wordContext + " mismatch (Sentence: " + expected.getSentenceNumber() + ", Pos: " + expected.getPosition() + ")";

        assertEquals(expected.getPosition(), actual.getPosition(), context + " - Position");
        assertEquals(expected.getSentenceNumber(), actual.getSentenceNumber(), context + " - Sentence Number");
        assertEquals(expected.getText(), actual.getText(), context + " - Text");
        assertEquals(expected.getLemma(), actual.getLemma(), context + " - Lemma");
        assertEquals(expected.getPosTag(), actual.getPosTag(), context + " - POS Tag");

        for (DependencyTag tag : DependencyTag.values()) {
            ImmutableList<Word> outExpected = expected.getOutgoingDependencyWordsWithType(tag);
            ImmutableList<Word> outActual = actual.getOutgoingDependencyWordsWithType(tag);

            assertEquals(outExpected.size(), outActual.size(), () -> context + " - Dependency count mismatch for tag " + tag);

            for (int k = 0; k < outExpected.size(); k++) {
                int finalK = k;
                assertEquals(outExpected.get(k).getPosition(), outActual.get(k).getPosition(),
                        () -> context + " - Dependency target mismatch for tag " + tag + " at index " + finalK);
            }
        }
    }

    private static void assertPhrasesEqual(Phrase expected, Phrase actual) {
        String context = "Phrase mismatch: " + expected.getText();

        assertEquals(expected.getPhraseType(), actual.getPhraseType(), context + " - Type");
        assertEquals(expected.getText(), actual.getText(), context + " - Text");
        assertEquals(expected.getContainedWords().size(), actual.getContainedWords().size(), context + " - Contained words count");

        List<Word> expected_words = expected.getContainedWords().toSortedList();
        List<Word> actual_words = actual.getContainedWords().toSortedList();

        for (int i = 0; i < expected_words.size(); i++) {
            assertEquals(expected_words.get(i).getPosition(), actual_words.get(i).getPosition(),
                    context + " - Contained word position mismatch at index " + i);
        }

        assertEquals(expected.getSubphrases().size(), actual.getSubphrases().size(), context + " - Subphrase count");

        for (int i = 0; i < expected.getSubphrases().size(); i++) {
            assertPhrasesEqual(expected.getSubphrases().get(i), actual.getSubphrases().get(i));
        }
    }
}
