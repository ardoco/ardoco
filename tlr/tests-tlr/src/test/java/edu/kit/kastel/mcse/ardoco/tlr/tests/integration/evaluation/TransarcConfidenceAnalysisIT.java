/* Licensed under MIT 2025-2026. */
package edu.kit.kastel.mcse.ardoco.tlr.tests.integration.evaluation;

import static edu.kit.kastel.mcse.ardoco.core.api.models.Metamodel.ARCHITECTURE_WITH_COMPONENTS;
import static edu.kit.kastel.mcse.ardoco.core.api.models.Metamodel.CODE_WITH_COMPILATION_UNITS;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.eclipse.collections.api.factory.SortedMaps;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import edu.kit.kastel.mcse.ardoco.core.api.entity.ModelEntity;
import edu.kit.kastel.mcse.ardoco.core.api.models.ModelFormat;
import edu.kit.kastel.mcse.ardoco.core.api.output.ArdocoResult;
import edu.kit.kastel.mcse.ardoco.core.api.stage.connectiongenerator.ConnectionStates;
import edu.kit.kastel.mcse.ardoco.core.api.stage.connectiongenerator.RecommendationModelTraceLink;
import edu.kit.kastel.mcse.ardoco.core.api.stage.recommendationgenerator.RecommendedInstance;
import edu.kit.kastel.mcse.ardoco.core.api.tracelink.TraceLink;
import edu.kit.kastel.mcse.ardoco.core.common.tuple.Pair;
import edu.kit.kastel.mcse.ardoco.tlr.execution.Transarc;
import edu.kit.kastel.mcse.ardoco.tlr.models.agents.ArchitectureConfiguration;
import edu.kit.kastel.mcse.ardoco.tlr.models.agents.CodeConfiguration;
import edu.kit.kastel.mcse.ardoco.tlr.tests.approach.TransarcEvaluationProject;

/**
 * Integration test that analyzes TransArc errors and their relationship with confidence values.
 * This test runs TransArc on all datasets and outputs detailed confidence analysis.
 */
class TransarcConfidenceAnalysisIT extends AbstractEvaluation {

    @DisplayName("Analyze TransArc Confidence vs Errors")
    @ParameterizedTest(name = "{0}")
    @EnumSource(TransarcEvaluationProject.class)
    void analyzeConfidenceVsErrors(TransarcEvaluationProject project) throws IOException {
        // Run TransArc
        var result = runTransarc(project);

        // Get gold standard
        var goldStandard = project.getTlrTask().getExpectedTraceLinks();
        goldStandard = enrollGoldStandard(goldStandard, result, CODE_WITH_COMPILATION_UNITS);

        // Analyze confidence
        analyzeConfidenceAndErrors(project.name(), result, goldStandard);
    }

    private ArdocoResult runTransarc(TransarcEvaluationProject project) {
        String projectName = project.name().toLowerCase();
        File textInput = project.getTlrTask().getTextFile();
        ModelFormat architectureModelFormat = ModelFormat.PCM;
        File inputArchitectureModel = project.getTlrTask().getArchitectureModelFile(architectureModelFormat);
        CodeConfiguration inputCode = new CodeConfiguration(project.getTlrTask().getCodeModelFromResources(), CodeConfiguration.CodeConfigurationType.ACM_FILE);
        File outputDirectory = new File("target", projectName + "-confidence-analysis");
        outputDirectory.mkdirs();

        var runner = new Transarc(projectName);
        runner.setUp(textInput, new ArchitectureConfiguration(inputArchitectureModel, architectureModelFormat), inputCode, SortedMaps.immutable.empty(),
                outputDirectory);
        return runner.run();
    }

    private void analyzeConfidenceAndErrors(String projectName, ArdocoResult result, List<Pair<Integer, String>> goldStandard) throws IOException {
        // Get SAD-SAM trace links with confidence
        var connectionStatesOpt = result.dataRepository().getData(ConnectionStates.ID, ConnectionStates.class);
        if (connectionStatesOpt.isEmpty()) {
            logger.error("No ConnectionStates found for {}", projectName);
            return;
        }

        var connectionStates = connectionStatesOpt.get();
        var connectionState = connectionStates.getConnectionState(ARCHITECTURE_WITH_COMPONENTS);
        if (connectionState == null) {
            logger.error("No ConnectionState found for architecture model in {}", projectName);
            return;
        }

        // Get instance links (RecommendationModelTraceLink) with confidence
        var instanceLinks = connectionState.getInstanceLinks();

        // Create a map: sentenceNumber -> (modelElementId -> confidence)
        Map<Integer, Map<String, Double>> sentenceConfidenceMap = new HashMap<>();
        for (TraceLink<RecommendedInstance, ModelEntity> link : instanceLinks) {
            if (link instanceof RecommendationModelTraceLink rmtl) {
                double confidence = rmtl.getConfidence();
                var recommendedInstance = rmtl.getFirstEndpoint();
                var modelEntity = rmtl.getSecondEndpoint();

                for (var nm : recommendedInstance.getNameMappings()) {
                    for (var word : nm.getWords()) {
                        int sentenceNum = word.getSentenceNumber() + 1; // 1-indexed
                        sentenceConfidenceMap.computeIfAbsent(sentenceNum, k -> new HashMap<>()).put(modelEntity.getId(), confidence);
                    }
                }
            }
        }

        // Get SAD-Code trace links
        var sadCodeTls = result.getSadCodeTraceLinks()
                .collect(tl -> tl.getFirstEndpoint().getSentence().getSentenceNumber() + 1 + " -> " + tl.getSecondEndpoint().toString())
                .toSortedSet();

        var goldStandardAsStrings = goldStandard.stream().map(pair -> pair.first() + " -> " + pair.second()).collect(Collectors.toCollection(TreeSet::new));

        // Classify: TP, FP, FN
        List<ErrorEntry> truePositives = new ArrayList<>();
        List<ErrorEntry> falsePositives = new ArrayList<>();
        List<ErrorEntry> falseNegatives = new ArrayList<>();

        for (String claimed : sadCodeTls) {
            String[] parts = claimed.split(" -> ");
            int sentenceNum = Integer.parseInt(parts[0].trim());
            String codeFile = parts[1].trim();

            // Get max confidence for this sentence (across all architecture model links)
            Map<String, Double> sentenceConfs = sentenceConfidenceMap.getOrDefault(sentenceNum, new HashMap<>());
            double maxConfidence = sentenceConfs.isEmpty() ? 0.0 : sentenceConfs.values().stream().mapToDouble(d -> d).max().orElse(0.0);

            if (goldStandardAsStrings.contains(claimed)) {
                truePositives.add(new ErrorEntry(sentenceNum, codeFile, maxConfidence, "TP"));
            } else {
                falsePositives.add(new ErrorEntry(sentenceNum, codeFile, maxConfidence, "FP"));
            }
        }

        for (String expected : goldStandardAsStrings) {
            if (!sadCodeTls.contains(expected)) {
                String[] parts = expected.split(" -> ");
                int sentenceNum = Integer.parseInt(parts[0].trim());
                String codeFile = parts[1].trim();

                // For FN, get the confidence if any architecture link exists for this sentence
                Map<String, Double> sentenceConfs = sentenceConfidenceMap.getOrDefault(sentenceNum, new HashMap<>());
                double maxConfidence = sentenceConfs.isEmpty() ? -1.0 : sentenceConfs.values().stream().mapToDouble(d -> d).max().orElse(-1.0);

                falseNegatives.add(new ErrorEntry(sentenceNum, codeFile, maxConfidence, "FN"));
            }
        }

        // Calculate confidence statistics
        double avgTpConf = truePositives.stream().mapToDouble(e -> e.confidence).average().orElse(0.0);
        double avgFpConf = falsePositives.stream().mapToDouble(e -> e.confidence).average().orElse(0.0);
        double avgFnConf = falseNegatives.stream().filter(e -> e.confidence >= 0).mapToDouble(e -> e.confidence).average().orElse(-1.0);

        // Output analysis
        File outputDir = new File("target", projectName.toLowerCase() + "-confidence-analysis");
        outputDir.mkdirs();

        File reportFile = new File(outputDir, projectName.toLowerCase() + "_confidence_analysis.txt");
        try (PrintWriter writer = new PrintWriter(new FileWriter(reportFile))) {
            writer.println("================================================================================");
            writer.println("CONFIDENCE ANALYSIS FOR: " + projectName);
            writer.println("================================================================================");
            writer.println();
            writer.println("SUMMARY:");
            writer.println("----------------------------------------");
            writer.printf("True Positives:  %d%n", truePositives.size());
            writer.printf("False Positives: %d%n", falsePositives.size());
            writer.printf("False Negatives: %d%n", falseNegatives.size());
            writer.println();

            writer.println("CONFIDENCE STATISTICS:");
            writer.println("----------------------------------------");
            writer.printf("Average TP Confidence: %.4f%n", avgTpConf);
            writer.printf("Average FP Confidence: %.4f%n", avgFpConf);
            writer.printf("Average FN Confidence: %.4f (for sentences with arch links)%n", avgFnConf);
            writer.println();

            // Confidence distribution for FP
            long fpLowConf = falsePositives.stream().filter(e -> e.confidence < 0.5).count();
            long fpMedConf = falsePositives.stream().filter(e -> e.confidence >= 0.5 && e.confidence < 0.8).count();
            long fpHighConf = falsePositives.stream().filter(e -> e.confidence >= 0.8).count();

            writer.println("FALSE POSITIVE CONFIDENCE DISTRIBUTION:");
            writer.println("----------------------------------------");
            writer.printf("Low confidence (<0.5):     %d (%.1f%%)%n", fpLowConf, 100.0 * fpLowConf / Math.max(1, falsePositives.size()));
            writer.printf("Medium confidence (0.5-0.8): %d (%.1f%%)%n", fpMedConf, 100.0 * fpMedConf / Math.max(1, falsePositives.size()));
            writer.printf("High confidence (>=0.8):    %d (%.1f%%)%n", fpHighConf, 100.0 * fpHighConf / Math.max(1, falsePositives.size()));
            writer.println();

            // Confidence distribution for TP
            long tpLowConf = truePositives.stream().filter(e -> e.confidence < 0.5).count();
            long tpMedConf = truePositives.stream().filter(e -> e.confidence >= 0.5 && e.confidence < 0.8).count();
            long tpHighConf = truePositives.stream().filter(e -> e.confidence >= 0.8).count();

            writer.println("TRUE POSITIVE CONFIDENCE DISTRIBUTION:");
            writer.println("----------------------------------------");
            writer.printf("Low confidence (<0.5):     %d (%.1f%%)%n", tpLowConf, 100.0 * tpLowConf / Math.max(1, truePositives.size()));
            writer.printf("Medium confidence (0.5-0.8): %d (%.1f%%)%n", tpMedConf, 100.0 * tpMedConf / Math.max(1, truePositives.size()));
            writer.printf("High confidence (>=0.8):    %d (%.1f%%)%n", tpHighConf, 100.0 * tpHighConf / Math.max(1, truePositives.size()));
            writer.println();

            // Analyze if FPs have lower confidence than TPs
            writer.println("ANALYSIS:");
            writer.println("----------------------------------------");
            if (avgFpConf < avgTpConf) {
                writer.printf("YES - FP average confidence (%.4f) is LOWER than TP (%.4f)%n", avgFpConf, avgTpConf);
                writer.println("=> Errors DO correlate with lower confidence!");
            } else {
                writer.printf("NO - FP average confidence (%.4f) is NOT lower than TP (%.4f)%n", avgFpConf, avgTpConf);
                writer.println("=> Errors do NOT strongly correlate with lower confidence.");
            }
            writer.println();

            // List FPs with confidence
            writer.println("FALSE POSITIVES (sorted by confidence):");
            writer.println("----------------------------------------");
            falsePositives.stream()
                    .sorted(Comparator.comparingDouble(e -> e.confidence))
                    .limit(50)
                    .forEach(e -> writer.printf("  [conf=%.4f] Sentence %d -> %s%n", e.confidence, e.sentence, e.codeFile));
            if (falsePositives.size() > 50) {
                writer.printf("  ... and %d more FPs%n", falsePositives.size() - 50);
            }
            writer.println();

            // List FNs
            writer.println("FALSE NEGATIVES (first 50):");
            writer.println("----------------------------------------");
            falseNegatives.stream().limit(50).forEach(e -> {
                if (e.confidence >= 0) {
                    writer.printf("  [arch conf=%.4f] Sentence %d -> %s%n", e.confidence, e.sentence, e.codeFile);
                } else {
                    writer.printf("  [no arch link] Sentence %d -> %s%n", e.sentence, e.codeFile);
                }
            });
            if (falseNegatives.size() > 50) {
                writer.printf("  ... and %d more FNs%n", falseNegatives.size() - 50);
            }
        }

        logger.info("Confidence analysis written to: {}", reportFile.getAbsolutePath());

        // Also print summary to console
        logger.info("");
        logger.info("=== {} CONFIDENCE ANALYSIS ===", projectName);
        logger.info("TP: {}, FP: {}, FN: {}", truePositives.size(), falsePositives.size(), falseNegatives.size());
        logger.info("Avg TP Conf: {}, Avg FP Conf: {}", String.format("%.4f", avgTpConf), String.format("%.4f", avgFpConf));
        if (avgFpConf < avgTpConf) {
            logger.info("=> ERRORS CORRELATE WITH LOW CONFIDENCE");
        } else {
            logger.info("=> Errors do NOT strongly correlate with low confidence");
        }
    }

    private record ErrorEntry(int sentence, String codeFile, double confidence, String type) {
    }
}
