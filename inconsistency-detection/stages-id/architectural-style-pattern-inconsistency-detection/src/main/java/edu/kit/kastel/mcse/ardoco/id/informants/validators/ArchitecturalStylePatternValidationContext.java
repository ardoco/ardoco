package edu.kit.kastel.mcse.ardoco.id.informants.validators;

import java.util.Objects;

import edu.kit.kastel.mcse.ardoco.core.data.DataRepository;

public record ArchitecturalStylePatternValidationContext(DataRepository dataRepository, String namedEntityOccurrence, String architecturalStylePatternName,
                                                         String fullDocumentationText, String sentenceText, String codeProjectPath) {

    public ArchitecturalStylePatternValidationContext {
        Objects.requireNonNull(dataRepository, "dataRepository must not be null");
        Objects.requireNonNull(namedEntityOccurrence, "namedEntityOccurrence must not be null");
        Objects.requireNonNull(architecturalStylePatternName, "architecturalStylePatternName must not be null");
        Objects.requireNonNull(fullDocumentationText, "fullDocumentationText must not be null");
        Objects.requireNonNull(sentenceText, "sentenceText must not be null");
    }

    public boolean hasCodeProjectPath() {
        return codeProjectPath != null && !codeProjectPath.isBlank();
    }
}
