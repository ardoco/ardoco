/* Licensed under MIT 2026. */
package io.github.ardoco.core.neo4jschema.entities.tracelink;

import java.util.HashSet;
import java.util.Set;

import org.springframework.data.annotation.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Relationship;

import io.github.ardoco.core.neo4jschema.entities.inconsistencies.ArchitectureType;
import io.github.ardoco.core.neo4jschema.entities.inconsistencies.InconsistencyNode;

@Node("Traceable")
public abstract class TraceableNode {

    @Id
    protected String ardocoId;

    @Relationship(type = "TRACES_TO", direction = Relationship.Direction.OUTGOING)
    protected Set<TraceLinkRelationship> outgoingLinks = new HashSet<>();

    @Relationship(type = "HAS_INCONSISTENCY", direction = Relationship.Direction.OUTGOING)
    protected Set<InconsistencyNode> inconsistencies = new HashSet<>();

    protected TraceableNode() {
    }

    protected TraceableNode(String ardocoId) {
        this.ardocoId = ardocoId;
    }

    public void addOutgoingTraceLink(TraceLinkRelationship link) {
        this.outgoingLinks.add(link);
    }

    public Set<TraceLinkRelationship> getOutgoingLinks() {
        return outgoingLinks;
    }

    public Set<InconsistencyNode> getInconsistencies() {
        return inconsistencies;
    }

    public void addInconsistency(InconsistencyNode inconsistency) {
        this.inconsistencies.add(inconsistency);
    }

    public abstract ArchitectureType getModelType();

    public void setArdocoId(String ardocoId) {
        this.ardocoId = ardocoId;
    }

    public String getArdocoId() {
        return ardocoId;
    }

}
