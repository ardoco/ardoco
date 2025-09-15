/* Licensed under MIT 2025. */
package edu.kit.kastel.mcse.ardoco.tlr.tests.integration.evaluation;

import static edu.kit.kastel.mcse.ardoco.core.api.models.Metamodel.CODE_WITH_COMPILATION_UNITS;

import java.io.File;

import org.eclipse.collections.api.factory.SortedMaps;
import org.junit.jupiter.api.Assertions;

import edu.kit.kastel.mcse.ardoco.core.api.models.ModelFormat;
import edu.kit.kastel.mcse.ardoco.core.api.output.ArDoCoResult;
import edu.kit.kastel.mcse.ardoco.core.execution.runner.ArDoCoRunner;
import edu.kit.kastel.mcse.ardoco.tlr.execution.Transarc;
import edu.kit.kastel.mcse.ardoco.tlr.models.agents.ArchitectureConfiguration;
import edu.kit.kastel.mcse.ardoco.tlr.models.agents.CodeConfiguration;
import edu.kit.kastel.mcse.ardoco.tlr.tests.approach.TransArCEvaluationProject;

public class TransarcEvaluation extends AbstractDocumentationToCodeTlrEvaluation {
    private final TransArCEvaluationProject project;
    private final boolean useAcmFile;

    public TransarcEvaluation(TransArCEvaluationProject project, boolean useAcmFile) {
        this.project = project;
        this.useAcmFile = useAcmFile;
    }

    public ArDoCoResult runTraceLinkEvaluation() {
        ArDoCoRunner transArC = createTransarc();
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

    private ArDoCoRunner createTransarc() {
        String projectName = project.name().toLowerCase();
        File textInput = project.getTlrTask().getTextFile();
        ModelFormat architectureModelFormat = ModelFormat.PCM;
        File inputArchitectureModel = project.getTlrTask().getArchitectureModelFile(architectureModelFormat);
        CodeConfiguration inputCode = useAcmFile //
                ? new CodeConfiguration(project.getTlrTask().getCodeModelFromResources(), CodeConfiguration.CodeConfigurationType.ACM_FILE) //
                : new CodeConfiguration(project.getTlrTask().getCodeDirectory(), CodeConfiguration.CodeConfigurationType.DIRECTORY);
        File outputDirectory = new File("target", projectName + "-output");
        outputDirectory.mkdirs();

        var runner = new Transarc(projectName);
        runner.setUp(textInput, new ArchitectureConfiguration(inputArchitectureModel, architectureModelFormat), inputCode, SortedMaps.immutable.empty(),
                outputDirectory);
        return runner;
    }
}
