package edu.kit.kastel.mcse.ardoco.secdreqan;

import edu.kit.kastel.mcse.ardoco.core.tests.eval.Project;
import edu.kit.kastel.mcse.ardoco.tlr.tests.integration.SadSamTraceabilityLinkRecoveryEvaluation;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Arrays;
import java.util.List;

public class SecDReqAnEvaluation<T extends GoldStandardMultiProject> {

    /*
    protected static final String OUTPUT = "target/testout-secdreqan-eval";

    @DisplayName("Evaluate SAD-SAM TLR")
    @ParameterizedTest(name = "{0}")
    @MethodSource("getProjects")
    void evaluateSadSamTlrIT(T project) {
        var evaluation = new SadSamTraceabilityLinkRecoveryEvaluation<>();
        var arDoCoResult = evaluation.runTraceLinkEvaluation(project);
        Assertions.assertNotNull(arDoCoResult);
    }

    private static List<? extends GoldStandardMultiProject> getProjects() {
        return Arrays.asList(Project.values());
    }

     */
}
