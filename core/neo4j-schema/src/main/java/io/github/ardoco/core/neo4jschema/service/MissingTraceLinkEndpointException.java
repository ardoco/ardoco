/* Licensed under MIT 2026. */
package io.github.ardoco.core.neo4jschema.service;

public class MissingTraceLinkEndpointException extends UnresolvedPersistenceTargetException {

    public MissingTraceLinkEndpointException(String message) {
        super(message);
    }
}
