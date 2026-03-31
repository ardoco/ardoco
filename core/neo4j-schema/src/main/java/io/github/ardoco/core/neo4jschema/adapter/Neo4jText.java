/* Licensed under MIT 2026. */
package io.github.ardoco.core.neo4jschema.adapter;

import java.util.List;

import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.impl.factory.Lists;

import edu.kit.kastel.mcse.ardoco.core.api.text.Sentence;
import edu.kit.kastel.mcse.ardoco.core.api.text.Text;
import edu.kit.kastel.mcse.ardoco.core.api.text.Word;

/**
 * This class serves as an adapter to convert Neo4j data into the Text interface expected by the rest of the ARDoCo application.
 */
public class Neo4jText implements Text {

    private final String id;
    private final ImmutableList<Sentence> sentences;
    private final ImmutableList<Word> words;

    public Neo4jText(String id, List<Neo4jSentence> sentences) {
        this.id = id;
        this.sentences = Lists.immutable.withAll(sentences);
        this.words = this.sentences.flatCollect(Sentence::getWords).toImmutable();
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
}
