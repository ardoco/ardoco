/* Licensed under MIT 2025-2026. */
package edu.kit.kastel.mcse.ardoco.tlr.tests.integration.evaluation;

import static edu.kit.kastel.mcse.ardoco.core.api.models.Metamodel.ARCHITECTURE_WITH_COMPONENTS;
import static edu.kit.kastel.mcse.ardoco.core.api.models.Metamodel.CODE_WITH_COMPILATION_UNITS;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.collections.api.factory.SortedMaps;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import edu.kit.kastel.mcse.ardoco.core.api.entity.Entity;
import edu.kit.kastel.mcse.ardoco.core.api.models.ModelFormat;
import edu.kit.kastel.mcse.ardoco.core.api.output.ArdocoResult;
import edu.kit.kastel.mcse.ardoco.core.api.stage.codetraceability.CodeTraceabilityState;
import edu.kit.kastel.mcse.ardoco.core.api.stage.connectiongenerator.ConnectionStates;
import edu.kit.kastel.mcse.ardoco.core.api.text.SentenceEntity;
import edu.kit.kastel.mcse.ardoco.core.api.tracelink.TraceLink;
import edu.kit.kastel.mcse.ardoco.core.common.tuple.Pair;
import edu.kit.kastel.mcse.ardoco.core.common.util.DataRepositoryHelper;
import edu.kit.kastel.mcse.ardoco.tlr.execution.Transarc;
import edu.kit.kastel.mcse.ardoco.tlr.models.agents.ArchitectureConfiguration;
import edu.kit.kastel.mcse.ardoco.tlr.models.agents.CodeConfiguration;
import edu.kit.kastel.mcse.ardoco.tlr.tests.approach.TransarcEvaluationProject;
import edu.kit.kastel.mcse.ardoco.tlr.tests.task.DocumentationToArchitectureModelTlrTask;
import edu.kit.kastel.mcse.ardoco.tlr.tests.task.ModelToCodeTlrTask;

/**
 * Integration test that analyzes TransArc recall bottleneck.
 * For each False Negative in SAD-Code, this test determines whether the cause is:
 * 1. Missing SAD-SAM link (sentence to model)
 * 2. Missing SAM-Code link (model to code)
 * 3. Both missing
 * 4. Impossible (no transitive path exists even in gold standards)
 */
class TransarcRecallBottleneckAnalysisIT extends AbstractEvaluation {

    @DisplayName("Analyze TransArc Recall Bottleneck")
    @ParameterizedTest(name = "{0}")
    @EnumSource(TransarcEvaluationProject.class)
    void analyzeRecallBottleneck(TransarcEvaluationProject project) throws IOException {
        // Run TransArc
        var result = runTransarc(project);

        // Get all gold standards
        var sadCodeGoldStandard = project.getTlrTask().getExpectedTraceLinks();
        sadCodeGoldStandard = enrollGoldStandard(sadCodeGoldStandard, result, CODE_WITH_COMPILATION_UNITS);

        var sadSamGoldStandard = getSadSamGoldStandard(project);
        var samCodeGoldStandard = getSamCodeGoldStandard(project);

        // Enroll SAM-Code gold standard (expand directories)
        samCodeGoldStandard = enrollSamCodeGoldStandard(samCodeGoldStandard, result, CODE_WITH_COMPILATION_UNITS);

        // Analyze bottleneck
        analyzeBottleneck(project.name(), result, sadCodeGoldStandard, sadSamGoldStandard, samCodeGoldStandard);
    }

    private ArdocoResult runTransarc(TransarcEvaluationProject project) {
        String projectName = project.name().toLowerCase();
        File textInput = project.getTlrTask().getTextFile();
        ModelFormat architectureModelFormat = ModelFormat.PCM;
        File inputArchitectureModel = project.getTlrTask().getArchitectureModelFile(architectureModelFormat);
        CodeConfiguration inputCode = new CodeConfiguration(project.getTlrTask().getCodeModelFromResources(), CodeConfiguration.CodeConfigurationType.ACM_FILE);
        File outputDirectory = new File("target", projectName + "-bottleneck-analysis");
        outputDirectory.mkdirs();

        var runner = new Transarc(projectName);
        runner.setUp(textInput, new ArchitectureConfiguration(inputArchitectureModel, architectureModelFormat), inputCode, SortedMaps.immutable.empty(),
                outputDirectory);
        return runner.run();
    }

    private List<Pair<Integer, String>> getSadSamGoldStandard(TransarcEvaluationProject project) {
        return switch (project) {
            case MEDIASTORE -> DocumentationToArchitectureModelTlrTask.MEDIASTORE.getExpectedTraceLinks();
            case TEASTORE -> DocumentationToArchitectureModelTlrTask.TEASTORE.getExpectedTraceLinks();
            case TEAMMATES -> DocumentationToArchitectureModelTlrTask.TEAMMATES.getExpectedTraceLinks();
            case BIGBLUEBUTTON -> DocumentationToArchitectureModelTlrTask.BIGBLUEBUTTON.getExpectedTraceLinks();
            case JABREF -> DocumentationToArchitectureModelTlrTask.JABREF.getExpectedTraceLinks();
        };
    }

    private List<Pair<String, String>> getSamCodeGoldStandard(TransarcEvaluationProject project) {
        return switch (project) {
            case MEDIASTORE -> ModelToCodeTlrTask.MEDIASTORE.getExpectedTraceLinks();
            case TEASTORE -> ModelToCodeTlrTask.TEASTORE.getExpectedTraceLinks();
            case TEAMMATES -> ModelToCodeTlrTask.TEAMMATES.getExpectedTraceLinks();
            case BIGBLUEBUTTON -> ModelToCodeTlrTask.BIGBLUEBUTTON.getExpectedTraceLinks();
            case JABREF -> ModelToCodeTlrTask.JABREF.getExpectedTraceLinks();
        };
    }

    private List<Pair<String, String>> enrollSamCodeGoldStandard(List<Pair<String, String>> goldStandard, ArdocoResult result,
            edu.kit.kastel.mcse.ardoco.core.api.models.Metamodel metamodel) {
        // Get code model to expand directories
        var codeModel = result.getModelState(metamodel);
        if (codeModel == null) {
            return goldStandard;
        }

        List<Pair<String, String>> expanded = new ArrayList<>();
        for (var pair : goldStandard) {
            String modelElement = pair.first();
            String codeElement = pair.second();

            if (codeElement.endsWith("/")) {
                // It's a directory/package - expand to all files
                String prefix = codeElement.substring(0, codeElement.length() - 1);
                for (var endpoint : codeModel.getEndpoints()) {
                    String endpointPath = endpoint.toString();
                    if (endpointPath.startsWith(prefix)) {
                        expanded.add(new Pair<>(modelElement, endpointPath));
                    }
                }
            } else {
                expanded.add(pair);
            }
        }
        return expanded;
    }

    private void analyzeBottleneck(String projectName, ArdocoResult result, List<Pair<Integer, String>> sadCodeGoldStandard,
            List<Pair<Integer, String>> sadSamGoldStandard, List<Pair<String, String>> samCodeGoldStandard) throws IOException {

        // Build lookup structures from gold standards
        // SAD-SAM: sentence -> set of model elements
        Map<Integer, Set<String>> sadSamGsMap = new HashMap<>();
        for (var pair : sadSamGoldStandard) {
            sadSamGsMap.computeIfAbsent(pair.first(), k -> new HashSet<>()).add(pair.second());
        }

        // SAM-Code: model element -> set of code elements
        Map<String, Set<String>> samCodeGsMap = new HashMap<>();
        for (var pair : samCodeGoldStandard) {
            samCodeGsMap.computeIfAbsent(pair.first(), k -> new HashSet<>()).add(pair.second());
        }

        // Get computed trace links
        // SAD-SAM computed links
        var connectionStatesOpt = result.dataRepository().getData(ConnectionStates.ID, ConnectionStates.class);
        Map<Integer, Set<String>> sadSamComputedMap = new HashMap<>();
        if (connectionStatesOpt.isPresent()) {
            var connectionState = connectionStatesOpt.get().getConnectionState(ARCHITECTURE_WITH_COMPONENTS);
            if (connectionState != null) {
                for (TraceLink<SentenceEntity, ? extends Entity> link : connectionState.getTraceLinks()) {
                    int sentenceNum = link.getFirstEndpoint().getSentence().getSentenceNumber() + 1; // 1-indexed
                    String modelId = link.getSecondEndpoint().getId();
                    sadSamComputedMap.computeIfAbsent(sentenceNum, k -> new HashSet<>()).add(modelId);
                }
            }
        }

        // SAM-Code computed links
        CodeTraceabilityState codeTraceabilityState = DataRepositoryHelper.getCodeTraceabilityState(result.dataRepository());
        Map<String, Set<String>> samCodeComputedMap = new HashMap<>();
        if (codeTraceabilityState != null) {
            for (var link : codeTraceabilityState.getSamCodeTraceLinks()) {
                String modelId = link.getFirstEndpoint().getId();
                String codeId = link.getSecondEndpoint().toString();
                samCodeComputedMap.computeIfAbsent(modelId, k -> new HashSet<>()).add(codeId);
            }
        }

        // Get SAD-Code computed links
        var sadCodeComputed = result.getSadCodeTraceLinks()
                .collect(tl -> new Pair<>(tl.getFirstEndpoint().getSentence().getSentenceNumber() + 1, tl.getSecondEndpoint().toString()))
                .toSet();

        // Convert gold standard to set for comparison
        Set<Pair<Integer, String>> sadCodeGsSet = new HashSet<>(sadCodeGoldStandard);

        // Find True Positives and False Negatives
        List<Pair<Integer, String>> truePositives = new ArrayList<>();
        List<Pair<Integer, String>> falseNegatives = new ArrayList<>();

        for (var expected : sadCodeGsSet) {
            if (sadCodeComputed.contains(expected)) {
                truePositives.add(expected);
            } else {
                falseNegatives.add(expected);
            }
        }

        // Analyze each False Negative
        List<BottleneckEntry> fnAnalysis = new ArrayList<>();

        int missingSadSamOnly = 0;
        int missingSamCodeOnly = 0;
        int missingBoth = 0;
        int impossibleTransitive = 0;
        int otherReason = 0;

        for (var fn : falseNegatives) {
            int sentence = fn.first();
            String codeElement = fn.second();

            // Find model elements that connect sentence to code in gold standard
            Set<String> gsModelsForSentence = sadSamGsMap.getOrDefault(sentence, Set.of());
            Set<String> gsModelsForCode = new HashSet<>();
            for (var entry : samCodeGsMap.entrySet()) {
                if (entry.getValue().contains(codeElement)) {
                    gsModelsForCode.add(entry.getKey());
                }
            }
            Set<String> transitiveModels = new HashSet<>(gsModelsForSentence);
            transitiveModels.retainAll(gsModelsForCode);

            // Check computed links
            Set<String> computedModelsForSentence = sadSamComputedMap.getOrDefault(sentence, Set.of());
            Set<String> computedModelsForCode = new HashSet<>();
            for (var entry : samCodeComputedMap.entrySet()) {
                if (entry.getValue().contains(codeElement)) {
                    computedModelsForCode.add(entry.getKey());
                }
            }

            BottleneckCategory category;
            String details;

            if (transitiveModels.isEmpty()) {
                // No transitive path exists even in gold standard
                impossibleTransitive++;
                category = BottleneckCategory.IMPOSSIBLE_TRANSITIVE;
                details = String.format("No common model element in GS. SAD-SAM GS models: %s, SAM-Code GS models: %s", gsModelsForSentence, gsModelsForCode);
            } else {
                // Transitive path exists in gold standard - check what's missing
                boolean hasSadSam = false;
                boolean hasSamCode = false;

                for (String model : transitiveModels) {
                    if (computedModelsForSentence.contains(model)) {
                        hasSadSam = true;
                    }
                    if (computedModelsForCode.contains(model)) {
                        hasSamCode = true;
                    }
                }

                if (!hasSadSam && !hasSamCode) {
                    missingBoth++;
                    category = BottleneckCategory.MISSING_BOTH;
                    details = String.format("Missing both SAD-SAM and SAM-Code for models: %s", transitiveModels);
                } else if (!hasSadSam) {
                    missingSadSamOnly++;
                    category = BottleneckCategory.MISSING_SAD_SAM;
                    details = String.format("Missing SAD-SAM for models: %s", transitiveModels);
                } else if (!hasSamCode) {
                    missingSamCodeOnly++;
                    category = BottleneckCategory.MISSING_SAM_CODE;
                    details = String.format("Missing SAM-Code for models: %s (computed SAD-SAM models: %s)", transitiveModels, computedModelsForSentence);
                } else {
                    // Both links exist but not connecting through the same model
                    otherReason++;
                    category = BottleneckCategory.OTHER;
                    details = String.format("Links exist but don't connect. SAD-SAM computed: %s, SAM-Code computed for target: %s, needed: %s",
                            computedModelsForSentence, computedModelsForCode, transitiveModels);
                }
            }

            fnAnalysis.add(new BottleneckEntry(sentence, codeElement, category, details));
        }

        // Calculate overall stats
        int totalGoldStandard = sadCodeGsSet.size();
        int totalTP = truePositives.size();
        int totalFN = falseNegatives.size();
        double recall = totalGoldStandard > 0 ? (double) totalTP / totalGoldStandard : 0;

        // Count SAD-SAM and SAM-Code gold standard stats
        int sadSamGsCount = sadSamGoldStandard.size();
        int samCodeGsCount = samCodeGoldStandard.size();

        // Output report
        File outputDir = new File("target", projectName.toLowerCase() + "-bottleneck-analysis");
        outputDir.mkdirs();

        File reportFile = new File(outputDir, projectName.toLowerCase() + "_bottleneck_analysis.txt");
        try (PrintWriter writer = new PrintWriter(new FileWriter(reportFile))) {
            writer.println("================================================================================");
            writer.println("RECALL BOTTLENECK ANALYSIS FOR: " + projectName);
            writer.println("================================================================================");
            writer.println();

            writer.println("GOLD STANDARD STATISTICS:");
            writer.println("----------------------------------------");
            writer.printf("SAD-SAM gold standard links:  %d%n", sadSamGsCount);
            writer.printf("SAM-Code gold standard links: %d%n", samCodeGsCount);
            writer.printf("SAD-Code gold standard links: %d%n", totalGoldStandard);
            writer.println();

            writer.println("RECALL ANALYSIS:");
            writer.println("----------------------------------------");
            writer.printf("True Positives (found):    %d%n", totalTP);
            writer.printf("False Negatives (missed):  %d%n", totalFN);
            writer.printf("Recall:                    %.2f%% (%d/%d)%n", recall * 100, totalTP, totalGoldStandard);
            writer.println();

            writer.println("FALSE NEGATIVE BREAKDOWN:");
            writer.println("----------------------------------------");
            writer.printf("Missing SAD-SAM only:      %d (%.1f%%)%n", missingSadSamOnly, 100.0 * missingSadSamOnly / Math.max(1, totalFN));
            writer.printf("Missing SAM-Code only:     %d (%.1f%%)%n", missingSamCodeOnly, 100.0 * missingSamCodeOnly / Math.max(1, totalFN));
            writer.printf("Missing Both:              %d (%.1f%%)%n", missingBoth, 100.0 * missingBoth / Math.max(1, totalFN));
            writer.printf("Impossible (no GS path):   %d (%.1f%%)%n", impossibleTransitive, 100.0 * impossibleTransitive / Math.max(1, totalFN));
            writer.printf("Other reasons:             %d (%.1f%%)%n", otherReason, 100.0 * otherReason / Math.max(1, totalFN));
            writer.println();

            // Summary interpretation
            writer.println("BOTTLENECK SUMMARY:");
            writer.println("----------------------------------------");
            int recoverableFN = missingSadSamOnly + missingSamCodeOnly + missingBoth;
            writer.printf("Recoverable FNs (has GS path):  %d (%.1f%%)%n", recoverableFN, 100.0 * recoverableFN / Math.max(1, totalFN));
            writer.printf("Unrecoverable FNs (no GS path): %d (%.1f%%)%n", impossibleTransitive + otherReason,
                    100.0 * (impossibleTransitive + otherReason) / Math.max(1, totalFN));
            writer.println();

            if (missingSadSamOnly > missingSamCodeOnly) {
                writer.println(">>> PRIMARY BOTTLENECK: SAD-SAM (sentence to model)");
            } else if (missingSamCodeOnly > missingSadSamOnly) {
                writer.println(">>> PRIMARY BOTTLENECK: SAM-Code (model to code)");
            } else {
                writer.println(">>> BOTTLENECK: Roughly equal between SAD-SAM and SAM-Code");
            }
            writer.println();

            // Detailed FN list grouped by category
            writer.println("DETAILED FALSE NEGATIVES:");
            writer.println("========================================");

            for (BottleneckCategory cat : BottleneckCategory.values()) {
                var entries = fnAnalysis.stream().filter(e -> e.category == cat).toList();
                if (entries.isEmpty())
                    continue;

                writer.println();
                writer.println(cat.name() + " (" + entries.size() + " entries):");
                writer.println("----------------------------------------");
                int count = 0;
                for (var entry : entries) {
                    if (count++ >= 20) {
                        writer.printf("... and %d more%n", entries.size() - 20);
                        break;
                    }
                    writer.printf("  Sentence %d -> %s%n", entry.sentence, entry.codeElement);
                    writer.printf("    %s%n", entry.details);
                }
            }
        }

        logger.info("Bottleneck analysis written to: {}", reportFile.getAbsolutePath());

        // Console summary
        logger.info("");
        logger.info("=== {} BOTTLENECK ANALYSIS ===", projectName);
        logger.info("Gold Standards: SAD-SAM={}, SAM-Code={}, SAD-Code={}", sadSamGsCount, samCodeGsCount, totalGoldStandard);
        logger.info("Recall: {}/{} = {:.1f}%", totalTP, totalGoldStandard, recall * 100);
        logger.info("FN Breakdown: SAD-SAM={}, SAM-Code={}, Both={}, Impossible={}, Other={}", missingSadSamOnly, missingSamCodeOnly, missingBoth,
                impossibleTransitive, otherReason);
        if (missingSadSamOnly > missingSamCodeOnly) {
            logger.info(">>> PRIMARY BOTTLENECK: SAD-SAM");
        } else if (missingSamCodeOnly > missingSadSamOnly) {
            logger.info(">>> PRIMARY BOTTLENECK: SAM-Code");
        } else {
            logger.info(">>> BOTTLENECK: Roughly equal");
        }
    }

    private enum BottleneckCategory {
        MISSING_SAD_SAM,      // SAD-SAM link is missing (sentence -> model not found)
        MISSING_SAM_CODE,     // SAM-Code link is missing (model -> code not found)
        MISSING_BOTH,         // Both links are missing
        IMPOSSIBLE_TRANSITIVE, // No transitive path exists even in gold standard
        OTHER                 // Links exist but don't connect (unexpected)
    }

    private record BottleneckEntry(int sentence, String codeElement, BottleneckCategory category, String details) {
    }
}
