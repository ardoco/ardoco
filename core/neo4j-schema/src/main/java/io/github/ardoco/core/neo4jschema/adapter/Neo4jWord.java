/* Licensed under MIT 2026. */
package io.github.ardoco.core.neo4jschema.adapter;

import java.util.Objects;

import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.multimap.list.MutableListMultimap;
import org.eclipse.collections.impl.factory.Multimaps;

import edu.kit.kastel.mcse.ardoco.core.api.text.DependencyTag;
import edu.kit.kastel.mcse.ardoco.core.api.text.POSTag;
import edu.kit.kastel.mcse.ardoco.core.api.text.Phrase;
import edu.kit.kastel.mcse.ardoco.core.api.text.Sentence;
import edu.kit.kastel.mcse.ardoco.core.api.text.Word;

/**
 * Adapter class that implements the Word interface and is backed by Neo4j data.
 */
public class Neo4jWord implements Word {

    private final int position;
    private final String text;
    private final String lemma;
    private final POSTag posTag;
    private final Neo4jSentence parentSentence;
    private final MutableListMultimap<DependencyTag, Word> outgoingDependencies = Multimaps.mutable.list.empty();
    private final MutableListMultimap<DependencyTag, Word> incomingDependencies = Multimaps.mutable.list.empty();
    private Neo4jWord nextWord;
    private Neo4jWord preWord;
    private Phrase phrase;

    public Neo4jWord(int position, String text, String lemma, String posTagStr, Neo4jSentence parentSentence) {
        this.position = position;
        this.text = text;
        this.lemma = lemma;
        this.posTag = POSTag.get(posTagStr);
        this.parentSentence = parentSentence;
    }

    public void addOutgoingDependency(DependencyTag tag, Neo4jWord target) {
        this.outgoingDependencies.put(tag, target);
    }

    public void addIncomingDependency(DependencyTag tag, Neo4jWord source) {
        this.incomingDependencies.put(tag, source);
    }

    @Override
    public int getPosition() {
        return position;
    }

    @Override
    public String getText() {
        return text;
    }

    @Override
    public String getLemma() {
        return lemma;
    }

    @Override
    public POSTag getPosTag() {
        return posTag;
    }

    @Override
    public Sentence getSentence() {
        return parentSentence;
    }

    @Override
    public int getSentenceNumber() {
        return parentSentence.getSentenceNumber();
    }

    @Override
    public Word getPreWord() {
        return preWord;
    }

    public void setPreWord(Neo4jWord preWord) {
        this.preWord = preWord;
    }

    @Override
    public Word getNextWord() {
        return nextWord;
    }

    public void setNextWord(Neo4jWord nextWord) {
        this.nextWord = nextWord;
    }

    @Override
    public Phrase getPhrase() {
        if (this.phrase == null) {
            this.phrase = findDeepestPhrase(parentSentence.getPhrases(), this);
        }
        return this.phrase;
    }

    private Phrase findDeepestPhrase(ImmutableList<Phrase> phrases, Word word) {
        for (Phrase p : phrases) {
            if (p.getContainedWords().contains(word)) {
                Phrase deeper = findDeepestPhrase(p.getSubphrases(), word);
                return deeper != null ? deeper : p;
            }
        }
        return null;
    }

    @Override
    public ImmutableList<Word> getOutgoingDependencyWordsWithType(DependencyTag dependencyTag) {
        return this.outgoingDependencies.get(dependencyTag).toImmutable();
    }

    @Override
    public ImmutableList<Word> getIncomingDependencyWordsWithType(DependencyTag dependencyTag) {
        return this.incomingDependencies.get(dependencyTag).toImmutable();
    }

    @Override
    public int compareTo(Word o) {
        if (this.equals(o))
            return 0;
        int s = Integer.compare(this.getSentenceNumber(), o.getSentenceNumber());
        return (s != 0) ? s : Integer.compare(this.position, o.getPosition());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof Word w))
            return false;
        return position == w.getPosition() && getSentenceNumber() == w.getSentenceNumber();
    }

    @Override
    public int hashCode() {
        return Objects.hash(position, text, posTag, getSentenceNumber());
    }
}
