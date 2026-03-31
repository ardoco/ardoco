/* Licensed under MIT 2026. */
package io.github.ardoco.core.neo4jschema.adapter;

import java.util.List;
import java.util.Objects;

import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.impl.factory.Lists;

import edu.kit.kastel.mcse.ardoco.core.api.text.Phrase;
import edu.kit.kastel.mcse.ardoco.core.api.text.Sentence;
import edu.kit.kastel.mcse.ardoco.core.api.text.Word;

/**
 * This class represents a Sentence in the Neo4j schema. It implements the Sentence interface from the core API and is used to map data from the Neo4j database
 * to the domain model.
 */
public class Neo4jSentence implements Sentence {

    private final int sentenceNumber;
    private final String text;

    private List<Neo4jWord> words;
    private List<Phrase> phrases;

    public Neo4jSentence(int sentenceNumber, String text) {
        this.sentenceNumber = sentenceNumber;
        this.text = text;
    }

    @Override
    public int getSentenceNumber() {
        return sentenceNumber;
    }

    @Override
    public ImmutableList<Word> getWords() {
        return Lists.immutable.withAll(words);
    }

    public void setWords(List<Neo4jWord> words) {
        this.words = words;
    }

    @Override
    public String getText() {
        return text;
    }

    @Override
    public ImmutableList<Phrase> getPhrases() {
        return Lists.immutable.withAll(phrases);
    }

    public void setPhrases(List<Phrase> phrases) {
        this.phrases = phrases;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof Sentence s))
            return false;
        return sentenceNumber == s.getSentenceNumber() && Objects.equals(text, s.getText());
    }

    @Override
    public int hashCode() {
        return Objects.hash(sentenceNumber, text);
    }
}
