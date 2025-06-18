/* Licensed under MIT 2022-2025. */
package edu.kit.kastel.mcse.ardoco.core.api.models;

public enum Metamodel {
    CODE_ONLY_COMPILATION_UNITS, ARCHITECTURE_WITH_COMPONENTS_AND_INTERFACES, CODE_WITH_COMPILATION_UNITS_AND_PACKAGES, ARCHITECTURE_ONLY_COMPONENTS;

    public static boolean isACodeModel(Metamodel metamodel) {
        return switch (metamodel) {
            case CODE_WITH_COMPILATION_UNITS_AND_PACKAGES, CODE_ONLY_COMPILATION_UNITS -> true;
            case ARCHITECTURE_ONLY_COMPONENTS, ARCHITECTURE_WITH_COMPONENTS_AND_INTERFACES -> false;
        };
    }

    public static boolean isAnArchitectureModel(Metamodel metamodel) {
        return switch (metamodel) {
            case ARCHITECTURE_WITH_COMPONENTS_AND_INTERFACES, ARCHITECTURE_ONLY_COMPONENTS -> true;
            case CODE_ONLY_COMPILATION_UNITS, CODE_WITH_COMPILATION_UNITS_AND_PACKAGES -> false;
        };
    }
}
