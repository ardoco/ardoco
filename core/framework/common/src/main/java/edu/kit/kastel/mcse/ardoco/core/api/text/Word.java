/* Licensed under MIT 2021-2024. */
package edu.kit.kastel.mcse.ardoco.core.api.text;

import java.io.Serializable;

import org.eclipse.collections.api.list.ImmutableList;

/**
 * The Interface IWord defines a word in a text.
 */
public interface Word extends Comparable<Word>, Serializable {

    /**
     * Gets the sentence number starting at 0.
     *
     * @return the sentence number
     */
    int getSentenceNo();

    /**
     * Return the sentence the word is contained in
     *
     * @return the sentence the word is contained in
     */
    Sentence getSentence();

    /**
     * Gets the text representation of the word.
     *
     * @return the text
     */
    String getText();

    /**
     * Gets the pos tag.
     *
     * @return the pos tag
     */
    POSTag getPosTag();

    /**
     * Gets the previous word.
     *
     * @return the previous word
     */
    Word getPreWord();

    /**
     * Gets the next word.
     *
     * @return the next word
     */
    Word getNextWord();

    /**
     * FIXME This description is confusing. Is this relative to the sentence or relative to the entire text?
     * Gets the position in the sentence / text.
     *
     * @return the position
     */
    int getPosition();

    /**
     * Gets the lemmatized version of the word.
     *
     * @return the lemma
     */
    String getLemma();

    /**
     * Gets the words that are dependency of this.
     *
     * @param dependencyTag the dependency tag
     * @return the words that are dependency of this
     */
    ImmutableList<Word> getOutgoingDependencyWordsWithType(DependencyTag dependencyTag);

    /**
     * Gets the words that are dependent on this.
     *
     * @param dependencyTag the dependency tag
     * @return the words that are dependent on this
     */
    ImmutableList<Word> getIncomingDependencyWordsWithType(DependencyTag dependencyTag);

    Phrase getPhrase();

    @Override
    default int compareTo(Word o) {
        if (this.equals(o))
            return 0;

        int compareSentences = Integer.compare(this.getSentenceNo(), o.getSentenceNo());
        if (compareSentences != 0) {
            return compareSentences;
        }
        return Integer.compare(this.getPosition(), o.getPosition());
    }
}
