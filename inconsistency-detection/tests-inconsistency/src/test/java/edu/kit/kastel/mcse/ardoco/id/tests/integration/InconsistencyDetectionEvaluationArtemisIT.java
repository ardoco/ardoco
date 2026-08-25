/* Licensed under MIT 2026. */
package edu.kit.kastel.mcse.ardoco.id.tests.integration;

import static edu.kit.kastel.mcse.ardoco.id.tests.integration.inconsistencyhelper.InconsistencyDetectionEvaluationUtil.logExplicitResults;
import static edu.kit.kastel.mcse.ardoco.id.tests.integration.inconsistencyhelper.InconsistencyDetectionEvaluationUtil.logResults;

import java.util.Map;

import org.eclipse.collections.api.factory.Lists;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.kit.kastel.mcse.ardoco.core.api.output.ArdocoResult;
import edu.kit.kastel.mcse.ardoco.id.tests.integration.inconsistencyhelper.artemis.ArtemisEvaluationRun;
import edu.kit.kastel.mcse.ardoco.id.tests.integration.inconsistencyhelper.artemis.ArtemisInconsistencyApproach;
import edu.kit.kastel.mcse.ardoco.id.tests.integration.inconsistencyhelper.artemis.ArtemisInconsistencyEvaluationWriter;
import edu.kit.kastel.mcse.ardoco.id.tests.integration.inconsistencyhelper.artemis.ArtemisInconsistencyEvaluator;
import edu.kit.kastel.mcse.ardoco.id.tests.integration.inconsistencyhelper.artemis.ArtemisInconsistencyRunProducer;
import edu.kit.kastel.mcse.ardoco.id.tests.integration.inconsistencyhelper.artemis.ClassArtemisInconsistencyEvaluator;
import edu.kit.kastel.mcse.ardoco.id.tests.integration.inconsistencyhelper.artemis.ComponentArtemisInconsistencyEvaluator;
import edu.kit.kastel.mcse.ardoco.id.tests.tasks.ArtemisInconsistencyTask;
import edu.kit.kastel.mcse.ardoco.id.tests.tasks.ClassArtemisInconsistencyTask;
import edu.kit.kastel.mcse.ardoco.id.tests.tasks.ComponentArtemisInconsistencyTask;
import edu.kit.kastel.mcse.ardoco.metrics.ClassificationMetricsCalculator;
import edu.kit.kastel.mcse.ardoco.metrics.result.AggregationType;
import edu.kit.kastel.mcse.ardoco.metrics.result.SingleClassificationResult;
import edu.kit.kastel.mcse.ardoco.tlr.models.informants.LargeLanguageModel;

/**
 * Integration test that evaluates inconsistency detection based on ArTEMiS.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class InconsistencyDetectionEvaluationArtemisIT {
    private static final Logger logger = LoggerFactory.getLogger(InconsistencyDetectionEvaluationArtemisIT.class);

    private static final LargeLanguageModel LLM = LargeLanguageModel.GPT_5_5;

    @DisplayName("Evaluate ArTEMiS Component MEAT inconsistency detection")
    @ParameterizedTest(name = "Evaluating ArTEMiS Component MEAT for {0}")
    @EnumSource(value = ComponentArtemisInconsistencyTask.class, names = { "BIGBLUEBUTTON" })
    //@EnumSource(ComponentArtemisInconsistencyTask.class) //use this for all evaluation projects
    @Order(1)
    void componentMeatInconsistencyIT(ComponentArtemisInconsistencyTask project) {
        runMeatEvaluation(project, ArtemisInconsistencyApproach.COMPONENT, new ComponentArtemisInconsistencyEvaluator());
    }

    @DisplayName("Evaluate ArTEMiS Component TEAM inconsistency detection")
    @ParameterizedTest(name = "Evaluating ArTEMiS Component TEAM for {0}")
    @EnumSource(ComponentArtemisInconsistencyTask.class)
    @Order(2)
    void componentTeamInconsistencyIT(ComponentArtemisInconsistencyTask project) {
        runTeamEvaluation(project, ArtemisInconsistencyApproach.COMPONENT, new ComponentArtemisInconsistencyEvaluator());
    }

    @DisplayName("Evaluate ArTEMiS Class TEAM inconsistency detection")
    @ParameterizedTest(name = "Evaluating ArTEMiS Class TEAM for {0}")
    @EnumSource(value = ClassArtemisInconsistencyTask.class, names = { "TEAMMATES" })
    @Order(3)
    void classTeamInconsistencyIT(ClassArtemisInconsistencyTask project) {
        runTeamEvaluation(project, ArtemisInconsistencyApproach.CLASS, new ClassArtemisInconsistencyEvaluator());
    }

    @DisplayName("Evaluate ArTEMiS Class TEAM inconsistency detection using holdbacks")
    @ParameterizedTest(name = "Evaluating ArTEMiS Class TEAM holdback for {0}")
    @EnumSource(value = ClassArtemisInconsistencyTask.class, names = { "TEAMMATES" })
    @Order(4)
    void classTeamHoldbackInconsistencyIT(ClassArtemisInconsistencyTask project) {
        int numberOfRuns = 3;
        int numberOfHeldBackClassesPerRun = 2;
        long seed = 42L;

        logger.info("Start ArTEMiS Class TEAM holdback evaluation for project {}", project.getEvaluationProject().name());

        var approach = ArtemisInconsistencyApproach.CLASS;
        var producer = new ArtemisInconsistencyRunProducer(LLM, approach);
        Map<ArtemisEvaluationRun, ArdocoResult> runs = producer.produceClassHoldBackTeamRuns(project, numberOfRuns, numberOfHeldBackClassesPerRun, seed);

        evaluateAndWriteTeamResults(project, approach, new ClassArtemisInconsistencyEvaluator(), runs, " TEAM holdback");
    }

    private <T extends ArtemisInconsistencyTask> void runMeatEvaluation(T project, ArtemisInconsistencyApproach approach,
            ArtemisInconsistencyEvaluator<T> evaluator) {
        Assumptions.assumeTrue(approach.supportsMeat(), () -> approach + " does not support MEAT evaluation");

        logger.info("Start ArTEMiS MEAT evaluation for project {} using approach {}", project.getEvaluationProject().name(), approach);

        var producer = new ArtemisInconsistencyRunProducer(LLM, approach);
        ArdocoResult baseRun = producer.produceBaseRun(project);

        Assertions.assertNotNull(baseRun, "Base run must not be null");

        SingleClassificationResult<String> result = evaluator.evaluateMeat(project, baseRun);

        logExplicitResults(logger, project.getEvaluationProject().name() + " " + approach.getDisplayName() + " MEAT", result);
        ArtemisInconsistencyEvaluationWriter.writeMeatResult(project, approach, result);
    }

    private <T extends ArtemisInconsistencyTask> void runTeamEvaluation(T project, ArtemisInconsistencyApproach approach,
            ArtemisInconsistencyEvaluator<T> evaluator) {
        Assumptions.assumeTrue(approach.supportsTeam(), () -> approach + " does not support TEAM evaluation");

        logger.info("Start ArTEMiS TEAM evaluation for project {} using approach {}", project.getEvaluationProject().name(), approach);

        var producer = new ArtemisInconsistencyRunProducer(LLM, approach);
        Map<ArtemisEvaluationRun, ArdocoResult> runs = producer.produceTeamRuns(project);

        evaluateAndWriteTeamResults(project, approach, evaluator, runs, " TEAM");
    }

    private <T extends ArtemisInconsistencyTask> void evaluateAndWriteTeamResults(T project, ArtemisInconsistencyApproach approach,
            ArtemisInconsistencyEvaluator<T> evaluator, Map<ArtemisEvaluationRun, ArdocoResult> runs, String logSuffix) {
        Assertions.assertNotNull(runs, "Runs must not be null");
        Assertions.assertFalse(runs.isEmpty(), "Runs must not be empty");

        var results = Lists.mutable.withAll(evaluator.evaluateTeam(project, runs));

        var metrics = ClassificationMetricsCalculator.getInstance();
        var microAverage = metrics.calculateAverages(results, null)
                .stream()
                .filter(it -> it.getType() == AggregationType.MICRO_AVERAGE)
                .findFirst()
                .orElseThrow();

        logResults(logger, project.getEvaluationProject().name() + " " + approach.getDisplayName() + logSuffix, microAverage);
        ArtemisInconsistencyEvaluationWriter.writeTeamResult(project, approach, results, runs);
    }
}
