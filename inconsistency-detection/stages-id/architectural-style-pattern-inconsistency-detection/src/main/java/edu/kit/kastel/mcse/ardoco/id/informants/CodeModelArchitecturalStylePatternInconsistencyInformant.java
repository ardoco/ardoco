/* Licensed under MIT 2026. */
package edu.kit.kastel.mcse.ardoco.id.informants;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dev.langchain4j.service.AiServices;
import edu.kit.kastel.mcse.ardoco.core.common.readonlytools.CodeModelReadOnlyTools;
import edu.kit.kastel.mcse.ardoco.core.data.DataRepository;
import edu.kit.kastel.mcse.ardoco.id.informants.llmagents.ArchitecturalStylePatternValidationLlmAgent;
import edu.kit.kastel.mcse.ardoco.id.informants.llmagents.ArchitecturalStylePatternValidationResult;
import edu.kit.kastel.mcse.ardoco.id.informants.validators.ArchitecturalStylePatternStatementValidatorRegistry;
import edu.kit.kastel.mcse.ardoco.tlr.artemis.strategies.ArtemisNerStrategy;
import edu.kit.kastel.mcse.ardoco.tlr.models.informants.LargeLanguageModel;

/**
 * Informant that detects inconsistencies between documented architectural styles or patterns and the code model stored in the data repository.
 * <p>
 * The informant validates architectural style or pattern statements from the documentation against the available code model. Validation is delegated to an
 * LLM-based validation agent equipped with read-only tools for inspecting the code model.
 */
public class CodeModelArchitecturalStylePatternInconsistencyInformant extends AbstractArchitecturalStylePatternInconsistencyInformant {

    private static final Logger logger = LoggerFactory.getLogger(CodeModelArchitecturalStylePatternInconsistencyInformant.class);
    private static final int MAX_VALIDATION_RETRIES = 3;

    private final ArchitecturalStylePatternValidationLlmAgent validationAgent;

    public CodeModelArchitecturalStylePatternInconsistencyInformant(DataRepository dataRepository, ArtemisNerStrategy nerStrategy,
            LargeLanguageModel validationAgentLlm) {
        super("CodeModelArchitecturalStylePatternInconsistencyInformant", dataRepository, nerStrategy, ArchitecturalStylePatternStatementValidatorRegistry
                .withDefaultValidators());

        CodeModelReadOnlyTools codeModelReadOnlyTools = new CodeModelReadOnlyTools(dataRepository);

        this.validationAgent = AiServices.builder(ArchitecturalStylePatternValidationLlmAgent.class)
                .chatModel(validationAgentLlm.createUncached())
                .tools(codeModelReadOnlyTools)
                .beforeToolExecution(before -> codeModelReadOnlyTools.initializeCodeModel())
                .build();

        logger.info("Using {} for architectural style/pattern inconsistency-detection based on code model.", validationAgentLlm.getHumanReadableName());
    }

    @Override
    protected ArchitecturalStylePatternValidationResult validateArchitecturalStylePatternStatement(String namedEntityOccurrence,
            String architecturalStylePatternName, String fullDocumentationText, String sentenceText) {
        Exception lastException = null;

        for (int attempt = 1; attempt <= MAX_VALIDATION_RETRIES; attempt++) {
            try {
                return validationAgent.validateArchitecturalStylePatternStatementAgainstCodeModel(namedEntityOccurrence, architecturalStylePatternName,
                        fullDocumentationText, sentenceText);
            } catch (Exception e) {
                lastException = e;
                logger.warn("Validation failed. Retrying ({}/{}).", attempt, MAX_VALIDATION_RETRIES, e);
            }
        }

        throw new RuntimeException(lastException);
    }
}
