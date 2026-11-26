/* Licensed under MIT 2026. */
package io.github.ardoco.core.neo4jschema.entities.ner;

import org.springframework.data.neo4j.core.schema.Node;

import io.github.ardoco.core.neo4jschema.entities.inconsistencies.ArchitectureType;
import io.github.ardoco.core.neo4jschema.entities.tracelink.TraceableNode;

/**
 * Occurrence of a named architecture entity in SimpleText (sentence / line number).
 * Extends {@link TraceableNode} so NER→model links can use {@code TRACES_TO}.
 */
@Node("NamedArchitectureEntityOccurrence")
public class NamedArchitectureEntityOccurrenceNode extends TraceableNode {

    private String name;
    private int sentenceNumber;

    public NamedArchitectureEntityOccurrenceNode() {
    }

    public NamedArchitectureEntityOccurrenceNode(String ardocoId) {
        super(ardocoId);
    }

    @Override
    public ArchitectureType getModelType() {
        return ArchitectureType.DOCUMENTATION;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getSentenceNumber() {
        return sentenceNumber;
    }

    public void setSentenceNumber(int sentenceNumber) {
        this.sentenceNumber = sentenceNumber;
    }
}
