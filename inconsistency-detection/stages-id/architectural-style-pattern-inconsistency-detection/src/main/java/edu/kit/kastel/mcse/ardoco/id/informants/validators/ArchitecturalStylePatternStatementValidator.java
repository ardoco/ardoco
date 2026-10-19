package edu.kit.kastel.mcse.ardoco.id.informants.validators;

import edu.kit.kastel.mcse.ardoco.id.informants.llmagents.ArchitecturalStylePatternValidationResult;

/**
 * Defines the contract for validating statements associated with a specific architectural style/pattern. Implementations of this interface are used to ensure
 * that statements comply with predefined rules or descriptions of architectural style/patterns.
 */
public interface ArchitecturalStylePatternStatementValidator {

    /**
     * Checks if the given architectural style/pattern name is supported by this validator.
     *
     * @param architecturalStylePatternName the name of the architectural style/pattern to be validated; must not be null or empty.
     * @return {@code true} if the validator supports the given architectural style/pattern name; {@code false} otherwise.
     */
    boolean supports(String architecturalStylePatternName);

    /**
     * Validates a specific aspect of an architectural style or pattern statement within a given context. The validation ensures that the statement conforms to
     * the rules and requirements of the specified architectural style or pattern.
     *
     * @param context the validation context containing details about the named entity occurrence, architectural style/pattern, associated documentation, and
     *                related metadata; must not be null.
     * @return an {@code ArchitecturalStylePatternValidationResult} indicating whether the validation was successful or failed, along with an explanation.
     */
    ArchitecturalStylePatternValidationResult validate(ArchitecturalStylePatternValidationContext context);
}
