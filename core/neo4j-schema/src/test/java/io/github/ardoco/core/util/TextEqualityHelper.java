package io.github.ardoco.core.util;

import edu.kit.kastel.mcse.ardoco.core.api.text.DependencyTag;
import edu.kit.kastel.mcse.ardoco.core.api.text.Phrase;
import edu.kit.kastel.mcse.ardoco.core.api.text.Sentence;
import edu.kit.kastel.mcse.ardoco.core.api.text.Text;
import edu.kit.kastel.mcse.ardoco.core.api.text.Word;
import org.eclipse.collections.api.list.ImmutableList;

import java.util.Objects;

public class TextEqualityHelper {

    /**
     * Deep compares two Text objects.
     * Checks Sentences, Words (including dependencies), and Phrases.
     *
     * @param textA The original text
     * @param textB The restored text
     * @return true if identical
     */
    public static boolean areTextsEqual(Text textA, Text textB) {
        if (textA == textB) return true;
        if (textA == null || textB == null) return false;

        if (textA.getSentences().size() != textB.getSentences().size()) {
            System.err.println("Sentence count mismatch: " + textA.getSentences().size() + " vs " + textB.getSentences().size());
            return false;
        }

        for (int i = 0; i < textA.getSentences().size(); i++) {
            Sentence sA = textA.getSentences().get(i);
            Sentence sB = textB.getSentences().get(i);

            if (!areSentencesEqual(sA, sB)) {
                System.err.println("Sentence mismatch at index " + i);
                return false;
            }
        }

        return true;
    }

    private static boolean areSentencesEqual(Sentence a, Sentence b) {
        if (a.getSentenceNumber() != b.getSentenceNumber()) return false;
        if (!Objects.equals(a.getText(), b.getText())) {
            System.err.println("Sentence text mismatch: [" + a.getText() + "] vs [" + b.getText() + "]");
            return false;
        }

        if (a.getWords().size() != b.getWords().size()) return false;
        for (int i = 0; i < a.getWords().size(); i++) {
            if (!areWordsEqual(a.getWords().get(i), b.getWords().get(i))) {
                System.err.println("Word mismatch at index " + i + " in sentence " + a.getSentenceNumber());
                return false;
            }
        }

        if (a.getPhrases().size() != b.getPhrases().size()) return false;
        for (int i = 0; i < a.getPhrases().size(); i++) {
            if (!arePhrasesEqual(a.getPhrases().get(i), b.getPhrases().get(i))) {
                System.err.println("Phrase mismatch at index " + i + " in sentence " + a.getSentenceNumber());
                return false;
            }
        }
        return true;
    }

    private static boolean areWordsEqual(Word a, Word b) {
        if (a.getPosition() != b.getPosition()) return false;
        if (a.getSentenceNumber() != b.getSentenceNumber()) return false;
        if (!Objects.equals(a.getText(), b.getText())) return false;
        if (!Objects.equals(a.getLemma(), b.getLemma())) return false;
        if (a.getPosTag() != b.getPosTag()) return false;

        for (DependencyTag tag : DependencyTag.values()) {
            ImmutableList<Word> outA = a.getOutgoingDependencyWordsWithType(tag);
            ImmutableList<Word> outB = b.getOutgoingDependencyWordsWithType(tag);

            if (outA.size() != outB.size()) {
                System.err.println("Dependency count mismatch for tag " + tag + " on word " + a.getText());
                return false;
            }

            for (int k = 0; k < outA.size(); k++) {
                if (outA.get(k).getPosition() != outB.get(k).getPosition()) {
                    System.err.println("Dependency target mismatch for tag " + tag);
                    return false;
                }
            }
        }
        return true;
    }

    private static boolean arePhrasesEqual(Phrase a, Phrase b) {
        if (a.getPhraseType() != b.getPhraseType()) return false;
        if (!Objects.equals(a.getText(), b.getText())) return false;

        if (a.getContainedWords().size() != b.getContainedWords().size()) return false;
        for (int i = 0; i < a.getContainedWords().size(); i++) {
            if (a.getContainedWords().get(i).getPosition() != b.getContainedWords().get(i).getPosition()) {
                return false;
            }
        }

        if (a.getSubphrases().size() != b.getSubphrases().size()) return false;
        for (int i = 0; i < a.getSubphrases().size(); i++) {
            if (!arePhrasesEqual(a.getSubphrases().get(i), b.getSubphrases().get(i))) {
                return false;
            }
        }

        return true;
    }
}
