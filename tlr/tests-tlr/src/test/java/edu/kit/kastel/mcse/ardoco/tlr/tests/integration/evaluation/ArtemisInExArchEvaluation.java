/* Licensed under MIT 2023-2025. */
package edu.kit.kastel.mcse.ardoco.tlr.tests.integration.evaluation;

import static edu.kit.kastel.mcse.ardoco.core.api.models.Metamodel.CODE_WITH_COMPILATION_UNITS;

import java.io.File;

import org.eclipse.collections.api.factory.SortedMaps;
import org.junit.jupiter.api.Assertions;

import edu.kit.kastel.mcse.ardoco.core.api.output.ArDoCoResult;
import edu.kit.kastel.mcse.ardoco.core.execution.runner.ArDoCoRunner;
import edu.kit.kastel.mcse.ardoco.tlr.execution.ArtemisInExArch;
import edu.kit.kastel.mcse.ardoco.tlr.models.agents.CodeConfiguration;
import edu.kit.kastel.mcse.ardoco.tlr.models.informants.LargeLanguageModel;
import edu.kit.kastel.mcse.ardoco.tlr.models.informants.LlmArchitecturePrompt;
import edu.kit.kastel.mcse.ardoco.tlr.tests.approach.ArDoCodeEvaluationProject;

public class ArtemisInExArchEvaluation extends AbstractDocumentationToCodeTlrEvaluation {

    private final ArDoCodeEvaluationProject project;

    private final LargeLanguageModel llmForExArch;
    private final LlmArchitecturePrompt documentationExtractionPrompt;
    private final LlmArchitecturePrompt codeExtractionPrompt;
    private final LlmArchitecturePrompt aggregationPrompt;
    private final LlmArchitecturePrompt.Features codeFeatures;
    private final LargeLanguageModel llmForNer;

    public ArtemisInExArchEvaluation(ArDoCodeEvaluationProject project, LargeLanguageModel llmForExArch, LlmArchitecturePrompt documentationExtractionPrompt,
            LlmArchitecturePrompt codeExtractionPrompt, LlmArchitecturePrompt.Features codeFeatures, LlmArchitecturePrompt aggregationPrompt,
            LargeLanguageModel llmForNer) {
        this.project = project;
        this.llmForExArch = llmForExArch;
        this.documentationExtractionPrompt = documentationExtractionPrompt;
        this.codeExtractionPrompt = codeExtractionPrompt;
        this.codeFeatures = codeFeatures;
        this.aggregationPrompt = aggregationPrompt;
        this.llmForNer = llmForNer;
    }

    public ArDoCoResult runTraceLinkEvaluation() {
        ArDoCoRunner exArchRunner = createArtemisInExArch();
        ArDoCoResult result = exArchRunner.run();
        Assertions.assertNotNull(result);

        var goldStandard = project.getTlrTask().getExpectedTraceLinks();
        goldStandard = enrollGoldStandard(goldStandard, result, CODE_WITH_COMPILATION_UNITS);
        var evaluationResults = calculateEvaluationResults(result, goldStandard, CODE_WITH_COMPILATION_UNITS);
        var expectedResults = project.getExpectedResults();

        logExtendedResultsWithExpected(project.name(), evaluationResults, expectedResults);
        return result;
    }

    private ArDoCoRunner createArtemisInExArch() {
        String projectName = project.name().toLowerCase();
        File textInput = project.getTlrTask().getTextFile();
        File inputCode = project.getTlrTask().getCodeModelFromResources();
        File outputDirectory = new File("target", projectName + "-output");
        outputDirectory.mkdirs();

        var runner = new ArtemisInExArch(projectName);
        runner.setUp(textInput, new CodeConfiguration(inputCode, CodeConfiguration.CodeConfigurationType.ACM_FILE), SortedMaps.immutable.empty(),
                outputDirectory, llmForExArch, documentationExtractionPrompt, codeExtractionPrompt, codeFeatures, aggregationPrompt, llmForNer);
        return runner;
    }
}
