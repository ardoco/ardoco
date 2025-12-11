package io.github.ardoco.core.adapter;

import edu.kit.kastel.mcse.ardoco.core.api.text.*;
import io.github.ardoco.core.entities.documentation.PhraseNode;

import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.map.sorted.ImmutableSortedMap;
import org.eclipse.collections.api.factory.SortedMaps;

import edu.kit.kastel.mcse.ardoco.core.api.text.Phrase;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Neo4jPhrase implements Phrase {

    private final PhraseNode node;
    private final Neo4jSentence parentSentence;
    private final ImmutableList<Word> containedWords;
    private final ImmutableList<Phrase> subPhrases;

    public Neo4jPhrase(PhraseNode node, Neo4jSentence parentSentence, Map<Integer, Neo4jWord> wordMap) {
        this.node = node;
        this.parentSentence = parentSentence;

        // Restore Words: map the IDs/Positions in PhraseNode to the actual Neo4jWord objects
        // stored in the context
        List<Word> words = node.getContainedWords().stream()
                .map(wn -> wordMap.get(wn.getPosition()))
                .collect(Collectors.toList());
        this.containedWords = Lists.immutable.withAll(words);

        // Restore Subphrases recursively
        List<Phrase> subs = node.getChildPhrases().stream()
                .map(childNode -> new Neo4jPhrase(childNode, parentSentence, wordMap))
                .collect(Collectors.toList());
        this.subPhrases = Lists.immutable.withAll(subs);
    }

    @Override
    public int getSentenceNumber() {
        return parentSentence.getSentenceNumber();
    }


    @Override
    public String getText() {
        return node.getText();
    }

    @Override
    public PhraseType getPhraseType() {
        return PhraseType.get(node.getPhraseType());
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
        // Simple containment logic based on words
        return this.getContainedWords().containsAll((Collection<?>) other.getContainedWords())
                && this.getContainedWords().size() > other.getContainedWords().size();
    }


    @Override
    public boolean isSubphraseOf(Phrase other) {
        return other.getContainedWords().containsAll((Collection<?>) this.getContainedWords())
                && other.getContainedWords().size() > this.getContainedWords().size();
    }

    @Override
    public ImmutableSortedMap<Word, Integer> getPhraseVector() {
        // Re-implementing logic from PhraseImpl
        var map = SortedMaps.mutable.<Word, Integer>empty();
        var grouped = getContainedWords().groupBy(Word::getText).toMap();
        grouped.forEach((key, value) -> map.put(value.getAny(), value.size()));
        return map.toImmutable();
    }

    @Override
    public int compareTo(Phrase o) {
        if (this == o)
            return 0;
        return Comparator.comparing(Phrase::getSentenceNumber)
                .thenComparing(Phrase::getText)
                .thenComparing(Phrase::getPhraseType)
                .thenComparingInt(p -> p.getContainedWords().get(0).getPosition())
                .compare(this, o);
    }
}
