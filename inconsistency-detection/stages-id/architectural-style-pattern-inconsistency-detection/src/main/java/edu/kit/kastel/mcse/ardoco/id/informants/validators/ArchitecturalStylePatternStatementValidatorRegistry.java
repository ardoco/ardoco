package edu.kit.kastel.mcse.ardoco.id.informants.validators;

import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;

/**
 * This class serves as a registry for managing and accessing {@code ArchitecturalStylePatternStatementValidator} instances. It is responsible for holding a
 * list of validators and providing mechanisms to retrieve validators based on a normalized architectural style or pattern name.
 */
public class ArchitecturalStylePatternStatementValidatorRegistry {

    private final List<ArchitecturalStylePatternStatementValidator> validators;

    public ArchitecturalStylePatternStatementValidatorRegistry(List<ArchitecturalStylePatternStatementValidator> validators) {
        this.validators = List.copyOf(Objects.requireNonNull(validators, "validators must not be null"));
    }

    public static ArchitecturalStylePatternStatementValidatorRegistry withDefaultValidators() {
        return new ArchitecturalStylePatternStatementValidatorRegistry(
                List.of()); //specific validators (implementations of the ArchitecturalStylePatternStatementValidator interface) can be added here
    }

    static String normalize(String name) {
        Objects.requireNonNull(name, "name must not be null");
        String normalized = name.trim().toLowerCase(Locale.ROOT);
        normalized = normalized.replace("_", "-");
        normalized = normalized.replaceAll("\\s+", " ");

        return normalized;
    }

    public Optional<ArchitecturalStylePatternStatementValidator> findValidator(String architecturalStylePatternName) {
        String normalizedName = normalize(architecturalStylePatternName);
        return validators.stream().filter(validator -> validator.supports(normalizedName)).findFirst();
    }
}
