/* Licensed under MIT 2026. */
package io.github.ardoco.core.neo4jschema.entities.documentation;

import java.util.ArrayList;
import java.util.List;

import io.github.ardoco.core.neo4jschema.entities.inconsistencies.ArchitectureType;
import io.github.ardoco.core.neo4jschema.entities.tracelink.TraceableNode;

import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Relationship;

@Node("Sentence")
public class SentenceNode extends TraceableNode {

    private int sentenceNumber;
    private String text;

    @Relationship(type = "NEXT_SENTENCE", direction = Relationship.Direction.OUTGOING)
    private SentenceNode nextSentence;

    // Words contained in this sentence
    @Relationship(type = "CONTAINS_WORD", direction = Relationship.Direction.OUTGOING)
    private List<WordNode> words = new ArrayList<>();

    @Relationship(type = "HAS_ROOT_PHRASE", direction = Relationship.Direction.OUTGOING)
    private List<PhraseNode> rootPhrases = new ArrayList<>();

    public SentenceNode(int sentenceNumber, String text) {
        super(String.valueOf(sentenceNumber) + text.hashCode());
        this.sentenceNumber = sentenceNumber;
        this.text = text;
    }

    public SentenceNode() {
    }

    @Override
    public ArchitectureType getModelType() {
        return ArchitectureType.DOCUMENTATION;
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
