/* Licensed under MIT 2026. */
package edu.kit.kastel.mcse.ardoco.id.informants;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dev.langchain4j.service.AiServices;
import edu.kit.kastel.mcse.ardoco.core.common.readonlytools.CodeModelReadOnlyTools;
import edu.kit.kastel.mcse.ardoco.core.data.DataRepository;
import edu.kit.kastel.mcse.ardoco.id.informants.llmagents.ProgrammingLanguageValidationLlmAgent;
import edu.kit.kastel.mcse.ardoco.id.informants.llmagents.ProgrammingLanguageValidationResult;
import edu.kit.kastel.mcse.ardoco.tlr.artemis.strategies.ArtemisNerStrategy;
import edu.kit.kastel.mcse.ardoco.tlr.models.informants.LargeLanguageModel;

/**
 * Informant that detects inconsistencies between documented programming language statements and the code model.
 * <p>
 * The informant validates programming language occurrences extracted from the documentation by using an LLM agent with read-only access to the available code
 * model. Statements that cannot be supported by the model are reported as programming language inconsistencies.
 * </p>
 */
public class CodeModelProgrammingLanguageInconsistencyInformant extends AbstractProgrammingLanguageInconsistencyInformant {

    private static final Logger logger = LoggerFactory.getLogger(CodeModelProgrammingLanguageInconsistencyInformant.class);
    private static final int MAX_VALIDATION_RETRIES = 3;

    private final ProgrammingLanguageValidationLlmAgent validationAgent;

    public CodeModelProgrammingLanguageInconsistencyInformant(DataRepository dataRepository, ArtemisNerStrategy nerStrategy,
            LargeLanguageModel validationAgentLlm) {
        super("CodeModelProgrammingLanguageInconsistencyInformant", dataRepository, nerStrategy);

        CodeModelReadOnlyTools codeModelReadOnlyTools = new CodeModelReadOnlyTools(dataRepository);
        this.validationAgent = AiServices.builder(ProgrammingLanguageValidationLlmAgent.class)
                .chatModel(validationAgentLlm.createUncached()) //this is important because Ardoco CachedChatLanguageModel does not support Tool calling
                .tools(codeModelReadOnlyTools)
                .beforeToolExecution(before -> codeModelReadOnlyTools.initializeCodeModel())
                .build();

        logger.info("Using {} for programming language inconsistency-detection based on code model.", validationAgentLlm.getHumanReadableName());
    }

    @Override
    protected ProgrammingLanguageValidationResult validateProgrammingLanguageStatement(String namedEntityOccurrence, String programmingLanguageName,
            String fullDocumentationText, String sentenceText) {
        IllegalArgumentException lastIllegalArgumentException = null;

        for (int attempt = 1; attempt <= MAX_VALIDATION_RETRIES; attempt++) {
            try {
                return validationAgent.validateProgrammingLanguageStatementAgainstCodeModel(namedEntityOccurrence, programmingLanguageName,
                        fullDocumentationText, sentenceText);
            } catch (IllegalArgumentException e) {
                lastIllegalArgumentException = e;
                logger.warn("Validation failed. Retrying ({}/{}).", attempt, MAX_VALIDATION_RETRIES, e);
            }
        }

        throw lastIllegalArgumentException;
    }
}
