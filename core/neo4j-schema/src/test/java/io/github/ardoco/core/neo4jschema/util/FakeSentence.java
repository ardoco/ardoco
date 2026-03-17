package io.github.ardoco.core.neo4jschema.util;

import edu.kit.kastel.mcse.ardoco.core.api.text.Sentence;

import org.eclipse.collections.api.factory.Lists;

public class FakeSentence implements Sentence {

    int sentenceNumber;
    String text;

    public FakeSentence(int sentenceNumber, String text) {
        this.sentenceNumber = sentenceNumber;
        this.text = text;
    }

    @Override public int getSentenceNumber() {
        return this.sentenceNumber;
    }
    @Override public String getText() {
        return text;
    }
    @Override public org.eclipse.collections.api.list.ImmutableList<edu.kit.kastel.mcse.ardoco.core.api.text.Word> getWords() {
        return Lists.immutable.empty();
    }
    @Override public org.eclipse.collections.api.list.ImmutableList<edu.kit.kastel.mcse.ardoco.core.api.text.Phrase> getPhrases() {
        return Lists.immutable.empty();
    }

}
