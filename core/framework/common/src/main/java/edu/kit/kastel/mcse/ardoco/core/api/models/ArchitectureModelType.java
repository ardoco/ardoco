/* Licensed under MIT 2022-2024. */
package edu.kit.kastel.mcse.ardoco.core.api.models;

/**
 * Enum for the different types of supported architecture (meta-) models
 */
public enum ArchitectureModelType implements ModelType {
    /**
     * Palladio Component Model
     */
    PCM,
    /**
     * Unified Modeling Language (UML)
     */
    UML,
    /**
     * RAW Model. E.g., text-based models.
     */
    RAW;

    @Override
    public Metamodel getMetamodel() {
        return Metamodel.ARCHITECTURE;
    }
}
