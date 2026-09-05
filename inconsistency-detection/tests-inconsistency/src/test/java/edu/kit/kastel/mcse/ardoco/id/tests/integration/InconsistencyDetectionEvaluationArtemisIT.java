/* Licensed under MIT 2026. */
package edu.kit.kastel.mcse.ardoco.id.tests.integration;

import static edu.kit.kastel.mcse.ardoco.id.tests.integration.inconsistencyhelper.InconsistencyDetectionEvaluationUtil.logExplicitResults;
import static edu.kit.kastel.mcse.ardoco.id.tests.integration.inconsistencyhelper.InconsistencyDetectionEvaluationUtil.logResults;

import java.util.Map;

import edu.kit.kastel.mcse.ardoco.id.tests.integration.inconsistencyhelper.artemis.evalruns.ArtemisEvaluationRun;
import edu.kit.kastel.mcse.ardoco.id.tests.integration.inconsistencyhelper.artemis.evaluators.ArtemisInconsistencyEvaluator;
import edu.kit.kastel.mcse.ardoco.id.tests.integration.inconsistencyhelper.artemis.evaluators.ClassArtemisInconsistencyEvaluator;
import edu.kit.kastel.mcse.ardoco.id.tests.integration.inconsistencyhelper.artemis.evaluators.ComponentArtemisInconsistencyEvaluator;
import edu.kit.kastel.mcse.ardoco.id.tests.integration.inconsistencyhelper.artemis.evaluators.DatafileArtemisInconsistencyEvaluator;
import edu.kit.kastel.mcse.ardoco.id.tests.integration.inconsistencyhelper.artemis.runproducer.ArtemisInconsistencyRunProducer;
import edu.kit.kastel.mcse.ardoco.id.tests.integration.inconsistencyhelper.artemis.runproducer.ClassHoldBackArtemisInconsistencyRunProducer;
import edu.kit.kastel.mcse.ardoco.id.tests.integration.inconsistencyhelper.artemis.runproducer.ComponentHoldBackArtemisInconsistencyRunProducer;
import edu.kit.kastel.mcse.ardoco.id.tests.integration.inconsistencyhelper.artemis.runproducer.SingleArtemisInconsistencyRunProducer;

import edu.kit.kastel.mcse.ardoco.id.tests.tasks.DatafileArtemisInconsistencyTask;

import org.eclipse.collections.api.factory.Lists;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.kit.kastel.mcse.ardoco.core.api.output.ArdocoResult;
import edu.kit.kastel.mcse.ardoco.id.tests.integration.inconsistencyhelper.artemis.*;
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
        var configuration = ArtemisInconsistencyEvaluationConfiguration.component();
        var producer = new SingleArtemisInconsistencyRunProducer<ComponentArtemisInconsistencyTask>(LLM, configuration);

        runMeatEvaluation(project, configuration, producer, new ComponentArtemisInconsistencyEvaluator());
    }

    @DisplayName("Evaluate ArTEMiS Component TEAM inconsistency detection")
    @ParameterizedTest(name = "Evaluating ArTEMiS Component TEAM for {0}")
    @EnumSource(ComponentArtemisInconsistencyTask.class)
    @Order(2)
    void componentTeamInconsistencyIT(ComponentArtemisInconsistencyTask project) {
        var configuration = ArtemisInconsistencyEvaluationConfiguration.component();
        var producer = new ComponentHoldBackArtemisInconsistencyRunProducer(LLM);

        runTeamEvaluation(project, configuration, producer, new ComponentArtemisInconsistencyEvaluator(), "_TEAM");
    }

    @DisplayName("Evaluate ArTEMiS Class TEAM inconsistency detection")
    @ParameterizedTest(name = "Evaluating ArTEMiS Class TEAM for {0}")
    @EnumSource(value = ClassArtemisInconsistencyTask.class, names = { "TEAMMATES" })
    @Order(3)
    void classTeamInconsistencyIT(ClassArtemisInconsistencyTask project) {
        var configuration = ArtemisInconsistencyEvaluationConfiguration.clazz();
        var producer = new SingleArtemisInconsistencyRunProducer<ClassArtemisInconsistencyTask>(LLM, configuration);

        runTeamEvaluation(project, configuration, producer, new ClassArtemisInconsistencyEvaluator(), "_TEAM");
    }

    @DisplayName("Evaluate ArTEMiS Class TEAM inconsistency detection using holdbacks")
    @ParameterizedTest(name = "Evaluating ArTEMiS Class TEAM holdback for {0}")
    @EnumSource(value = ClassArtemisInconsistencyTask.class, names = { "TEAMMATES" })
    @Order(4)
    void classTeamHoldbackInconsistencyIT(ClassArtemisInconsistencyTask project) {
        int numberOfRuns = 3;
        int numberOfHeldBackClassesPerRun = 2;
        long seed = 42L;

        var configuration = ArtemisInconsistencyEvaluationConfiguration.clazz();
        var producer = new ClassHoldBackArtemisInconsistencyRunProducer(LLM, numberOfRuns, numberOfHeldBackClassesPerRun, seed);

        runTeamEvaluation(project, configuration, producer, new ClassArtemisInconsistencyEvaluator(), "_TEAM holdback");
    }

    @DisplayName("Evaluate ArTEMiS Datafile TEAM inconsistency detection")
    @ParameterizedTest(name = "Evaluating ArTEMiS Datafile TEAM for {0}")
    @EnumSource(DatafileArtemisInconsistencyTask.class)
    @Order(5)
    void datafileTeamInconsistencyIT(DatafileArtemisInconsistencyTask project) {
        var configuration = ArtemisInconsistencyEvaluationConfiguration.datafile();
        var producer = new SingleArtemisInconsistencyRunProducer<DatafileArtemisInconsistencyTask>(LLM, configuration);

        runTeamEvaluation(project, configuration, producer, new DatafileArtemisInconsistencyEvaluator(), "_TEAM");
    }

    private <T extends ArtemisInconsistencyTask> void runMeatEvaluation(T project, ArtemisInconsistencyEvaluationConfiguration configuration,
            SingleArtemisInconsistencyRunProducer<T> producer, ArtemisInconsistencyEvaluator<T> evaluator) {
        logger.info("Start ArTEMiS MEAT evaluation for project {} using {}", project.getEvaluationProject().name(), configuration.name());

        ArdocoResult baseRun = producer.produceBaseRun(project);

        Assertions.assertNotNull(baseRun, "Base run must not be null");

        SingleClassificationResult<String> result = evaluator.evaluateMeat(project, baseRun);

        logExplicitResults(logger, project.getEvaluationProject().name() + " " + configuration.name() + " MEAT", result);
        ArtemisInconsistencyEvaluationWriter.writeMeatResult(project, configuration, result);
    }

    private <T extends ArtemisInconsistencyTask> void runTeamEvaluation(T project, ArtemisInconsistencyEvaluationConfiguration configuration,
            ArtemisInconsistencyRunProducer<T> producer, ArtemisInconsistencyEvaluator<T> evaluator, String logSuffix) {
        logger.info("Start ArTEMiS TEAM evaluation for project {} using {}", project.getEvaluationProject().name(), configuration.name());

        Map<ArtemisEvaluationRun, ArdocoResult> runs = producer.produceRuns(project);

        evaluateAndWriteTeamResults(project, configuration, evaluator, runs, logSuffix);
    }

    private <T extends ArtemisInconsistencyTask> void evaluateAndWriteTeamResults(T project, ArtemisInconsistencyEvaluationConfiguration configuration,
            ArtemisInconsistencyEvaluator<T> evaluator, Map<ArtemisEvaluationRun, ArdocoResult> runs, String logSuffix) {
        Assertions.assertNotNull(runs, "Runs must not be null");
        Assertions.assertFalse(runs.isEmpty(), "Runs must not be empty");

        var results = Lists.mutable.withAll(evaluator.evaluateTeam(project, runs));

        var metrics = ClassificationMetricsCalculator.getInstance();
        var microAverage = metrics.calculateAverages(results, null)
                .stream()
                .filter(it -> it.getType() == AggregationType.WEIGHTED_AVERAGE)
                .findFirst()
                .orElseThrow();

        logResults(logger, project.getEvaluationProject().name() + " " + configuration.name() + logSuffix, microAverage);
        ArtemisInconsistencyEvaluationWriter.writeTeamResult(project, configuration, results, runs, logSuffix);
    }
}
