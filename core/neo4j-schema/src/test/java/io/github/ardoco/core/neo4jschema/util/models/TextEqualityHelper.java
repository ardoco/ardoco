/* Licensed under MIT 2026. */
package io.github.ardoco.core.neo4jschema.util.models;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.Comparator;
import java.util.List;

import org.eclipse.collections.api.list.ImmutableList;

import edu.kit.kastel.mcse.ardoco.core.api.text.DependencyTag;
import edu.kit.kastel.mcse.ardoco.core.api.text.Phrase;
import edu.kit.kastel.mcse.ardoco.core.api.text.Sentence;
import edu.kit.kastel.mcse.ardoco.core.api.text.Text;
import edu.kit.kastel.mcse.ardoco.core.api.text.Word;

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
            assertWordsEqual(expected.getWords().get(i).getNextWord(), actual.getWords().get(i).getNextWord(), "Next word");
            assertWordsEqual(expected.getWords().get(i).getPreWord(), actual.getWords().get(i).getPreWord(), "Previous word");
        }

        // Sorted by structure (start position, then span size), not by (text, type): the same text can legitimately occur
        // in more than one phrase with different types (e.g. a short token appearing in both a nested NP and its
        // enclosing FRAG), which made a (text, type) sort key pair up the wrong phrases depending on incidental
        // traversal order.
        Comparator<Phrase> phraseComparator = Comparator
                .<Phrase>comparingInt(p -> p.getContainedWords().isEmpty() ? -1 : p.getContainedWords().get(0).getPosition())
                .thenComparingInt(p -> p.getContainedWords().size())
                .thenComparing(Phrase::getText)
                .thenComparing(Phrase::getPhraseType);

        List<? extends Phrase> expectedPhrases = expected.getPhrases().stream().sorted(phraseComparator).toList();
        List<? extends Phrase> actualPhrases = actual.getPhrases().stream().sorted(phraseComparator).toList();

        assertEquals(expectedPhrases.size(), actualPhrases.size(), () -> "Phrase count mismatch in sentence " + expected.getSentenceNumber());

        for (int i = 0; i < expectedPhrases.size(); i++) {
            assertPhrasesEqual(expectedPhrases.get(i), actualPhrases.get(i));
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

            // Compare target positions as a set, not by traversal index: Neo4j relationships of the same type carry no
            // inherent order, so when a word has multiple same-tag dependencies, a plain round trip through the database
            // can return them in a different order than they were added without actually losing or corrupting any of them.
            List<Integer> expectedPositions = outExpected.stream().map(Word::getPosition).sorted().toList();
            List<Integer> actualPositions = outActual.stream().map(Word::getPosition).sorted().toList();
            assertEquals(expectedPositions, actualPositions, () -> context + " - Dependency target positions mismatch for tag " + tag);
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
            assertEquals(expected_words.get(i).getPosition(), actual_words.get(i).getPosition(), context + " - Contained word position mismatch at index " + i);
        }

        assertEquals(expected.getSubphrases().size(), actual.getSubphrases().size(), context + " - Subphrase count");

        for (int i = 0; i < expected.getSubphrases().size(); i++) {
            assertPhrasesEqual(expected.getSubphrases().get(i), actual.getSubphrases().get(i));
        }
    }
}
