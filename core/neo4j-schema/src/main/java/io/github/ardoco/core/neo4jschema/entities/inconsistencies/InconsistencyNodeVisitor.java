/* Licensed under MIT 2026. */
package io.github.ardoco.core.neo4jschema.entities.inconsistencies;

/**
 * This visistor interface is used to convert InconsistencyNodes to other types.
 * It is used to convert InconsistencyNodes to Inconsistencies from ARDoCo.
 * 
 * @param <T>
 */
public interface InconsistencyNodeVisitor<T> {
    T visit(ModelInconsistencyNode node);

    T visit(TextInconsistencyNode node);
}
