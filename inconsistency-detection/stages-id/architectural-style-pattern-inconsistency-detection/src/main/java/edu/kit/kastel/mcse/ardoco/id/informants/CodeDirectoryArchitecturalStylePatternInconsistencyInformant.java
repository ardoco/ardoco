package edu.kit.kastel.mcse.ardoco.id.informants;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dev.langchain4j.service.AiServices;
import edu.kit.kastel.mcse.ardoco.core.data.DataRepository;
import edu.kit.kastel.mcse.ardoco.id.informants.llmagents.ArchitecturalStylePatternValidationLlmAgent;
import edu.kit.kastel.mcse.ardoco.id.informants.llmagents.ArchitecturalStylePatternValidationResult;
import edu.kit.kastel.mcse.ardoco.core.common.readonlytools.CodeDirectoryReadOnlyTools;
import edu.kit.kastel.mcse.ardoco.id.informants.validators.ArchitecturalStylePatternStatementValidatorRegistry;
import edu.kit.kastel.mcse.ardoco.tlr.artemis.strategies.ArtemisNerStrategy;
import edu.kit.kastel.mcse.ardoco.tlr.models.informants.LargeLanguageModel;

public class CodeDirectoryArchitecturalStylePatternInconsistencyInformant extends AbstractArchitecturalStylePatternInconsistencyInformant {

    private static final Logger logger = LoggerFactory.getLogger(CodeDirectoryArchitecturalStylePatternInconsistencyInformant.class);

    private final String codeProjectPath;
    private final ArchitecturalStylePatternValidationLlmAgent validationAgent;

    public CodeDirectoryArchitecturalStylePatternInconsistencyInformant(DataRepository dataRepository, ArtemisNerStrategy nerStrategy, String codeProjectPath) {
        super("CodeDirectoryArchitecturalStylePatternInconsistencyInformant", dataRepository, nerStrategy,
                ArchitecturalStylePatternStatementValidatorRegistry.withDefaultValidators());

        this.codeProjectPath = codeProjectPath;

        var llm = LargeLanguageModel.GPT_5_4_MINI;
        this.validationAgent = AiServices.builder(ArchitecturalStylePatternValidationLlmAgent.class)
                .chatModel(llm.createUncached())
                .tools(new CodeDirectoryReadOnlyTools(codeProjectPath))
                .build();

        logger.info("Using {} for architectural style/pattern inconsistency-detection based on code directory.", llm.getHumanReadableName());
    }

    @Override
    protected String getCodeProjectPathOrNull() {
        return codeProjectPath;
    }

    @Override
    protected ArchitecturalStylePatternValidationResult validateArchitecturalStylePatternStatement(String namedEntityOccurrence,
            String architecturalStylePatternName, String fullDocumentationText, String sentenceText) {
        return validationAgent.validateArchitecturalStylePatternStatementAgainstCodeDirectory(namedEntityOccurrence, architecturalStylePatternName,
                fullDocumentationText, sentenceText, codeProjectPath);
    }
}
