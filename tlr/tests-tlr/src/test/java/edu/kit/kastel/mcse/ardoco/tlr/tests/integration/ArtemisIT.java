package edu.kit.kastel.mcse.ardoco.tlr.tests.integration;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.eclipse.collections.api.factory.Lists;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.condition.DisabledIfEnvironmentVariable;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import edu.kit.kastel.mcse.ardoco.core.common.util.Environment;
import edu.kit.kastel.mcse.ardoco.metrics.result.SingleClassificationResult;
import edu.kit.kastel.mcse.ardoco.tlr.models.informants.LargeLanguageModel;
import edu.kit.kastel.mcse.ardoco.tlr.tests.approach.ArtemisEvaluationProject;
import edu.kit.kastel.mcse.ardoco.tlr.tests.approach.ClassArtemisEvaluationProject;
import edu.kit.kastel.mcse.ardoco.tlr.tests.approach.ComponentArtemisEvaluationProject;
import edu.kit.kastel.mcse.ardoco.tlr.tests.approach.DatafileArtemisEvaluationProject;
import edu.kit.kastel.mcse.ardoco.tlr.tests.integration.evaluation.ClassArtemisEvaluation;
import edu.kit.kastel.mcse.ardoco.tlr.tests.integration.evaluation.ComponentArtemisEvaluation;
import edu.kit.kastel.mcse.ardoco.tlr.tests.integration.evaluation.DatafileArtemisEvaluation;

public class ArtemisIT extends AbstractArdocoIT {
    private static final int NUMBER_OF_RUNS = 5;

    @BeforeAll
    static void beforeAll() {
        Assumptions.assumeTrue(Environment.getEnv("OPENAI_API_KEY") != null || Environment.getEnv("OLLAMA_HOST") != null);
        Assumptions.assumeTrue(Environment.getEnv("CI") == null);
    }

    private static <T extends Enum<T>> Stream<Arguments> llmsXProjects(T[] projects) {
        List<Arguments> result = new ArrayList<>();
        for (LargeLanguageModel llm : LargeLanguageModel.values()) {
            for (T project : projects) {
                result.add(Arguments.of(project, llm));
            }
        }
        return result.stream();
    }

    private static Stream<Arguments> llmsXComponentProjects() {
        return llmsXProjects(ComponentArtemisEvaluationProject.values());
    }

    private static Stream<Arguments> llmsXClassProjects() {
        return llmsXProjects(ClassArtemisEvaluationProject.values());
    }

    private static Stream<Arguments> llmsXDatafileProjects() {
        //return llmsXProjects(new DatafileArtemisEvaluationProject[] { DatafileArtemisEvaluationProject.CWA });
        return llmsXProjects(DatafileArtemisEvaluationProject.values());
    }

    @DisabledIfEnvironmentVariable(named = "mutipleRuns", matches = ".*")
    @DisplayName("Evaluate Component ArTEMiS TLR")
    @ParameterizedTest(name = "{0} ({1})")
    @MethodSource("llmsXComponentProjects")
    void evaluateComponentArtemisTlrIT(ArtemisEvaluationProject project, LargeLanguageModel llm) {
        var evaluation = new ComponentArtemisEvaluation(project, llm);
        var result = evaluation.runTraceLinkEvaluation();
        Assertions.assertNotNull(result);
    }

    @DisabledIfEnvironmentVariable(named = "mutipleRuns", matches = ".*")
    @DisplayName("Evaluate Class ArTEMiS TLR")
    @ParameterizedTest(name = "{0} ({1})")
    @MethodSource("llmsXClassProjects")
    void evaluateClassArtemisTlrIT(ArtemisEvaluationProject project, LargeLanguageModel llm) {
        var evaluation = new ClassArtemisEvaluation(project, llm);
        var result = evaluation.runTraceLinkEvaluation();
        Assertions.assertNotNull(result);
    }

    @DisabledIfEnvironmentVariable(named = "mutipleRuns", matches = ".*")
    @DisplayName("Evaluate Datafile ArTEMiS TLR")
    @ParameterizedTest(name = "{0} ({1})")
    @MethodSource("llmsXDatafileProjects")
    void evaluateDatafileArtemisTlrIT(ArtemisEvaluationProject project, LargeLanguageModel llm) {
        var evaluation = new DatafileArtemisEvaluation(project, llm);
        var result = evaluation.runTraceLinkEvaluation();
        Assertions.assertNotNull(result);
    }

    @EnabledIfEnvironmentVariable(named = "mutipleRuns", matches = ".*")
    @DisplayName("Evaluate Component ArTEMiS TLR Multi")
    @ParameterizedTest(name = "{0} ({1})")
    @MethodSource("llmsXComponentProjects")
    void evaluateComponentArtemisTlrMultipleIT(ArtemisEvaluationProject project, LargeLanguageModel llm) {
        logger.warn("Currently, multiple-runs evaluation is not meaningful if the LLM is cached.");

        List<SingleClassificationResult<String>> results = Lists.mutable.empty();
        for (int i = 0; i < NUMBER_OF_RUNS; i++) {
            logger.info("Eval run {}/{} [{},{}]", i + 1, NUMBER_OF_RUNS, project, llm);
            var evaluation = new ComponentArtemisEvaluation(project, llm);
            var result = evaluation.runTraceLinkEvaluation();
            Assertions.assertNotNull(result);
            results.add(result);
        }
        averageAndLog(results);
    }
}
