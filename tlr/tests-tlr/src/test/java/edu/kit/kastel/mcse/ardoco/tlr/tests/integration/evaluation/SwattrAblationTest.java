/* Licensed under MIT 2026. */
package edu.kit.kastel.mcse.ardoco.tlr.tests.integration.evaluation;

import static edu.kit.kastel.mcse.ardoco.core.api.models.Metamodel.ARCHITECTURE_WITH_COMPONENTS;

import java.io.File;
import java.util.List;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.eclipse.collections.api.factory.SortedMaps;
import org.eclipse.collections.api.map.sorted.ImmutableSortedMap;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import edu.kit.kastel.mcse.ardoco.core.api.entity.ModelEntity;
import edu.kit.kastel.mcse.ardoco.core.api.models.ModelFormat;
import edu.kit.kastel.mcse.ardoco.core.api.output.ArdocoResult;
import edu.kit.kastel.mcse.ardoco.core.api.text.SentenceEntity;
import edu.kit.kastel.mcse.ardoco.core.api.tracelink.TraceLink;
import edu.kit.kastel.mcse.ardoco.core.common.tuple.Pair;
import edu.kit.kastel.mcse.ardoco.core.common.util.CommonTextToolsConfig;
import edu.kit.kastel.mcse.ardoco.tlr.execution.Swattr;
import edu.kit.kastel.mcse.ardoco.tlr.models.agents.ArchitectureConfiguration;
import edu.kit.kastel.mcse.ardoco.tlr.tests.approach.SwattrEvaluationProject;

/**
 * Ablation test for SWATTR parameter sweep. Reads @Configurable overrides from
 * system properties prefixed with "ablation." and passes them as additionalConfigs.
 * Outputs results in a machine-parseable format for the Python orchestration script.
 *
 * Usage: mvn test -pl tlr/tests-tlr -Dtest=SwattrAblationTest
 *        -Dablation.MappingCombinerInformant::minCosineSimilarity=0.3
 */
class SwattrAblationTest {

    private static final String ABLATION_PREFIX = "ablation.";

    @Test
    @DisplayName("SwATTR Ablation Parameter Sweep")
    void runAblation() {
        ImmutableSortedMap<String, String> additionalConfigs = collectAblationConfigs();

        System.out.println("=== SWATTR ABLATION TEST ===");
        System.out.printf("JaroWinkler threshold: %.4f%n", CommonTextToolsConfig.JAROWINKLER_SIMILARITY_THRESHOLD);
        System.out.printf("MinProportion: %.4f%n", CommonTextToolsConfig.GET_MOST_RECOMMENDED_I_BY_REF_MIN_PROPORTION);
        System.out.printf("AdditionalConfigs: %s%n", additionalConfigs);
        System.out.println();

        for (SwattrEvaluationProject project : SwattrEvaluationProject.values()) {
            String name = project.name();
            try {
                var metrics = runProject(project, additionalConfigs);
                System.out.printf("ABLATION_RESULT|%s|P=%.6f|R=%.6f|F1=%.6f|TP=%d|FP=%d|FN=%d%n",
                        name, metrics.precision, metrics.recall, metrics.f1,
                        metrics.tp, metrics.fp, metrics.fn);
            } catch (Exception e) {
                System.out.printf("ABLATION_RESULT|%s|ERROR=%s%n", name, e.getMessage());
            }
        }
        System.out.println("=== ABLATION TEST COMPLETE ===");
    }

    private ImmutableSortedMap<String, String> collectAblationConfigs() {
        var props = System.getProperties();
        java.util.TreeMap<String, String> configs = new java.util.TreeMap<>();
        for (String key : props.stringPropertyNames()) {
            if (key.startsWith(ABLATION_PREFIX)) {
                String configKey = key.substring(ABLATION_PREFIX.length());
                configs.put(configKey, props.getProperty(key));
            }
        }
        return SortedMaps.immutable.ofSortedMap(configs);
    }

    private Metrics runProject(SwattrEvaluationProject project,
            ImmutableSortedMap<String, String> additionalConfigs) {
        String name = project.name().toLowerCase();
        File textFile = project.getTlrTask().getTextFile();
        File modelFile = project.getTlrTask().getArchitectureModelFile(ModelFormat.PCM);
        File outDir = new File("target", name + "-ablation");
        outDir.mkdirs();

        var runner = new Swattr(name);
        runner.setUp(textFile, new ArchitectureConfiguration(modelFile, ModelFormat.PCM),
                additionalConfigs, outDir);
        var result = runner.run();

        var gold = project.getTlrTask().getExpectedTraceLinks();
        return computeMetrics(result, gold);
    }

    private Metrics computeMetrics(ArdocoResult result,
            List<Pair<Integer, String>> goldStandard) {
        var connectionState = result.getConnectionState(ARCHITECTURE_WITH_COMPONENTS);
        if (connectionState == null) {
            return new Metrics(0, 0, 0, 0, 0, 0);
        }

        var traceLinks = connectionState.getTraceLinks();
        var computedKeys = new TreeSet<String>();
        for (TraceLink<SentenceEntity, ModelEntity> tl : traceLinks) {
            int sentNum = tl.getFirstEndpoint().getSentence().getSentenceNumber() + 1;
            String meId = tl.getSecondEndpoint().getId();
            computedKeys.add(sentNum + " -> " + meId);
        }

        var goldKeys = goldStandard.stream()
                .map(p -> p.first() + " -> " + p.second())
                .collect(Collectors.toCollection(TreeSet::new));

        int tp = 0, fp = 0, fn = 0;
        for (String key : computedKeys) {
            if (goldKeys.contains(key)) tp++;
            else fp++;
        }
        for (String key : goldKeys) {
            if (!computedKeys.contains(key)) fn++;
        }

        double p = tp + fp > 0 ? (double) tp / (tp + fp) : 0;
        double r = tp + fn > 0 ? (double) tp / (tp + fn) : 0;
        double f1 = p + r > 0 ? 2 * p * r / (p + r) : 0;

        return new Metrics(p, r, f1, tp, fp, fn);
    }

    private record Metrics(double precision, double recall, double f1, int tp, int fp, int fn) {}
}
