/* Licensed under MIT 2026. */
package edu.kit.kastel.mcse.ardoco.id.informants;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dev.langchain4j.service.AiServices;
import edu.kit.kastel.mcse.ardoco.core.common.readonlytools.CodeDirectoryReadOnlyTools;
import edu.kit.kastel.mcse.ardoco.core.data.DataRepository;
import edu.kit.kastel.mcse.ardoco.id.informants.llmagents.ProgrammingLanguageValidationLlmAgent;
import edu.kit.kastel.mcse.ardoco.id.informants.llmagents.ProgrammingLanguageValidationResult;
import edu.kit.kastel.mcse.ardoco.tlr.artemis.strategies.ArtemisNerStrategy;
import edu.kit.kastel.mcse.ardoco.tlr.models.informants.LargeLanguageModel;

/**
 * Informant that detects inconsistencies between documented programming language statements and the source-code directory.
 * <p>
 * The informant validates programming language occurrences extracted from the documentation by using an LLM agent with read-only access to the configured code
 * project directory. Statements that cannot be supported by the directory contents are reported as programming language inconsistencies.
 * </p>
 */
public class CodeDirectoryProgrammingLanguageInconsistencyInformant extends AbstractProgrammingLanguageInconsistencyInformant {

    private static final Logger logger = LoggerFactory.getLogger(CodeDirectoryProgrammingLanguageInconsistencyInformant.class);
    private static final int MAX_VALIDATION_RETRIES = 3;

    private final String codeProjectPath;
    private final ProgrammingLanguageValidationLlmAgent validationAgent;

    public CodeDirectoryProgrammingLanguageInconsistencyInformant(DataRepository dataRepository, ArtemisNerStrategy nerStrategy, String codeProjectPath) {
        super("CodeDirectoryProgrammingLanguageInconsistencyInformant", dataRepository, nerStrategy);
        this.codeProjectPath = codeProjectPath;

        var llm = LargeLanguageModel.GPT_5_4_MINI;
        this.validationAgent = AiServices.builder(ProgrammingLanguageValidationLlmAgent.class)
                .chatModel(llm.createUncached()) //this is important because Ardoco CachedChatLanguageModel does not support Tool calling
                .tools(new CodeDirectoryReadOnlyTools(codeProjectPath))
                .build();

        logger.info("Using {} for programming language inconsistency-detection based on code directory.", llm.getHumanReadableName());
    }

    @Override
    protected ProgrammingLanguageValidationResult validateProgrammingLanguageStatement(String namedEntityOccurrence, String programmingLanguageName,
            String fullDocumentationText, String sentenceText) {
        IllegalArgumentException lastIllegalArgumentException = null;

        for (int attempt = 1; attempt <= MAX_VALIDATION_RETRIES; attempt++) {
            try {
                return validationAgent.validateProgrammingLanguageStatementAgainstCodeDirectory(namedEntityOccurrence, programmingLanguageName,
                        fullDocumentationText, sentenceText, codeProjectPath);
            } catch (IllegalArgumentException e) {
                lastIllegalArgumentException = e;
                logger.warn("Validation failed. Retrying ({}/{}).", attempt, MAX_VALIDATION_RETRIES, e);
            }
        }

        throw lastIllegalArgumentException;
    }
}
