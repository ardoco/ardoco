package io.github.ardoco.core.neo4jschema.entities.architectureModel;

import io.github.ardoco.core.neo4jschema.entities.tracelink.TraceLinkRelationship;

import org.springframework.data.neo4j.core.schema.GeneratedValue;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Relationship;
import org.springframework.data.neo4j.core.support.UUIDStringGenerator;

import java.util.HashSet;
import java.util.Set;

@Node("ArchitectureItem")
public abstract class ArchitectureItemNode {

    @Id
    @GeneratedValue(generatorClass = UUIDStringGenerator.class)
    protected String id;

    protected String name;
    protected String ardocoId;

    @Relationship(type = "TRACES_TO_CODE", direction = Relationship.Direction.OUTGOING)
    private Set<TraceLinkRelationship> traceLinks = new HashSet<>();

    public ArchitectureItemNode(String name, String ardocoId) {
        this.name = name;
        this.ardocoId = ardocoId;
    }

    protected ArchitectureItemNode() {}

    public String getId() {
        return id;
    }
    public String getName() {
        return name;
    }
    public String getArdocoId() {
        return ardocoId;
    }

    public void addTraceLink(TraceLinkRelationship link) {
        this.traceLinks.add(link);
    }

    public Set<TraceLinkRelationship> getTraceLinks() {
        return traceLinks;
    }
}
