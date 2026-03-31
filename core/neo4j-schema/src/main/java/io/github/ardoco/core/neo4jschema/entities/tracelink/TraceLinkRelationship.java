/* Licensed under MIT 2026. */
package io.github.ardoco.core.neo4jschema.entities.tracelink;

import org.springframework.data.neo4j.core.schema.GeneratedValue;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.RelationshipProperties;
import org.springframework.data.neo4j.core.schema.TargetNode;

@RelationshipProperties
public class TraceLinkRelationship {

    @Id
    @GeneratedValue
    protected String id;

    private TraceLinkType traceLinkType;
    private Double confidence;

    @TargetNode
    private TraceableNode targetNode;

    public TraceLinkRelationship() {
    }

        /**
        * Creates a TraceLinkRelationship with the specified target node, confidence value, and trace link type.
        *
        * @param targetNode the target node of the trace link relationship
        * @param confidence the confidence value associated with the trace link relationship
        * @param traceLinkType the type of the trace link relationship
        */
    public TraceLinkRelationship(TraceableNode targetNode, Double confidence, TraceLinkType traceLinkType) {
        this.targetNode = targetNode;
        this.confidence = confidence;
        this.traceLinkType = traceLinkType;
    }


    /**
     * Creates a TraceLinkRelationship with the specified target node and trace link type, without setting a confidence value.
     * The confidence will be set to null, indicating that it is not defined.
     *
     * @param targetNode the target node of the trace link relationship
     * @param traceLinkType the type of the trace link relationship
     */
    public TraceLinkRelationship(TraceableNode targetNode, TraceLinkType traceLinkType) {
        this.targetNode = targetNode;
        this.traceLinkType = traceLinkType;
        this.confidence = null;
    }

    public TraceableNode getTargetNode() {
        return this.targetNode;
    }


    public Double getConfidence() {
        return confidence;
    }

    public TraceLinkType getTraceLinkType() {
        return traceLinkType;
    }
}
