package io.github.ardoco.core.neo4jschema.entities.tracelink;

import edu.kit.kastel.mcse.ardoco.core.api.entity.ModelEntity;
import io.github.ardoco.core.neo4jschema.entities.inconsistencies.InconsistencyNode;

import io.github.ardoco.core.neo4jschema.entities.inconsistencies.ArchitectureType;

import org.springframework.data.annotation.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Relationship;

import java.util.HashSet;
import java.util.Set;

@Node("Traceable")
public abstract class TraceableNode {

    @Id
    protected String ardocoId;

    // Outgoing links: "I point to someone else"
    @Relationship(type = "TRACES_TO", direction = Relationship.Direction.OUTGOING)
    protected Set<TraceLinkRelationship> outgoingLinks = new HashSet<>();

    // Incoming links: "Someone else points to me"
    @Relationship(type = "TRACES_TO", direction = Relationship.Direction.INCOMING)
    protected Set<TraceLinkRelationship> incomingLinks = new HashSet<>();

    @Relationship(type = "HAS_INCONSISTENCY", direction = Relationship.Direction.OUTGOING)
    protected Set<InconsistencyNode> inconsistencies = new HashSet<>();

    protected TraceableNode () {
    }

    protected TraceableNode(String ardocoId) {
        this.ardocoId = ardocoId;
    }


    public void addOutgoingTraceLink(TraceLinkRelationship link) {
        this.outgoingLinks.add(link);
    }

    public Set<TraceLinkRelationship> getIncomingLinks() {
        return outgoingLinks;
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
