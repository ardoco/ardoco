package io.github.ardoco.core.neo4jschema.entities.inconsistencies;

import org.springframework.data.neo4j.core.schema.Node;

@Node("TextInconsistency")
public class TextInconsistencyNode extends InconsistencyNode {
    private int sentenceNumber;

    private double confidence;

    private String name;

    public TextInconsistencyNode(String name, int sentenceNumber, double confidence, String reason, String type) {
        super(reason, type);
        this.sentenceNumber = sentenceNumber;
        this.name = name;
        this.confidence = confidence;
    }

    public TextInconsistencyNode() {
    }

    @Override
    public <T> T accept(InconsistencyNodeVisitor<T> visitor) {
        return visitor.visit(this);
    }

    public int getSentenceNumber() {
        return sentenceNumber;
    }

    public void setSentenceNumber(int sentenceNumber) {
        this.sentenceNumber = sentenceNumber;
    }

    public double getConfidence() {
        return confidence;
    }

    public void setConfidence(double confidence) {
        this.confidence = confidence;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
