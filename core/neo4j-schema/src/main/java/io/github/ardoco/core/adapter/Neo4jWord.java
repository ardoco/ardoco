package io.github.ardoco.core.adapter;

import edu.kit.kastel.mcse.ardoco.core.api.text.*;

import io.github.ardoco.core.entities.documentation.WordNode;

import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.impl.factory.Lists;
import java.util.Objects;

public class Neo4jWord implements Word {

    private final WordNode node;
    private final Neo4jSentence parentSentence;
    private Neo4jWord nextWord;
    private Neo4jWord preWord;

    private Phrase phrase;

    public Neo4jWord(WordNode node, Neo4jSentence parentSentence) {
        this.node = node;
        this.parentSentence = parentSentence;
    }

    public void setNextWord(Neo4jWord nextWord) {
        this.nextWord = nextWord;
    }

    public void setPreWord(Neo4jWord preWord) {
        this.preWord = preWord;
    }

    @Override
    public int getSentenceNumber() {
        return parentSentence.getSentenceNumber();
    }

    @Override
    public Sentence getSentence() {
        return parentSentence;
    }

    @Override
    public String getText() {
        return node.getText();
    }

    @Override
    public POSTag getPosTag() {
        return POSTag.get(node.getPosTag());
    }

    @Override
    public Word getPreWord() {
        return preWord;
    }

    @Override
    public Word getNextWord() {
        return nextWord;
    }

    @Override
    public int getPosition() {
        return node.getPosition();
    }

    @Override
    public String getLemma() {
        return node.getLemma();
    }

    @Override
    public Phrase getPhrase() {
        if (this.phrase == null) {
            // Logic to find the deepest phrase containing this word
            this.phrase = findDeepestPhrase(this.parentSentence.getPhrases(), this);
        }
        return this.phrase;
    }

    private Phrase findDeepestPhrase(ImmutableList<Phrase> phrases, Word word) {
        for (Phrase p : phrases) {
            if (p.getContainedWords().contains(word)) {
                // Check children
                Phrase deeper = findDeepestPhrase(p.getSubphrases(), word);
                return deeper != null ? deeper : p;
            }
        }
        return null;
    }


    @Override
    public ImmutableList<Word> getOutgoingDependencyWordsWithType(DependencyTag dependencyTag) {
        return Lists.immutable.empty(); // TODO
    }

    @Override
    public ImmutableList<Word> getIncomingDependencyWordsWithType(DependencyTag dependencyTag) {
        return Lists.immutable.empty(); // TODO
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Word other)) return false;
        return getPosition() == other.getPosition() && getSentenceNumber() == other.getSentenceNumber();
    }

    @Override
    public int hashCode() {
        return Objects.hash(getPosition(), getText(), getPosTag(), getSentenceNumber());
    }

    @Override
    public int compareTo(Word o) {
        return Integer.compare(this.getPosition(), o.getPosition());
    }
}
