package io.github.ardoco.core.entities.documentation;

import org.springframework.data.neo4j.core.schema.GeneratedValue;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Relationship;
import org.springframework.data.neo4j.core.support.UUIDStringGenerator;

import java.util.ArrayList;
import java.util.List;

@Node("Sentence")
public class SentenceNode {

    @Id @GeneratedValue(generatorClass = UUIDStringGenerator.class)
    private String id;

    private int sentenceNumber;
    private String text;

    // Ordered sequence of sentences
    @Relationship(type = "NEXT_SENTENCE", direction = Relationship.Direction.OUTGOING)
    private SentenceNode nextSentence;

    // Words contained in this sentence
    @Relationship(type = "CONTAINS_WORD", direction = Relationship.Direction.OUTGOING)
    private List<WordNode> words = new ArrayList<>();

    // Root phrases (Constituency Parse)
    @Relationship(type = "HAS_ROOT_PHRASE", direction = Relationship.Direction.OUTGOING)
    private List<PhraseNode> rootPhrases = new ArrayList<>();

    public SentenceNode(int sentenceNumber, String text) {
        this.sentenceNumber = sentenceNumber;
        this.text = text;
    }

    public SentenceNode() {}


    public void setNextSentence(SentenceNode nextSentence) { this.nextSentence = nextSentence; }
    public List<WordNode> getWords() { return words; }
    public List<PhraseNode> getRootPhrases() { return rootPhrases; }

    public void setSentenceNumber(int sentenceNumber) {
        this.sentenceNumber = sentenceNumber;
    }

    public void setText(String text) {
        this.text = text;
    }

    public void setWords(List<WordNode> words) {
        this.words = words;
    }

    public void setRootPhrases(List<PhraseNode> rootPhrases) {
        this.rootPhrases = rootPhrases;
    }

    public String getId() {
        return id;
    }

    public int getSentenceNumber() {
        return sentenceNumber;
    }

    public String getText() {
        return text;
    }

    public SentenceNode getNextSentence() {
        return nextSentence;
    }
}
