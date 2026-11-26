/* Licensed under MIT 2026. */
package io.github.ardoco.core.neo4jschema.entities.tracelink;

import io.github.ardoco.core.neo4jschema.entities.documentation.SentenceNode;

/**
 * One sentence-architecture-code chain returned by the transitive trace link query.
 *
 * @param sentence     the start node
 * @param architecture the architecture node in the middle
 * @param code         the code node at the end
 */
public record TransitiveChainQueryResult(SentenceNode sentence, TraceableNode architecture, TraceableNode code) {
}
