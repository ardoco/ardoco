/* Licensed under MIT 2023-2026. */
package edu.kit.kastel.mcse.ardoco.tlr.tests.integration.evaluation;

import static edu.kit.kastel.mcse.ardoco.core.api.models.Metamodel.CODE_WITH_COMPILATION_UNITS;

import java.io.File;

import org.eclipse.collections.api.factory.SortedMaps;
import org.junit.jupiter.api.Assertions;

import edu.kit.kastel.mcse.ardoco.core.api.models.ModelFormat;
import edu.kit.kastel.mcse.ardoco.core.api.output.ArdocoResult;
import edu.kit.kastel.mcse.ardoco.core.execution.runner.ArdocoRunner;
import edu.kit.kastel.mcse.ardoco.tlr.execution.ArtemisInTransArC;
import edu.kit.kastel.mcse.ardoco.tlr.models.agents.ArchitectureConfiguration;
import edu.kit.kastel.mcse.ardoco.tlr.models.agents.CodeConfiguration;
import edu.kit.kastel.mcse.ardoco.tlr.models.informants.LargeLanguageModel;
import edu.kit.kastel.mcse.ardoco.tlr.tests.approach.TransarcEvaluationProject;

public class ArtemisInTransarcEvaluation extends AbstractDocumentationToCodeTlrEvaluation {

    private final TransarcEvaluationProject project;
    private final LargeLanguageModel largeLanguageModel;

    public ArtemisInTransarcEvaluation(TransarcEvaluationProject project, LargeLanguageModel llmForNer) {
        this.project = project;
        this.largeLanguageModel = llmForNer;
    }

    public ArdocoResult runTraceLinkEvaluation() {
        ArdocoRunner artemisInTransArC = createArtemisInTransArC();
        ArdocoResult result = artemisInTransArC.run();
        Assertions.assertNotNull(result);

        var goldStandard = project.getTlrTask().getExpectedTraceLinks();
        goldStandard = enrollGoldStandard(goldStandard, result, CODE_WITH_COMPILATION_UNITS);
        var evaluationResults = calculateEvaluationResults(result, goldStandard, CODE_WITH_COMPILATION_UNITS);
        var expectedResults = project.getExpectedResults();

        logExtendedResultsWithExpected(project.name(), evaluationResults, expectedResults);
        return result;
    }

    private ArdocoRunner createArtemisInTransArC() {
        String projectName = project.name().toLowerCase();
        File textInput = project.getTlrTask().getTextFile();
        ModelFormat architectureModelFormat = ModelFormat.PCM;
        File inputArchitectureModel = project.getTlrTask().getArchitectureModelFile(architectureModelFormat);
        CodeConfiguration inputCode = new CodeConfiguration(project.getTlrTask().getCodeModelFromResources(), CodeConfiguration.CodeConfigurationType.ACM_FILE);
        File outputDirectory = new File("target", projectName + "-output");
        outputDirectory.mkdirs();

        var runner = new ArtemisInTransArC(projectName);
        runner.setUp(textInput, new ArchitectureConfiguration(inputArchitectureModel, architectureModelFormat), inputCode, SortedMaps.immutable.empty(),
                outputDirectory, largeLanguageModel);
        return runner;
    }
}
