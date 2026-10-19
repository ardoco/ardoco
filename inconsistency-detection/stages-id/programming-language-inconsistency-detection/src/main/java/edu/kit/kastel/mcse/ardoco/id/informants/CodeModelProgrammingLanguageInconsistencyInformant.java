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
 * This class provides functionality for detecting inconsistencies in programming language-related statements within SAD by examining the code model.
 */
public class CodeModelProgrammingLanguageInconsistencyInformant extends AbstractProgrammingLanguageInconsistencyInformant {

    private static final Logger logger = LoggerFactory.getLogger(CodeModelProgrammingLanguageInconsistencyInformant.class);
    private static final int MAX_VALIDATION_RETRIES = 3;

    private final ProgrammingLanguageValidationLlmAgent validationAgent;

    public CodeModelProgrammingLanguageInconsistencyInformant(DataRepository dataRepository, ArtemisNerStrategy nerStrategy) {
        super("CodeModelProgrammingLanguageInconsistencyInformant", dataRepository, nerStrategy);

        var llm = LargeLanguageModel.GPT_5_4_MINI;
        CodeModelReadOnlyTools codeModelReadOnlyTools = new CodeModelReadOnlyTools(dataRepository);
        this.validationAgent = AiServices.builder(ProgrammingLanguageValidationLlmAgent.class)
                .chatModel(llm.createUncached()) //this is important because Ardoco CachedChatLanguageModel does not support Tool calling
                .tools(codeModelReadOnlyTools)
                .beforeToolExecution(before -> codeModelReadOnlyTools.initializeCodeModel())
                .build();

        logger.info("Using {} for programming language inconsistency-detection based on code model.", llm.getHumanReadableName());
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
