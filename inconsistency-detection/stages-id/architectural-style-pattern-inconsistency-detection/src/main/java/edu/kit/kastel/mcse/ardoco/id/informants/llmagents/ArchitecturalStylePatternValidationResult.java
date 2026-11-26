/* Licensed under MIT 2026. */
package edu.kit.kastel.mcse.ardoco.id.informants.llmagents;

import java.util.Objects;

public record ArchitecturalStylePatternValidationResult(boolean valid, String explanation) {

    public ArchitecturalStylePatternValidationResult {
        Objects.requireNonNull(explanation, "explanation must not be null");
    }

    public static ArchitecturalStylePatternValidationResult valid(String explanation) {
        return new ArchitecturalStylePatternValidationResult(true, explanation);
    }

    public static ArchitecturalStylePatternValidationResult invalid(String explanation) {
        return new ArchitecturalStylePatternValidationResult(false, explanation);
    }
}
