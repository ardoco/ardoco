/* Licensed under MIT 2025. */
package edu.kit.kastel.mcse.ardoco.tlr.tests.integration.evaluation;

import static edu.kit.kastel.mcse.ardoco.core.api.models.Metamodel.CODE_WITH_COMPILATION_UNITS;

import java.io.File;
import java.util.TreeMap;

import org.junit.jupiter.api.Assertions;

import edu.kit.kastel.mcse.ardoco.core.api.models.ModelFormat;
import edu.kit.kastel.mcse.ardoco.core.api.output.ArDoCoResult;
import edu.kit.kastel.mcse.ardoco.core.execution.runner.ArDoCoRunner;
import edu.kit.kastel.mcse.ardoco.tlr.execution.TransArC;
import edu.kit.kastel.mcse.ardoco.tlr.models.agents.ArchitectureConfiguration;
import edu.kit.kastel.mcse.ardoco.tlr.models.agents.CodeConfiguration;
import edu.kit.kastel.mcse.ardoco.tlr.tests.approach.TransArCEvaluationProject;

public class TransArCEvaluation extends AbstractDocumentationToCodeTlrEvaluation {
    private final TransArCEvaluationProject project;
    private final boolean useAcmFile;

    public TransArCEvaluation(TransArCEvaluationProject project, boolean useAcmFile) {
        this.project = project;
        this.useAcmFile = useAcmFile;
    }

    public ArDoCoResult runTraceLinkEvaluation() {
        ArDoCoRunner transArC = createTransArCRunner();
        ArDoCoResult result = transArC.run();
        Assertions.assertNotNull(result);

        var goldStandard = project.getTlrTask().getExpectedTraceLinks();
        goldStandard = enrollGoldStandard(goldStandard, result, CODE_WITH_COMPILATION_UNITS);
        var evaluationResults = this.calculateEvaluationResults(result, goldStandard, CODE_WITH_COMPILATION_UNITS);
        var expectedResults = project.getExpectedResults();

        logExtendedResultsWithExpected(project.name(), evaluationResults, expectedResults);
        compareResults(evaluationResults, expectedResults);
        return result;
    }

    private ArDoCoRunner createTransArCRunner() {
        String projectName = project.name().toLowerCase();
        File textInput = project.getTlrTask().getTextFile();
        ModelFormat architectureModelFormat = ModelFormat.PCM;
        File inputArchitectureModel = project.getTlrTask().getArchitectureModelFile(architectureModelFormat);
        CodeConfiguration inputCode = useAcmFile //
                ? new CodeConfiguration(project.getTlrTask().getCodeModelFromResources(), CodeConfiguration.CodeConfigurationType.ACM_FILE) //
                : new CodeConfiguration(project.getTlrTask().getCodeDirectory(), CodeConfiguration.CodeConfigurationType.DIRECTORY);
        File outputDirectory = new File("target", projectName + "-output");
        outputDirectory.mkdirs();

        var runner = new TransArC(projectName);
        runner.setUp(textInput, new ArchitectureConfiguration(inputArchitectureModel, architectureModelFormat), inputCode, new TreeMap<>(), outputDirectory);
        return runner;
    }
}
