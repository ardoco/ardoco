/* Licensed under MIT 2025-2026. */
package edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements;

import org.jspecify.annotations.Nullable;

/**
 * Represents an identifier for an element. An element gets identified by its
 * name, path, and type.
 */
public record ElementIdentifier(String name, String path, @Nullable Type type) {
}
