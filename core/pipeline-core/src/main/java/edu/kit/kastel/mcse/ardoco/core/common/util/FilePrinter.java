/* Licensed under MIT 2021-2025. */
package edu.kit.kastel.mcse.ardoco.core.common.util;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.function.Supplier;

import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.list.MutableList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.kit.kastel.mcse.ardoco.core.api.entity.ArchitectureEntity;
import edu.kit.kastel.mcse.ardoco.core.api.entity.ModelEntity;
import edu.kit.kastel.mcse.ardoco.core.api.output.ArDoCoResult;
import edu.kit.kastel.mcse.ardoco.core.api.stage.inconsistency.InconsistentSentence;
import edu.kit.kastel.mcse.ardoco.core.api.stage.inconsistency.ModelInconsistency;
import edu.kit.kastel.mcse.ardoco.core.api.text.SentenceEntity;
import edu.kit.kastel.mcse.ardoco.core.api.tracelink.TraceLink;
import edu.kit.kastel.mcse.ardoco.core.architecture.Deterministic;
import edu.kit.kastel.mcse.ardoco.core.common.tuple.Pair;

/**
 * The FilePrinter class contains utility methods for writing output files and statistics.
 */
@Deterministic
public final class FilePrinter {

    private static final Logger logger = LoggerFactory.getLogger(FilePrinter.class);

    private static final String LINE_SEPARATOR = System.lineSeparator();
    private static final String ENTRY_SEPARATOR = ",";

    private FilePrinter() {
        throw new IllegalAccessError();
    }

    /**
     * Writes the given text to the file with the specified name/path. Truncates existing files, creates the file if it doesn't exist, and writes in UTF-8
     * encoding.
     *
     * @param filename the name/path of the file
     * @param text     the text to write
     */
    public static void writeToFile(String filename, String text) {
        var file = Paths.get(filename);
        writeToFile(file, text);
    }

    /**
     * Writes the given text to the specified file path. Truncates existing files, creates the file if it doesn't exist, and writes in UTF-8 encoding.
     *
     * @param file the path of the file
     * @param text the text to write
     */
    public static void writeToFile(Path file, String text) {
        file.getParent().toFile().mkdirs();
        try (BufferedWriter writer = Files.newBufferedWriter(file, UTF_8)) {
            writer.write(text);
        } catch (IOException e) {
            logger.error("Could not write to file", e);
        }
    }

    /**
     * Writes inconsistency output to the specified file. Includes both inconsistent sentences and model inconsistencies.
     *
     * @param file         the file to write the inconsistency output to
     * @param arDoCoResult the ArDoCo result containing inconsistency data
     */
    public static void writeInconsistencyOutput(File file, ArDoCoResult arDoCoResult) {
        MutableList<String> allInconsistencies = Lists.mutable.empty();
        allInconsistencies.addAll(arDoCoResult.getInconsistentSentences().collect(InconsistentSentence::getInfoString).toList());
        allInconsistencies.addAll(arDoCoResult.getAllModelInconsistencies().collect(ModelInconsistency::getReason).toList());
        Supplier<List<String>> outputExtractor = () -> allInconsistencies;
        writeOutput(file, "Inconsistencies", outputExtractor);
    }

    /**
     * Writes traceability link recovery output to the specified file.
     *
     * @param file         the file to write the traceability link output to
     * @param arDoCoResult the ArDoCo result containing trace link data
     */
    public static void writeTraceabilityLinkRecoveryOutput(File file, ArDoCoResult arDoCoResult) {
        Supplier<List<String>> outputExtractor = arDoCoResult::getAllTraceLinksAsBeautifiedStrings;
        writeOutput(file, "Trace Links", outputExtractor);
    }

    private static void writeOutput(File file, String title, Supplier<List<String>> outputSupplier) {
        var outputBuilder = new StringBuilder("# ").append(title);
        outputBuilder.append(LINE_SEPARATOR).append(CommonUtilities.getCurrentTimeAsString());
        outputBuilder.append(LINE_SEPARATOR).append(LINE_SEPARATOR);

        for (var outputString : outputSupplier.get()) {
            outputBuilder.append(outputString);
            outputBuilder.append(LINE_SEPARATOR);
        }

        writeToFile(file.toPath(), outputBuilder.toString());
    }

    /**
     * Writes trace links as Comma-Separated Values files to the specified output directory. Creates separate files for different types of trace links.
     *
     * @param arDoCoResult the ArDoCo result containing trace link data
     * @param outputDir    the directory where Comma-Separated Values files should be written
     */
    public static void writeTraceLinksAsCsv(ArDoCoResult arDoCoResult, File outputDir) {
        String name = arDoCoResult.getProjectName();
        String header;

        var sadSamTls = Lists.immutable.ofAll(arDoCoResult.getArchitectureTraceLinks());
        if (!sadSamTls.isEmpty()) {
            var sadSamTlr = outputDir.toPath().resolve("sadSamTlr_" + name + ".csv");
            header = "modelElementID,sentence";
            var traceLinkStrings = getSadSamTraceLinksAsStringList(sadSamTls);
            writeTraceLinksToCsv(sadSamTlr, header, traceLinkStrings);
        }

        var samCodeTls = Lists.immutable.ofAll(arDoCoResult.getSamCodeTraceLinks());
        if (!samCodeTls.isEmpty()) {
            var samCodeTlr = outputDir.toPath().resolve("samCodeTlr_" + name + ".csv");
            header = "sentenceID,codeID";
            var traceLinkStrings = getSamCodeTraceLinksAsStringList(samCodeTls);
            writeTraceLinksToCsv(samCodeTlr, header, traceLinkStrings);
        }

        var sadCodeTls = Lists.immutable.ofAll(arDoCoResult.getSadCodeTraceLinks());
        if (!sadCodeTls.isEmpty()) {
            var sadCodeTlr = outputDir.toPath().resolve("sadCodeTlr_" + name + ".csv");
            header = "modelElementID,codeId";
            var traceLinkStrings = getSadCodeTraceLinksAsStringList(sadCodeTls);
            writeTraceLinksToCsv(sadCodeTlr, header, traceLinkStrings);
        }

    }

    private static void writeTraceLinksToCsv(Path filePath, String header, ImmutableList<String> traceLinks) {
        try {
            Files.deleteIfExists(filePath);
            Files.createFile(filePath);

            Files.writeString(filePath, header + System.lineSeparator(), StandardOpenOption.APPEND);
            for (String traceLink : traceLinks) {
                Files.writeString(filePath, traceLink + System.lineSeparator(), StandardOpenOption.APPEND);
            }
        } catch (IOException e) {
            logger.warn("An exception occurred when writing trace links to Comma-Separated Values file.", e);
        }
    }

    /**
     * Creates a trace link string from two element IDs.
     *
     * @param firstElementId  the first element ID
     * @param secondElementId the second element ID
     * @return the trace link string
     */
    public static String createTraceLinkString(String firstElementId, String secondElementId) {
        return firstElementId + ENTRY_SEPARATOR + secondElementId;
    }

    /**
     * Converts a list of trace links between sentences and model entities to a list of string representations.
     *
     * @param sadSamTraceLinks the list of trace links
     * @return the list of string representations
     */
    private static ImmutableList<String> getSadSamTraceLinksAsStringList(ImmutableList<TraceLink<SentenceEntity, ModelEntity>> sadSamTraceLinks) {
        return sadSamTraceLinks.collect(tl -> createTraceLinkString(tl.getSecondEndpoint().getId(), String.valueOf(tl.getFirstEndpoint()
                .getSentence()
                .getSentenceNumber() + 1)));
    }

    /**
     * Converts a list of trace links between architecture entities and model entities to a list of string representations.
     *
     * @param samCodeTraceLinks the list of trace links
     * @return the list of string representations
     */
    private static ImmutableList<String> getSamCodeTraceLinksAsStringList(
            ImmutableList<TraceLink<? extends ArchitectureEntity, ? extends ModelEntity>> samCodeTraceLinks) {
        MutableList<String> resultsMut = Lists.mutable.empty();
        for (var traceLink : samCodeTraceLinks) {
            Pair<? extends ArchitectureEntity, ? extends ModelEntity> endpointTuple = traceLink.asPair();
            var modelElement = endpointTuple.first();
            var codeElement = endpointTuple.second();
            String traceLinkString = createTraceLinkString(modelElement.getId(), codeElement.toString());
            resultsMut.add(traceLinkString);
        }
        return resultsMut.toImmutable();
    }

    /**
     * Converts a list of trace links between sentences and model entities to a list of string representations, with sentence number as the first element.
     *
     * @param sadCodeTraceLinks the list of trace links
     * @return the list of string representations
     */
    private static ImmutableList<String> getSadCodeTraceLinksAsStringList(ImmutableList<TraceLink<SentenceEntity, ? extends ModelEntity>> sadCodeTraceLinks) {
        MutableList<String> resultsMut = Lists.mutable.empty();
        for (var traceLink : sadCodeTraceLinks) {
            Pair<SentenceEntity, ? extends ModelEntity> endpointTuple = traceLink.asPair();
            var codeElement = endpointTuple.second();
            String sentenceNumber = String.valueOf(endpointTuple.first().getSentence().getSentenceNumber() + 1);
            String traceLinkString = createTraceLinkString(sentenceNumber, codeElement.toString());
            resultsMut.add(traceLinkString);
        }
        return resultsMut.toImmutable();
    }

}
