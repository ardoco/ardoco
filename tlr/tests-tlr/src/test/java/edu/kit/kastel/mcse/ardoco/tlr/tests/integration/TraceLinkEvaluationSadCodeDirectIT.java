/* Licensed under MIT 2023-2024. */
package edu.kit.kastel.mcse.ardoco.tlr.tests.integration;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.collections.api.tuple.Pair;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import edu.kit.kastel.mcse.ardoco.core.api.output.ArDoCoResult;
import edu.kit.kastel.mcse.ardoco.core.tests.eval.CodeProject;
import edu.kit.kastel.mcse.ardoco.core.tests.eval.GoldStandardProject;
import edu.kit.kastel.mcse.ardoco.core.tests.eval.results.EvaluationResults;
import edu.kit.kastel.mcse.ardoco.tlr.tests.integration.tlrhelper.ModelElementSentenceLink;

class TraceLinkEvaluationSadCodeDirectIT {
    protected static final String LOGGING_ARDOCO_CORE = "org.slf4j.simpleLogger.log.edu.kit.kastel.mcse.ardoco.core";

    protected static final List<Pair<GoldStandardProject, EvaluationResults<ModelElementSentenceLink>>> RESULTS = new ArrayList<>();
    protected static final Map<GoldStandardProject, ArDoCoResult> DATA_MAP = new LinkedHashMap<>();

    @BeforeAll
    static void beforeAll() {
        System.setProperty(LOGGING_ARDOCO_CORE, "info");
    }

    @AfterAll
    static void afterAll() {
        System.setProperty(LOGGING_ARDOCO_CORE, "error");
    }

    @DisplayName("Evaluate SAD-Code TLR")
    @ParameterizedTest(name = "{0}")
    @EnumSource(CodeProject.class)
    void evaluateSadCodeTlrIT(CodeProject project) {
        var evaluation = new SadCodeTraceabilityLinkRecoveryEvaluation(true);
        ArDoCoResult results = evaluation.runTraceLinkEvaluation(project);
        Assertions.assertNotNull(results);
    }
}
