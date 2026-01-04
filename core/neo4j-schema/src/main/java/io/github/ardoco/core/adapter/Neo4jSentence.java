package io.github.ardoco.core.adapter;



import edu.kit.kastel.mcse.ardoco.core.api.text.Phrase;
import edu.kit.kastel.mcse.ardoco.core.api.text.Sentence;




import edu.kit.kastel.mcse.ardoco.core.api.text.Word;

import io.github.ardoco.core.entities.documentation.SentenceNode;

import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.impl.factory.Lists;
import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;

public class Neo4jSentence implements Sentence {

    private final SentenceNode node;
    private final List<Word> words;
    private final List<Phrase> phrases;

    public Neo4jSentence(SentenceNode node) {
        this.node = node;

        List<Neo4jWord> mutableWords = new ArrayList<>();
        Map<Integer, Neo4jWord> wordMap = new HashMap<>();

        var wordNodes = node.getWords();
        wordNodes.sort((a, b) -> Integer.compare(a.getPosition(), b.getPosition()));

        for (var wn : wordNodes) {
            Neo4jWord word = new Neo4jWord(wn, this);
            mutableWords.add(word);
            wordMap.put(wn.getPosition(), word);
        }

        // Link Words (Previous/Next)
        for (int i = 0; i < mutableWords.size(); i++) {
            Neo4jWord curr = mutableWords.get(i);
            if (i > 0) curr.setPreWord(mutableWords.get(i - 1));
            if (i < mutableWords.size() - 1) curr.setNextWord(mutableWords.get(i + 1));
        }
        this.words = new ArrayList<>(mutableWords);

        List<Phrase> mutablePhrases = new ArrayList<>();
        for (var pn : node.getRootPhrases()) {
            mutablePhrases.add(new Neo4jPhrase(pn, this, wordMap));
        }
        this.phrases = mutablePhrases;
    }

    @Override
    public int getSentenceNumber() {
        return node.getSentenceNumber();
    }


    @Override
    public ImmutableList<Word> getWords() {
        return Lists.immutable.withAll(words);
    }

    @Override
    public String getText() {
        return node.getText();
    }

    @Override
    public boolean isEqualTo(Sentence other) {
        return Sentence.super.isEqualTo(other);
    }

    @Override
    public ImmutableList<Phrase> getPhrases() {
        return Lists.immutable.withAll(phrases);
    }

}
