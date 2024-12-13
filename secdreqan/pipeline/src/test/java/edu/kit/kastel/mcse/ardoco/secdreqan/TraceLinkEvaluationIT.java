package edu.kit.kastel.mcse.ardoco.secdreqan;

import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Arrays;
import java.util.List;

public class TraceLinkEvaluationIT <T extends MultiGoldStandardProject>{



    protected static final String LOGGING_ARDOCO_SECDREQAN = "org.slf4j.simpleLogger.log.edu.kit.kastel.mcse.ardoco.secdreqan";

    @BeforeAll
    static void beforeAll() {
        System.setProperty(LOGGING_ARDOCO_SECDREQAN, "info");
    }

    @AfterAll
    static void afterAll() {
        System.setProperty(LOGGING_ARDOCO_SECDREQAN, "error");
    }

    @DisplayName("Evaluate Req-SAM TLR")
    @ParameterizedTest(name = "{0}")
    @MethodSource("getProjects")
    void evaluateSadSamTlrIT(T project) {
        var evaluation = new SecDReqAnEvaluation();
        var macroResult = evaluation.runTraceLinkEvaluation(project);
        Assertions.assertNotNull(macroResult);
    }

    private static List<? extends MultiGoldStandardProject> getProjects() {
        return Arrays.asList(MultiGoldStandardProject.values());
    }

}
