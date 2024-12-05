/* Licensed under MIT 2023-2024. */
package edu.kit.kastel.mcse.ardoco.tlr.textextraction;

import java.util.Comparator;
import java.util.Objects;
import java.util.stream.Collectors;

import org.eclipse.collections.api.factory.SortedMaps;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.list.MutableList;
import org.eclipse.collections.api.map.sorted.ImmutableSortedMap;
import org.eclipse.collections.api.map.sorted.MutableSortedMap;
import org.eclipse.collections.impl.factory.Lists;

import edu.kit.kastel.mcse.ardoco.core.api.text.Phrase;
import edu.kit.kastel.mcse.ardoco.core.api.text.PhraseType;
import edu.kit.kastel.mcse.ardoco.core.api.text.Sentence;
import edu.kit.kastel.mcse.ardoco.core.api.text.Word;

/**
 * Phrase implementation for a phrase that is derived from context rather than text processing.
 */
public class ContextPhrase implements Phrase {
    private final MutableList<Word> words;
    private final Sentence sentence;

    /**
     * Creates a new phrase consisting of the provided words in the provided sentence.
     *
     * @param words    the words
     * @param sentence the sentence
     */
    public ContextPhrase(ImmutableList<Word> words, Sentence sentence) {
        this.words = Lists.mutable.ofAll(words);
        this.sentence = sentence;
    }

    @Override
    public int getSentenceNo() {
        return sentence.getSentenceNumber();
    }

    @Override
    public String getText() {
        return words.stream().map(Word::getText).collect(Collectors.joining(" "));
    }

    @Override
    public PhraseType getPhraseType() {
        return PhraseType.NP;
    }

    @Override
    public ImmutableList<Word> getContainedWords() {
        return words.toImmutableList();
    }

    @Override
    public ImmutableList<Phrase> getSubPhrases() {
        return Lists.immutable.empty();
    }

    @Override
    public boolean isSuperPhraseOf(Phrase other) {
        var currText = getText();
        var otherText = other.getText();
        return currText.contains(otherText) && currText.length() != otherText.length();
    }

    @Override
    public boolean isSubPhraseOf(Phrase other) {
        var currText = getText();
        var otherText = other.getText();
        return otherText.contains(currText) && currText.length() != otherText.length();
    }

    @Override
    public ImmutableSortedMap<Word, Integer> getPhraseVector() {
        MutableSortedMap<Word, Integer> phraseVector = SortedMaps.mutable.empty();

        getContainedWords().groupBy(Word::getText).forEachKeyImmutableList((text, listOfWords) -> phraseVector.put(listOfWords.getAny(), listOfWords.size()));

        return phraseVector.toImmutable();
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.getSentenceNo(), this.getText(), this.getPhraseType(), this.getContainedWords().get(0).getPosition());
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!(obj instanceof Phrase other))
            return false;
        return this.getSentenceNo() == other.getSentenceNo() && Objects.equals(this.getText(), other.getText()) && Objects.equals(this.getPhraseType(), other
                .getPhraseType()) && this.getContainedWords().get(0).getPosition() == other.getContainedWords().get(0).getPosition();
    }

    @Override
    public String toString() {
        return "Phrase{" + "text='" + getText() + '\'' + '}';
    }

    @Override
    public int compareTo(Phrase o) {
        return Comparator.comparing(Phrase::getSentenceNo)
                .thenComparing(Phrase::getText)
                .thenComparing(Phrase::getPhraseType)
                .thenComparingInt(p -> p.getContainedWords().get(0).getPosition())
                .compare(this, o);
    }
}
