package io.github.ardoco.core.adapter;

import edu.kit.kastel.mcse.ardoco.core.api.text.Sentence;
import edu.kit.kastel.mcse.ardoco.core.api.text.Text;
import edu.kit.kastel.mcse.ardoco.core.api.text.Word;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.impl.factory.Lists;

import java.util.List;

public class Neo4jText implements Text {

    private final String id; // kept just in case
    private final ImmutableList<Sentence> sentences;
    private final ImmutableList<Word> words;

    // Constructor is now clean and data-driven
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
