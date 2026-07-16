/* Licensed under MIT 2026. */
package edu.kit.kastel.mcse.ardoco.tlr.tests.integration.evaluation;

import static edu.kit.kastel.mcse.ardoco.core.api.models.Metamodel.ARCHITECTURE_WITH_COMPONENTS;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.eclipse.collections.api.factory.SortedMaps;
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
 * Runs SwATTR once with whatever thresholds are in CommonTextToolsConfig.properties.
 * Used by the shell script run_swattr_threshold_comparison.sh which modifies
 * the properties file between invocations.
 */
class SwattrHyperparamSingle {

    @Test
    @DisplayName("SwATTR Single Config Run")
    void runAll() throws IOException {
        // Print current thresholds
        System.out.printf("=== CURRENT THRESHOLDS ===%n");
        System.out.printf("  JaroWinkler threshold: %.4f%n", CommonTextToolsConfig.JAROWINKLER_SIMILARITY_THRESHOLD);
        System.out.printf("  Levenshtein threshold: %.4f%n", CommonTextToolsConfig.LEVENSHTEIN_THRESHOLD);
        System.out.printf("  Levenshtein maxDist:   %d%n", CommonTextToolsConfig.LEVENSHTEIN_MAX_DISTANCE);
        System.out.printf("  MinProportion:         %.4f%n", CommonTextToolsConfig.GET_MOST_RECOMMENDED_I_BY_REF_MIN_PROPORTION);
        System.out.println();

        File reportDir = new File("target", "swattr-single-results");
        reportDir.mkdirs();

        double sumF1 = 0;
        int count = 0;

        StringBuilder summary = new StringBuilder();
        summary.append(String.format("%-16s | %-40s%n", "Dataset", "P / R / F1"));
        summary.append("-".repeat(60)).append("\n");

        for (SwattrEvaluationProject project : SwattrEvaluationProject.values()) {
            String name = project.name().toLowerCase();

            File textFile = project.getTlrTask().getTextFile();
            File modelFile = project.getTlrTask().getArchitectureModelFile(ModelFormat.PCM);
            File outDir = new File("target", name + "-swattr-single");
            outDir.mkdirs();

            System.out.printf("  Running %s...%n", name);
            var runner = new Swattr(name);
            runner.setUp(textFile, new ArchitectureConfiguration(modelFile, ModelFormat.PCM), SortedMaps.immutable.empty(), outDir);
            var result = runner.run();

            var gold = project.getTlrTask().getExpectedTraceLinks();
            var metrics = analyze(name, result, gold, reportDir);

            summary.append(String.format("%-16s | P=%.3f R=%.3f F1=%.3f (%dTP %dFP %dFN)%n",
                    name, metrics.precision, metrics.recall, metrics.f1, metrics.tp, metrics.fp, metrics.fn));
            sumF1 += metrics.f1;
            count++;
        }

        summary.append("-".repeat(60)).append("\n");
        summary.append(String.format("%-16s | F1=%.3f%n", "MACRO AVG", sumF1 / count));

        System.out.println("\n" + summary);
    }

    private Metrics analyze(String projectName, ArdocoResult result,
            List<Pair<Integer, String>> goldStandard, File reportDir) throws IOException {

        var connectionState = result.getConnectionState(ARCHITECTURE_WITH_COMPONENTS);
        if (connectionState == null) {
            return new Metrics(0, 0, 0, 0, 0, 0);
        }

        var traceLinks = connectionState.getTraceLinks();
        var computedKeys = new TreeSet<String>();
        List<LinkInfo> computed = new ArrayList<>();

        for (TraceLink<SentenceEntity, ModelEntity> tl : traceLinks) {
            int sentNum = tl.getFirstEndpoint().getSentence().getSentenceNumber() + 1;
            String meId = tl.getSecondEndpoint().getId();
            String meName = tl.getSecondEndpoint().getName();
            double conf = 1.0;
            String sentText = tl.getFirstEndpoint().getSentence().getText();
            String key = sentNum + " -> " + meId;
            computedKeys.add(key);
            computed.add(new LinkInfo(sentNum, meId, meName, conf, sentText, key));
        }

        var goldKeys = goldStandard.stream().map(p -> p.first() + " -> " + p.second())
                .collect(Collectors.toCollection(TreeSet::new));

        List<LinkInfo> tps = new ArrayList<>();
        List<LinkInfo> fps = new ArrayList<>();
        List<String> fns = new ArrayList<>();

        for (var c : computed) {
            if (goldKeys.contains(c.key)) tps.add(c);
            else fps.add(c);
        }
        for (String g : goldKeys) {
            if (!computedKeys.contains(g)) fns.add(g);
        }

        double p = tps.size() + fps.size() > 0 ? (double) tps.size() / (tps.size() + fps.size()) : 0;
        double r = tps.size() + fns.size() > 0 ? (double) tps.size() / (tps.size() + fns.size()) : 0;
        double f1 = p + r > 0 ? 2 * p * r / (p + r) : 0;

        // Write detailed report
        File reportFile = new File(reportDir, projectName + "_single.txt");
        try (PrintWriter w = new PrintWriter(new FileWriter(reportFile))) {
            w.printf("=== %s ===%n", projectName.toUpperCase());
            w.printf("JW=%.4f  LEV=%.4f  LEV_DIST=%d  MIN_PROP=%.4f%n",
                    CommonTextToolsConfig.JAROWINKLER_SIMILARITY_THRESHOLD,
                    CommonTextToolsConfig.LEVENSHTEIN_THRESHOLD,
                    CommonTextToolsConfig.LEVENSHTEIN_MAX_DISTANCE,
                    CommonTextToolsConfig.GET_MOST_RECOMMENDED_I_BY_REF_MIN_PROPORTION);
            w.printf("%nTP=%d  FP=%d  FN=%d  P=%.4f  R=%.4f  F1=%.4f%n%n", tps.size(), fps.size(), fns.size(), p, r, f1);

            w.println("FALSE POSITIVES:");
            fps.stream().sorted(Comparator.comparingDouble(l -> l.confidence)).forEach(l ->
                    w.printf("  [%.4f] S%d -> %s (%s): %.100s%n", l.confidence, l.sentNum, l.meName, l.meId,
                            l.sentText.length() > 100 ? l.sentText.substring(0, 100) + "..." : l.sentText));
            w.println();

            w.println("TRUE POSITIVES (lowest conf first, top 30):");
            tps.stream().sorted(Comparator.comparingDouble(l -> l.confidence)).limit(30).forEach(l ->
                    w.printf("  [%.4f] S%d -> %s (%s): %.100s%n", l.confidence, l.sentNum, l.meName, l.meId,
                            l.sentText.length() > 100 ? l.sentText.substring(0, 100) + "..." : l.sentText));
            w.println();

            w.println("FALSE NEGATIVES:");
            fns.forEach(fn -> w.printf("  %s%n", fn));
        }

        System.out.printf("  %s: P=%.3f R=%.3f F1=%.3f (TP=%d FP=%d FN=%d)%n",
                projectName, p, r, f1, tps.size(), fps.size(), fns.size());

        return new Metrics(p, r, f1, tps.size(), fps.size(), fns.size());
    }

    private record LinkInfo(int sentNum, String meId, String meName, double confidence, String sentText, String key) {}
    private record Metrics(double precision, double recall, double f1, int tp, int fp, int fn) {}
}
