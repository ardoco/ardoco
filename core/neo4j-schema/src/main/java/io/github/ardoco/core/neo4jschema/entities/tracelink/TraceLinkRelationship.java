package io.github.ardoco.core.neo4jschema.entities.tracelink;

import io.github.ardoco.core.neo4jschema.entities.codeModel.CodeItemNode;

import org.springframework.data.neo4j.core.schema.GeneratedValue;
import org.springframework.data.neo4j.core.schema.RelationshipId;
import org.springframework.data.neo4j.core.schema.RelationshipProperties;
import org.springframework.data.neo4j.core.schema.TargetNode;
import org.springframework.data.neo4j.core.support.UUIDStringGenerator;

@RelationshipProperties
public class TraceLinkRelationship {

    @RelationshipId @GeneratedValue(generatorClass = UUIDStringGenerator.class)
    protected String id;

    private TraceLinkType traceLinkType;
    private Double confidence;

    @TargetNode
    private CodeItemNode targetCodeItem;

    public TraceLinkRelationship() {}

    // Constructor with confidence
    public TraceLinkRelationship(CodeItemNode targetCodeItem, Double confidence, TraceLinkType traceLinkType) {
        this.targetCodeItem = targetCodeItem;
        this.confidence = confidence;
        this.traceLinkType = traceLinkType;
    }

    // Constructor without confidence (defaults to null)
    public TraceLinkRelationship(CodeItemNode targetCodeItem, TraceLinkType traceLinkType) {
        this.targetCodeItem = targetCodeItem;
        this.traceLinkType = traceLinkType;
        this.confidence = null;
    }

    public CodeItemNode getTargetCodeItem() {
        return targetCodeItem;
    }

    // Returns Double (can be null)
    public Double getConfidence() {
        return confidence;
    }

    public TraceLinkType getTraceLinkType() {
        return traceLinkType;
    }
}
