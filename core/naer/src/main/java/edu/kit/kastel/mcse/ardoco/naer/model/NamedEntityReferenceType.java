/* Licensed under MIT 2025. */
package edu.kit.kastel.mcse.ardoco.naer.model;

/**
 * Indicates how a {@link NamedEntity} is referenced in a sentence.
 */
public enum NamedEntityReferenceType {
    /**
     * A {@link NamedEntity} is referenced directly in a sentence iff its name or one of its alternative names occurs in the sentence.
     */
    DIRECT,
    /**
     * A {@link NamedEntity} is referenced indirectly in a sentence iff it is not referenced directly.
     * <p>
     * Example: "Alice built a table. It is very big."
     * <br>
     * Here, "Table" is the entity. It is referenced directly in the first sentence,
     * and indirectly (via "it") in the second one.
     */
    INDIRECT
}
