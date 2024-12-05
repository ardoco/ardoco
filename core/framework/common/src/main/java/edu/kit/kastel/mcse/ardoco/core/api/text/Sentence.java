/* Licensed under MIT 2021-2024. */
package edu.kit.kastel.mcse.ardoco.core.api.text;

import java.io.Serializable;

import org.eclipse.collections.api.list.ImmutableList;

/**
 * Represents a sentence within the document.
 */
public interface Sentence extends Serializable {

    /**
     * Returns the sentence number (starting at {@code 0}.
     *
     * @return the sentence number
     */
    int getSentenceNumber();

    /**
     * Return the sentence number used for human readably output. Therefore, this method calculates the sentence number starting with {@code 1}.
     *
     * @return the sentence number starting at one
     */
    default int getSentenceNumberForOutput() {
        return getSentenceNumber() + 1;
    }

    /**
     * Returns the words contained by this sentence
     *
     * @return the words contained by this sentence
     */
    ImmutableList<Word> getWords();

    /**
     * Returns the text of this sentence
     *
     * @return the text of this sentence
     */
    String getText();

    default boolean isEqualTo(Sentence other) {
        return other != null && this.getSentenceNumber() == other.getSentenceNumber() && other.getText().equals(this.getText());
    }

    ImmutableList<Phrase> getPhrases();

    /**
     * Adds a new phrase to the sentence
     *
     * @param phrase the phrase
     */
    void addPhrase(Phrase phrase);
}
