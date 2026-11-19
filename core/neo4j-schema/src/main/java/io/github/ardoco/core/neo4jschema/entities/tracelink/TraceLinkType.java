/* Licensed under MIT 2026. */
package io.github.ardoco.core.neo4jschema.entities.tracelink;

public enum TraceLinkType {
    /**
     * Represents a direct link between an Architecture Model Item and a Code Model Item. In the {@code Transarc} pipeline it is also the second of the two
     * relationships that store each link of ArDoCo's final SAD-to-code result, see
     * {@link io.github.ardoco.core.neo4jschema.service.TraceLinkPersistenceService}.
     */
    ARCHITECTURE_CODE,

    /**
     * Represents a direct link from a Sentence to a Code Model Item, written by the connection state of a code model. In the {@code Transarc} pipeline it is
     * not part of ArDoCo's final SAD-to-code result; in the {@code Ardocode} pipeline the final result is stored with this type as well, see
     * {@link io.github.ardoco.core.neo4jschema.service.TraceLinkPersistenceService}.
     */
    SENTENCE_CODE,

    /**
     * Represents a link from a Sentence to an Architecture Model Item. In the {@code Transarc} pipeline it is also the first of the two relationships that
     * store each link of ArDoCo's final SAD-to-code result, see {@link io.github.ardoco.core.neo4jschema.service.TraceLinkPersistenceService}.
     */
    SENTENCE_ARCHITECTURE,

    /**
     * Represents a link from a RecommendedInstance to an Architecture Model Item (ConnectionState instance link).
     */
    RECOMMENDATION_ARCHITECTURE,

    /**
     * Represents a link from a RecommendedInstance to a Code Model Item (ConnectionState instance link).
     */
    RECOMMENDATION_CODE,

    /**
     * Represents a link from a NamedArchitectureEntityOccurrence to an Architecture Model Item (NER).
     */
    NER_ARCHITECTURE
}
