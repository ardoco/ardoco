package io.github.ardoco.core.adapter;

import edu.kit.kastel.mcse.ardoco.core.api.text.*;

import io.github.ardoco.core.entities.documentation.TextNode;

import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.impl.factory.Lists;
import java.util.stream.Collectors;

public class Neo4jText implements Text {

    private final ImmutableList<Sentence> sentences;
    private final ImmutableList<Word> words;
    private final String id;

    public Neo4jText(TextNode textNode) {
        // Hydrate Sentences
        // Ensure sentences are sorted by number
        var sentenceNodes = textNode.getSentences();
        sentenceNodes.sort((a, b) -> Integer.compare(a.getSentenceNumber(), b.getSentenceNumber()));

        var sentenceList = sentenceNodes.stream()
                .map(Neo4jSentence::new)
                .collect(Collectors.toList());

        this.sentences = Lists.immutable.withAll(sentenceList);

        // Flatten words list for easier access
        this.words = this.sentences.flatCollect(Sentence::getWords).toImmutable();
        this.id = textNode.getArdocoId();
    }

    @Override
    public ImmutableList<Word> words() {
        return words;
    }

    @Override
    public ImmutableList<Sentence> getSentences() {
        return sentences;
    }

    @Override
    public Word getWord(int index) {
        // Assuming global index is preserved or position matches list index
        return words.get(index);
    }

    public String getId() {
        return id;
    }
}
