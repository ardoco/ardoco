/* Licensed under MIT 2022-2025. */
package edu.kit.kastel.mcse.ardoco.core.api.models;

/**
 * Enum for the different types of supported architecture (meta-) models.
 */
public enum ModelFormat {
    /** Palladio Component Model. */
    PCM,
    /** Unified Modeling Language (UML). */
    UML,
    /** Component Listing Model, Simply a list of component names. */
    COMPONENT_LISTING,
    /** Code Model for the ArCoTL framework. */
    ACM
}
