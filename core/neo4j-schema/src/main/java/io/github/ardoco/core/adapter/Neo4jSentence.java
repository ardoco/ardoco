package io.github.ardoco.core.adapter;

import edu.kit.kastel.mcse.ardoco.core.api.text.Phrase;
import edu.kit.kastel.mcse.ardoco.core.api.text.Sentence;
import edu.kit.kastel.mcse.ardoco.core.api.text.Word;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.impl.factory.Lists;

import java.util.List;
import java.util.Objects;

public class Neo4jSentence implements Sentence {

    private final int sentenceNumber;
    private final String text;

    // Mutable during mapping phase, finalized via setters
    private List<Neo4jWord> words;
    private List<Phrase> phrases;

    public Neo4jSentence(int sentenceNumber, String text) {
        this.sentenceNumber = sentenceNumber;
        this.text = text;
    }

    // Setters used by Mapper
    public void setWords(List<Neo4jWord> words) {
        this.words = words;
    }

    public void setPhrases(List<Phrase> phrases) {
        this.phrases = phrases;
    }

    @Override
    public int getSentenceNumber() {
        return sentenceNumber;
    }

    @Override
    public ImmutableList<Word> getWords() {
        return Lists.immutable.withAll(words);
    }

    @Override
    public String getText() {
        return text;
    }

    @Override
    public ImmutableList<Phrase> getPhrases() {
        return Lists.immutable.withAll(phrases);
    }

    // Equals and HashCode implementations...
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Sentence s)) return false;
        return sentenceNumber == s.getSentenceNumber() && Objects.equals(text, s.getText());
    }

    @Override
    public int hashCode() {
        return Objects.hash(sentenceNumber, text);
    }
}
