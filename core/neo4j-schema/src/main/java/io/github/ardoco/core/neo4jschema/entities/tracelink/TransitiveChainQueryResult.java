package io.github.ardoco.core.neo4jschema.entities.tracelink;

import io.github.ardoco.core.neo4jschema.entities.documentation.SentenceNode;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class TransitiveChainQueryResult {
    private final SentenceNode sentence;
    private final TraceableNode architecture; //  mid node
    private final TraceableNode code;         //  end node
}
