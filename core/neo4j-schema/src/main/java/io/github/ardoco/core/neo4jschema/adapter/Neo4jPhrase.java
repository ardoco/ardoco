/* Licensed under MIT 2026. */
package io.github.ardoco.core.neo4jschema.adapter;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;

import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.factory.SortedMaps;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.map.sorted.ImmutableSortedMap;

import edu.kit.kastel.mcse.ardoco.core.api.text.*;

/**
 * Adapter class for Phrase entities stored in Neo4j.
 * This class implements the Phrase interface and provides methods to access the properties of a phrase.
 */
public class Neo4jPhrase implements Phrase {

    private final String text;
    private final PhraseType phraseType;
    private final Neo4jSentence parentSentence;
    private final ImmutableList<Word> containedWords;
    private final ImmutableList<Phrase> subPhrases;

    public Neo4jPhrase(String text, String phraseTypeStr, Neo4jSentence parentSentence, List<Neo4jWord> containedWords, List<Phrase> subPhrases) {
        this.text = text;
        this.phraseType = PhraseType.get(phraseTypeStr);
        this.parentSentence = parentSentence;
        this.containedWords = Lists.immutable.withAll(containedWords);
        this.subPhrases = Lists.immutable.withAll(subPhrases);
    }

    @Override
    public int getSentenceNumber() {
        return parentSentence.getSentenceNumber();
    }

    @Override
    public String getText() {
        return text;
    }

    @Override
    public PhraseType getPhraseType() {
        return phraseType;
    }

    @Override
    public ImmutableList<Word> getContainedWords() {
        return containedWords;
    }

    @Override
    public ImmutableList<Phrase> getSubphrases() {
        return subPhrases;
    }

    @Override
    public boolean isSuperphraseOf(Phrase other) {
        return this.getContainedWords().containsAll((Collection<?>) other.getContainedWords()) && this.getContainedWords().size() > other.getContainedWords()
                .size();
    }

    @Override
    public boolean isSubphraseOf(Phrase other) {
        return other.getContainedWords().containsAll((Collection<?>) this.getContainedWords()) && other.getContainedWords().size() > this.getContainedWords()
                .size();
    }

    @Override
    public ImmutableSortedMap<Word, Integer> getPhraseVector() {
        var map = SortedMaps.mutable.<Word, Integer>empty();
        var grouped = getContainedWords().groupBy(Word::getText).toMap();
        grouped.forEach((key, value) -> map.put(value.getAny(), value.size()));
        return map.toImmutable();
    }

    @Override
    public int compareTo(Phrase o) {
        if (this == o)
            return 0;
        return Comparator.comparing(Phrase::getSentenceNumber).thenComparing(Phrase::getText).thenComparing(Phrase::getPhraseType).compare(this, o);
    }
}
