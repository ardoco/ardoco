package io.github.ardoco.core.entities.documentation;

import org.springframework.data.neo4j.core.schema.GeneratedValue;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Relationship;
import org.springframework.data.neo4j.core.support.UUIDStringGenerator;

import java.util.ArrayList;
import java.util.List;

@Node("Phrase")
public class PhraseNode {

    @Id @GeneratedValue(generatorClass = UUIDStringGenerator.class)
    private String id;

    public PhraseNode() {}

    private String text;
    private String phraseType; // e.g., NP, VP


    @Relationship(type = "HAS_CHILD_PHRASE", direction = Relationship.Direction.OUTGOING)
    private List<PhraseNode> childPhrases = new ArrayList<>();


    @Relationship(type = "CONTAINS_WORD", direction = Relationship.Direction.OUTGOING)
    private List<WordNode> containedWords = new ArrayList<>();

    public PhraseNode(String text, String phraseType) {
        this.text = text;
        this.phraseType = phraseType;
    }

    public void addChildPhrase(PhraseNode phrase) {
        this.childPhrases.add(phrase);
    }

    public void addContainedWord(WordNode word) {
        this.containedWords.add(word);
    }

    public String getId() {
        return id;
    }

    public String getText() {
        return text;
    }

    public String getPhraseType() {
        return phraseType;
    }

    public List<PhraseNode> getChildPhrases() {
        return childPhrases;
    }

    public List<WordNode> getContainedWords() {
        return containedWords;
    }
}
