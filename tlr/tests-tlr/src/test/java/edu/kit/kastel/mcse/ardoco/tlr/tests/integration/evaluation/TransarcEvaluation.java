/* Licensed under MIT 2025-2026. */
package edu.kit.kastel.mcse.ardoco.tlr.tests.integration.evaluation;

import static edu.kit.kastel.mcse.ardoco.core.api.models.Metamodel.CODE_WITH_COMPILATION_UNITS;

import java.io.File;

import org.eclipse.collections.api.factory.SortedMaps;
import org.junit.jupiter.api.Assertions;

import edu.kit.kastel.mcse.ardoco.core.api.models.ModelFormat;
import edu.kit.kastel.mcse.ardoco.core.api.output.ArdocoResult;
import edu.kit.kastel.mcse.ardoco.core.execution.runner.ArdocoRunner;
import edu.kit.kastel.mcse.ardoco.tlr.execution.Transarc;
import edu.kit.kastel.mcse.ardoco.tlr.models.agents.ArchitectureConfiguration;
import edu.kit.kastel.mcse.ardoco.tlr.models.agents.CodeConfiguration;
import edu.kit.kastel.mcse.ardoco.tlr.tests.approach.TransarcEvaluationProject;

public class TransarcEvaluation extends AbstractDocumentationToCodeTlrEvaluation {
    private final TransarcEvaluationProject project;
    private final boolean useAcmFile;

    public TransarcEvaluation(TransarcEvaluationProject project, boolean useAcmFile) {
        this.project = project;
        this.useAcmFile = useAcmFile;
    }

    public ArdocoResult runTraceLinkEvaluation() {
        ArdocoRunner transArC = createTransarc();
        ArdocoResult result = transArC.run();
        Assertions.assertNotNull(result);

        var goldStandard = project.getTlrTask().getExpectedTraceLinks();
        goldStandard = enrollGoldStandard(goldStandard, result, CODE_WITH_COMPILATION_UNITS);
        var evaluationResults = this.calculateEvaluationResults(result, goldStandard, CODE_WITH_COMPILATION_UNITS);
        var expectedResults = project.getExpectedResults();

        logExtendedResultsWithExpected(project.name(), evaluationResults, expectedResults);
        compareResults(evaluationResults, expectedResults);
        return result;
    }

    private ArdocoRunner createTransarc() {
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
