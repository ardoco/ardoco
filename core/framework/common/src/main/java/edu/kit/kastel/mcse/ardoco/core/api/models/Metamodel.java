/* Licensed under MIT 2022-2025. */
package edu.kit.kastel.mcse.ardoco.core.api.models;

/**
 * Enum representing the supported metamodel types.
 */
public enum Metamodel {
    /** Code model with only compilation units. */
    CODE_WITH_COMPILATION_UNITS,
    /** Architecture model with components and interfaces. */
    ARCHITECTURE_WITH_COMPONENTS_AND_INTERFACES,
    /** Code model with compilation units and packages. */
    CODE_WITH_COMPILATION_UNITS_AND_PACKAGES,
    /** Architecture model with only components. */
    ARCHITECTURE_WITH_COMPONENTS;

    public boolean isArchitectureModel() {
        return this.name().startsWith("ARCHITECTURE");
    }
}
