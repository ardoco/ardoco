/* Licensed under MIT 2025. */
package edu.kit.kastel.mcse.ardoco.tlr.tests.integration.evaluation;

import static edu.kit.kastel.mcse.ardoco.core.api.models.Metamodel.CODE_WITH_COMPILATION_UNITS_AND_PACKAGES;

import java.io.File;

import org.eclipse.collections.api.factory.SortedMaps;
import org.junit.jupiter.api.Assertions;

import edu.kit.kastel.mcse.ardoco.core.api.output.ArDoCoResult;
import edu.kit.kastel.mcse.ardoco.core.execution.runner.ArDoCoRunner;
import edu.kit.kastel.mcse.ardoco.tlr.execution.Ardocode;
import edu.kit.kastel.mcse.ardoco.tlr.models.agents.CodeConfiguration;
import edu.kit.kastel.mcse.ardoco.tlr.tests.approach.ArDoCodeEvaluationProject;

public class ArdocodeEvaluation extends AbstractDocumentationToCodeTlrEvaluation {
    private final ArDoCodeEvaluationProject project;

    public ArdocodeEvaluation(ArDoCodeEvaluationProject project) {
        this.project = project;
    }

    public ArDoCoResult runTraceLinkEvaluation() {
        ArDoCoRunner ardocode = createArdocode();
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

    private ArDoCoRunner createArdocode() {
        String projectName = project.name().toLowerCase();
        File textInput = project.getTlrTask().getTextFile();
        File inputCode = project.getTlrTask().getCodeModelFromResources();
        File outputDirectory = new File("target", projectName + "-output");
        outputDirectory.mkdirs();

        var runner = new Ardocode(projectName);
        runner.setUp(textInput, new CodeConfiguration(inputCode, CodeConfiguration.CodeConfigurationType.ACM_FILE), SortedMaps.immutable.empty(),
                outputDirectory);
        return runner;
    }
}
