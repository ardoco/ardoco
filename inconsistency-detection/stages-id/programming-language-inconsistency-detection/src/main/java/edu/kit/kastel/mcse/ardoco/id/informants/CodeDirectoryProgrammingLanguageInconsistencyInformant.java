package edu.kit.kastel.mcse.ardoco.id.informants;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dev.langchain4j.service.AiServices;
import edu.kit.kastel.mcse.ardoco.core.data.DataRepository;
import edu.kit.kastel.mcse.ardoco.id.informants.llmagents.CodeDirectoryReadOnlyTools;
import edu.kit.kastel.mcse.ardoco.id.informants.llmagents.ProgrammingLanguageValidationLlmAgent;
import edu.kit.kastel.mcse.ardoco.id.informants.llmagents.ProgrammingLanguageValidationResult;
import edu.kit.kastel.mcse.ardoco.tlr.artemis.strategies.ArtemisNerStrategy;
import edu.kit.kastel.mcse.ardoco.tlr.models.informants.LargeLanguageModel;

/**
 * This class provides functionality for detecting inconsistencies in programming language-related statements within SAD by examining the code directory files.
 */
public class CodeDirectoryProgrammingLanguageInconsistencyInformant extends AbstractProgrammingLanguageInconsistencyInformant {

    private static final Logger logger = LoggerFactory.getLogger(CodeDirectoryProgrammingLanguageInconsistencyInformant.class);

    private final String codeProjectPath;
    private final ProgrammingLanguageValidationLlmAgent validationAgent;

    public CodeDirectoryProgrammingLanguageInconsistencyInformant(DataRepository dataRepository, ArtemisNerStrategy nerStrategy, String codeProjectPath) {
        super("CodeDirectoryProgrammingLanguageInconsistencyInformant", dataRepository, nerStrategy);
        this.codeProjectPath = codeProjectPath;

        var llm = LargeLanguageModel.GPT_5_4_MINI;
        this.validationAgent = AiServices.builder(ProgrammingLanguageValidationLlmAgent.class)
                .chatModel(llm.create())
                .tools(new CodeDirectoryReadOnlyTools(codeProjectPath))
                .build();

        logger.info("Using {} for programming language inconsistency-detection based on code directory.", llm.getHumanReadableName());
    }

    @Override
    protected ProgrammingLanguageValidationResult validateProgrammingLanguageStatement(String namedEntityOccurrence, String programmingLanguageName,
            String fullDocumentationText, String sentenceText) {
        return validationAgent.validateProgrammingLanguageStatementAgainstCodeDirectory(namedEntityOccurrence, programmingLanguageName, fullDocumentationText,
                sentenceText, codeProjectPath);
    }
}
