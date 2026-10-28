/* Licensed under MIT 2026. */
package edu.kit.kastel.mcse.ardoco.id.informants.llmagents;

import java.util.Objects;

public record ProgrammingLanguageValidationResult(boolean valid, String explanation) {

    public ProgrammingLanguageValidationResult {
        Objects.requireNonNull(explanation, "explanation must not be null");
    }
}
