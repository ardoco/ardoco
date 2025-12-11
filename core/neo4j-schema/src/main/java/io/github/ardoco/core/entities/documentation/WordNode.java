package io.github.ardoco.core.entities.documentation;

import org.springframework.data.neo4j.core.schema.GeneratedValue;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Relationship;
import org.springframework.data.neo4j.core.support.UUIDStringGenerator;

@Node("Word")
public class WordNode {

    @Id @GeneratedValue(generatorClass = UUIDStringGenerator.class)
    private String id;

    private int position; // Global index in text
    private String text;
    private String lemma;
    private String posTag; // Store Enum as String

    // Linked List for Word Order
    @Relationship(type = "NEXT_WORD", direction = Relationship.Direction.OUTGOING)
    private WordNode nextWord;

    public WordNode(int position, String text, String lemma, String posTag) {
        this.position = position;
        this.text = text;
        this.lemma = lemma;
        this.posTag = posTag;
    }

    public WordNode() {}

    public void setNextWord(WordNode nextWord) {
        this.nextWord = nextWord;
    }

    public String getId() {
        return id;
    }

    public int getPosition() {
        return position;
    }

    public String getText() {
        return text;
    }

    public String getLemma() {
        return lemma;
    }

    public String getPosTag() {
        return posTag;
    }

    public WordNode getNextWord() {
        return nextWord;
    }
}
