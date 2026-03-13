package io.github.ardoco.core.neo4jschema.entities.inconsistencies;

public interface InconsistencyNodeVisitor <T> {
    T visit(ModelInconsistencyNode node);
    T visit(TextInconsistencyNode node);
}
