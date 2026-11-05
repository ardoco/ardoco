/* Licensed under MIT 2026. */
package io.github.ardoco.core.neo4jschema.service;

public class MissingLinkTargetException extends UnresolvedPersistenceTargetException {

    public MissingLinkTargetException(String message) {
        super(message);
    }
}
