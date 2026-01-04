package io.github.ardoco.core.adapter;

import edu.kit.kastel.mcse.ardoco.core.api.text.*;

import io.github.ardoco.core.Neo4jPersistenceHandler;
import io.github.ardoco.core.entities.documentation.TextNode;

import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.impl.factory.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.stream.Collectors;

public class Neo4jText implements Text {

    private final ImmutableList<Sentence> sentences;
    private final ImmutableList<Word> words;
    private final String id;

    private static final Logger logger = LoggerFactory.getLogger(Neo4jPersistenceHandler.class);

    public Neo4jText(TextNode textNode) {

        logger.info("Hydrating Text with id: {}", textNode.getArdocoId());
        // Hydrate Sentences
        var sentenceNodes = textNode.getSentences();
        sentenceNodes.sort((a, b) -> Integer.compare(a.getSentenceNumber(), b.getSentenceNumber()));

        var sentenceList = sentenceNodes.stream()
                .map(Neo4jSentence::new)
                .collect(Collectors.toList());

        this.sentences = Lists.immutable.withAll(sentenceList);

        this.words = this.sentences.flatCollect(Sentence::getWords).toImmutable();
        this.id = textNode.getArdocoId();
        logger.info("Hydrated Text with id: {} containing {} sentences and {} words.", this.id, this.sentences.size(), this.words.size());
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
        return words.get(index);
    }

    public String getId() {
        return id;
    }
}
