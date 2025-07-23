/* Licensed under MIT 2023-2025. */
package edu.kit.kastel.mcse.ardoco.tlr.tests.integration.evaluation;

import static edu.kit.kastel.mcse.ardoco.core.api.models.Metamodel.CODE_WITH_COMPILATION_UNITS_AND_PACKAGES;

import java.io.File;

import org.eclipse.collections.api.factory.SortedMaps;
import org.junit.jupiter.api.Assertions;

import edu.kit.kastel.mcse.ardoco.core.api.output.ArDoCoResult;
import edu.kit.kastel.mcse.ardoco.core.execution.runner.ArDoCoRunner;
import edu.kit.kastel.mcse.ardoco.tlr.execution.TransArCAi;
import edu.kit.kastel.mcse.ardoco.tlr.models.agents.CodeConfiguration;
import edu.kit.kastel.mcse.ardoco.tlr.models.informants.LLMArchitecturePrompt;
import edu.kit.kastel.mcse.ardoco.tlr.models.informants.LargeLanguageModel;
import edu.kit.kastel.mcse.ardoco.tlr.tests.approach.ArDoCodeEvaluationProject;

public class TransArCAiEvaluation extends AbstractDocumentationToCodeTlrEvaluation {

    private final ArDoCodeEvaluationProject project;

    private final LargeLanguageModel largeLanguageModel;
    private final LLMArchitecturePrompt documentationExtractionPrompt;
    private final LLMArchitecturePrompt codeExtractionPrompt;
    private final LLMArchitecturePrompt aggregationPrompt;
    private final LLMArchitecturePrompt.Features codeFeatures;

    public TransArCAiEvaluation(ArDoCodeEvaluationProject project, LargeLanguageModel largeLanguageModel, LLMArchitecturePrompt documentationExtractionPrompt,
            LLMArchitecturePrompt codeExtractionPrompt, LLMArchitecturePrompt.Features codeFeatures, LLMArchitecturePrompt aggregationPrompt) {
        this.project = project;
        this.largeLanguageModel = largeLanguageModel;
        this.documentationExtractionPrompt = documentationExtractionPrompt;
        this.codeExtractionPrompt = codeExtractionPrompt;
        this.codeFeatures = codeFeatures;
        this.aggregationPrompt = aggregationPrompt;
    }

    public ArDoCoResult runTraceLinkEvaluation() {
        ArDoCoRunner transArCAiRunner = createTransArCAiRunner();
        ArDoCoResult result = transArCAiRunner.run();
        Assertions.assertNotNull(result);

        var goldStandard = project.getTlrTask().getExpectedTraceLinks();
        goldStandard = enrollGoldStandard(goldStandard, result, CODE_WITH_COMPILATION_UNITS_AND_PACKAGES);
        var evaluationResults = calculateEvaluationResults(result, goldStandard, CODE_WITH_COMPILATION_UNITS_AND_PACKAGES);
        var expectedResults = project.getExpectedResults();

        logExtendedResultsWithExpected(project.name(), evaluationResults, expectedResults);
        return result;
    }

    private ArDoCoRunner createTransArCAiRunner() {
        String projectName = project.name().toLowerCase();
        File textInput = project.getTlrTask().getTextFile();
        File inputCode = project.getTlrTask().getCodeModelFromResources();
        File outputDirectory = new File("target", projectName + "-output");
        outputDirectory.mkdirs();

        var runner = new TransArCAi(projectName);
        runner.setUp(textInput, new CodeConfiguration(inputCode, CodeConfiguration.CodeConfigurationType.ACM_FILE), SortedMaps.immutable.empty(),
                outputDirectory, largeLanguageModel, documentationExtractionPrompt, codeExtractionPrompt, codeFeatures, aggregationPrompt);
        return runner;
    }
}
