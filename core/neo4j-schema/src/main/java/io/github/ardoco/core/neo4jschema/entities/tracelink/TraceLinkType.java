package io.github.ardoco.core.neo4jschema.entities.tracelink;

public enum TraceLinkType {
    /**
     * Represents a direct link between an Architecture Model Item and a Code Model Item.
     */
    ARCHITECTURE_CODE,

    /**
     * Represents a Transitive Trace Link (e.g., Sentence -> Architecture -> Code, flattened).
     */
    TRANSITIVE,

    /**
     * Represents a link from a Sentence to a Code Model Item.
     */
    SENTENCE_CODE
}
