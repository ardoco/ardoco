package io.github.ardoco.core.neo4jschema.entities.inconsistencies;

import io.github.ardoco.core.neo4jschema.entities.tracelink.TraceableNode;

import org.springframework.data.neo4j.core.schema.GeneratedValue;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Relationship;
import org.springframework.data.neo4j.core.support.UUIDStringGenerator;

import java.util.Objects;

@Node("Inconsistency")
public abstract class InconsistencyNode {

    @Id
    @GeneratedValue(generatorClass = UUIDStringGenerator.class)
    private String id;

    protected String reason;

    protected String type;

    @Relationship(type = "HAS_INCONSISTENCY", direction = Relationship.Direction.INCOMING)
    protected TraceableNode traceableNode;

    public InconsistencyNode(String reason, String type) {
        this.reason = reason;
        this.type = type;
        traceableNode = null;
    }

    public InconsistencyNode() {
        traceableNode = null;
    }

    public abstract <T> T accept(InconsistencyNodeVisitor<T> visitor);

    public void setTraceableNode(TraceableNode traceableNode) {
        this.traceableNode = traceableNode;
    }

    public TraceableNode getTraceableNode() {
        return traceableNode;
    }

    public String getId() {
        return id;
    }


    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass())
            return false;
        InconsistencyNode that = (InconsistencyNode) o;
        return Objects.equals(reason, that.reason) && Objects.equals(type, that.type);
    }

    @Override
    public int hashCode() {
        return Objects.hash(reason, type);
    }
}
