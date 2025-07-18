/* Licensed under MIT 2025. */
package edu.kit.kastel.mcse.ardoco.tlr.tests.integration.evaluation;

import static edu.kit.kastel.mcse.ardoco.core.api.models.Metamodel.CODE_WITH_COMPILATION_UNITS_AND_PACKAGES;

import java.io.File;
import java.util.SortedMap;
import java.util.TreeMap;

import org.junit.jupiter.api.Assertions;

import edu.kit.kastel.mcse.ardoco.core.api.output.ArDoCoResult;
import edu.kit.kastel.mcse.ardoco.core.execution.runner.ArDoCoRunner;
import edu.kit.kastel.mcse.ardoco.tlr.execution.ArDoCode;
import edu.kit.kastel.mcse.ardoco.tlr.tests.approach.ArDoCodeEvaluationProject;

public class ArDoCodeEvaluation extends AbstractDocumentationToCodeTlrEvaluation {
    private final ArDoCodeEvaluationProject project;
    private final boolean useAcmFile;

    public ArDoCodeEvaluation(ArDoCodeEvaluationProject project) {
        this.project = project;
        this.useAcmFile = true;
    }

    public ArDoCoResult runTraceLinkEvaluation() {
        ArDoCoRunner ardocode = createArDoCodeRunner();
        ArDoCoResult result = ardocode.run();
        Assertions.assertNotNull(result);

        var goldStandard = project.getTlrTask().getExpectedTraceLinks();
        goldStandard = enrollGoldStandard(goldStandard, result, CODE_WITH_COMPILATION_UNITS_AND_PACKAGES);
        var evaluationResults = this.calculateEvaluationResults(result, goldStandard, CODE_WITH_COMPILATION_UNITS_AND_PACKAGES);
        var expectedResults = project.getExpectedResults();

        logExtendedResultsWithExpected(project.name(), evaluationResults, expectedResults);
        compareResults(evaluationResults, expectedResults);
        return result;
    }

    private ArDoCoRunner createArDoCodeRunner() {
        String projectName = project.name().toLowerCase();
        File textInput = project.getTlrTask().getTextFile();
        File inputCode = project.getTlrTask().getCodeModelFile(useAcmFile);
        SortedMap<String, String> additionalConfigsMap = new TreeMap<>();
        File outputDirectory = new File("target", projectName + "-output");
        outputDirectory.mkdirs();

        var runner = new ArDoCode(projectName);
        runner.setUp(textInput, inputCode, additionalConfigsMap, outputDirectory);
        return runner;
    }
}
