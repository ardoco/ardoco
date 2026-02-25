/* Licensed under MIT 2026. */
package io.github.ardoco.core.neo4jschema.entities.documentation;

import java.util.ArrayList;
import java.util.List;

import io.github.ardoco.core.neo4jschema.entities.tracelink.TraceableNode;

import org.springframework.data.neo4j.core.schema.GeneratedValue;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Relationship;
import org.springframework.data.neo4j.core.support.UUIDStringGenerator;

@Node("Sentence")
public class SentenceNode extends TraceableNode {

//    @Id
//    @GeneratedValue(generatorClass = UUIDStringGenerator.class)
//    private String id;

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
        super(java.util.UUID.randomUUID().toString()); // generate a unique ardocoId which will be used as the unique identifier in neo4j
        this.sentenceNumber = sentenceNumber;
        this.text = text;
    }

    public SentenceNode() {
    }

    public void setNextSentence(SentenceNode nextSentence) {
        this.nextSentence = nextSentence;
    }

    public List<WordNode> getWords() {
        return words;
    }

    public List<PhraseNode> getRootPhrases() {
        return rootPhrases;
    }

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
        return ardocoId;
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
