/* Licensed under MIT 2026. */
package io.github.ardoco.core.neo4jschema.entities.architectureModel;

import java.util.HashSet;
import java.util.Set;

import io.github.ardoco.core.neo4jschema.entities.inconsistencies.ArchitectureType;
import io.github.ardoco.core.neo4jschema.entities.tracelink.TraceableNode;

import org.springframework.data.neo4j.core.schema.GeneratedValue;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Relationship;
import org.springframework.data.neo4j.core.support.UUIDStringGenerator;

import io.github.ardoco.core.neo4jschema.entities.tracelink.TraceLinkRelationship;

@Node("ArchitectureItem")
public abstract class ArchitectureItemNode extends TraceableNode {

//    @Id
//    @GeneratedValue(generatorClass = UUIDStringGenerator.class)
//    protected String id;

    protected String name;

//    @Id
//    @GeneratedValue(generatorClass = UUIDStringGenerator.class)
//    protected String ardocoId;

//    @Relationship(type = "TRACES_TO_CODE", direction = Relationship.Direction.OUTGOING)
//    private Set<TraceLinkRelationship> traceLinks = new HashSet<>();

    public ArchitectureItemNode(String name, String ardocoId) {
        super(ardocoId);
        this.name = name;
//        this.ardocoId = ardocoId;
    }

    protected ArchitectureItemNode() {
    }

//    public String getId() {
//        return id;
//    }

    public String getName() {
        return name;
    }

    public String getArdocoId() {
        return ardocoId;
    }

    @Override
    public ArchitectureType getModelType() {
        return ArchitectureType.ARCHITECTURE;
    }

//    public void addTraceLink(TraceLinkRelationship link) {
//        this.traceLinks.add(link);
//    }
//
//    public Set<TraceLinkRelationship> getTraceLinks() {
//        return traceLinks;
//    }
}
