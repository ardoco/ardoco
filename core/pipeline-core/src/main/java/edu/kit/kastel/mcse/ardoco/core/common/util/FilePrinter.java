/* Licensed under MIT 2021-2024. */
package edu.kit.kastel.mcse.ardoco.core.common.util;

import static edu.kit.kastel.mcse.ardoco.core.common.util.DataRepositoryHelper.getConnectionStates;
import static edu.kit.kastel.mcse.ardoco.core.common.util.DataRepositoryHelper.getInconsistencyStates;
import static edu.kit.kastel.mcse.ardoco.core.common.util.DataRepositoryHelper.getModelStatesData;
import static edu.kit.kastel.mcse.ardoco.core.common.util.DataRepositoryHelper.getRecommendationStates;
import static edu.kit.kastel.mcse.ardoco.core.common.util.DataRepositoryHelper.getTextState;
import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.list.MutableList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.kit.kastel.mcse.ardoco.core.api.entity.ArchitectureEntity;
import edu.kit.kastel.mcse.ardoco.core.api.models.Metamodel;
import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.architecture.legacy.LegacyModelExtractionState;
import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.architecture.legacy.ModelInstance;
import edu.kit.kastel.mcse.ardoco.core.api.output.ArDoCoResult;
import edu.kit.kastel.mcse.ardoco.core.api.stage.connectiongenerator.ConnectionState;
import edu.kit.kastel.mcse.ardoco.core.api.stage.connectiongenerator.InstanceLink;
import edu.kit.kastel.mcse.ardoco.core.api.stage.inconsistency.Inconsistency;
import edu.kit.kastel.mcse.ardoco.core.api.stage.inconsistency.InconsistencyState;
import edu.kit.kastel.mcse.ardoco.core.api.stage.inconsistency.InconsistentSentence;
import edu.kit.kastel.mcse.ardoco.core.api.stage.inconsistency.ModelInconsistency;
import edu.kit.kastel.mcse.ardoco.core.api.stage.recommendationgenerator.RecommendationState;
import edu.kit.kastel.mcse.ardoco.core.api.stage.recommendationgenerator.RecommendedInstance;
import edu.kit.kastel.mcse.ardoco.core.api.stage.textextraction.MappingKind;
import edu.kit.kastel.mcse.ardoco.core.api.stage.textextraction.NounMapping;
import edu.kit.kastel.mcse.ardoco.core.api.stage.textextraction.TextState;
import edu.kit.kastel.mcse.ardoco.core.api.text.SentenceEntity;
import edu.kit.kastel.mcse.ardoco.core.api.text.Text;
import edu.kit.kastel.mcse.ardoco.core.api.text.Word;
import edu.kit.kastel.mcse.ardoco.core.api.tracelink.TraceLink;
import edu.kit.kastel.mcse.ardoco.core.architecture.Deterministic;

/**
 * The Class FilePrinter contains some helpers for stats.
 */
@Deterministic
public final class FilePrinter {
    private static final String DELIMITER = ",";

    private static final Logger logger = LoggerFactory.getLogger(FilePrinter.class);

    private static final String GENERIC_ERROR = "An error occurred.";
    private static final String SUCCESS_WRITE = "Successfully wrote to the file.";

    private static final String HORIZONTAL_RULE = "---------------------------------------------------------------------------------------------------------------------------------------------";
    private static final String LINE_SEPARATOR = System.lineSeparator();

    private FilePrinter() {
        throw new IllegalAccessError();
    }

    /**
     * Prints details of the {@link ArDoCoResult results} of a run into files within the given directory (path). Writes out detailed info about model instances,
     * noun mappings, trace links, all states, and inconsistencies. Uses the provided (project) name as part of the file names.
     *
     * @param path         the directory where the files should be written
     * @param name         name of the project
     * @param arDoCoResult the results that should be written
     */
    public static void printResultsInFiles(Path path, String name, ArDoCoResult arDoCoResult) {
        var outputDir = path.toFile();
        var data = arDoCoResult.dataRepository();
        var textState = getTextState(data);

        InconsistencyState inconsistencyState = null;
        if (DataRepositoryHelper.hasInconsistencyStates(data)) {
            var inconsistencyStates = getInconsistencyStates(data);
            inconsistencyState = inconsistencyStates.getInconsistencyState(Metamodel.ARCHITECTURE);
        }

        for (var model : getModelStatesData(data).modelIds()) {
            var modelState = getModelStatesData(data).getModelExtractionState(model);
            var metaModel = modelState.getMetamodel();
            var recommendationState = getRecommendationStates(data).getRecommendationState(metaModel);
            var connectionState = getConnectionStates(data).getConnectionState(metaModel);
            String metaModelTypeName = metaModel.toString().toLowerCase();

            FilePrinter.writeModelInstancesInCsvFile(Path.of(outputDir.getAbsolutePath(), name + "-instances-" + metaModelTypeName + model + ".csv").toFile(),
                    modelState, name);
            FilePrinter.writeNounMappingsInCsvFile(Path.of(outputDir.getAbsolutePath(), name + "_noun_mappings" + model + ".csv").toFile(), //
                    textState);
            FilePrinter.writeTraceLinksInCsvFile(Path.of(outputDir.getAbsolutePath(), name + "_trace_links" + model + ".csv").toFile(), //
                    connectionState);
            FilePrinter.writeStatesToFile(Path.of(outputDir.getAbsolutePath(), name + "_states" + model + ".csv").toFile(), //
                    modelState, textState, recommendationState, connectionState);

            if (inconsistencyState != null) {
                FilePrinter.writeInconsistenciesToFile(Path.of(outputDir.getAbsolutePath(), name + "_inconsistencies" + model + ".csv").toFile(),
                        inconsistencyState);
            }
        }
    }

    /**
     * Writes the sentences as they are stored in the PARSE graph into a file.
     *
     * @param target the target file
     * @param text   the text to use.
     */
    public static void writeSentencesInFile(File target, Text text) {
        var fileCreated = createFileIfNonExistent(target);
        if (!fileCreated) {
            return;
        }

        try (var myWriter = new FileWriter(target, StandardCharsets.UTF_8)) {
            var minSentenceNumber = 0;
            for (Word node : text.words()) {
                var sentenceNumber = Integer.parseInt(String.valueOf(node.getSentenceNo()));
                if (sentenceNumber + 1 > minSentenceNumber) {
                    myWriter.append(LINE_SEPARATOR).append(String.valueOf(sentenceNumber)).append(": ");
                    minSentenceNumber++;
                }
                myWriter.append(" ").append(node.getText());
            }

            logger.info(SUCCESS_WRITE);
        } catch (IOException | NumberFormatException e) {
            logger.error("An error occurred while writing sentences to file.");
            logger.debug(e.getMessage(), e.getCause());
        }
    }

    private static boolean createFileIfNonExistent(File file) {
        try {
            if (file.createNewFile()) {
                logger.info("File created: {}", file.getAbsolutePath());
            } else {
                logger.info("File already exists.");
            }
        } catch (IOException e) {
            logger.error("An error occured creating a file.");
            logger.debug(e.getMessage(), e.getCause());
            return false;
        }
        return true;
    }

    private static void writeStates(Writer myWriter, LegacyModelExtractionState extractionState, TextState ntrState, //
            RecommendationState recommendationState, ConnectionState connectionState) throws IOException {
        myWriter.write("Results of ModelConnector: ");
        myWriter.append(LINE_SEPARATOR);
        myWriter.write(HORIZONTAL_RULE);
        myWriter.append(LINE_SEPARATOR).append(LINE_SEPARATOR);

        myWriter.write("ExtractorState: ");
        myWriter.append(LINE_SEPARATOR);
        myWriter.write(extractionState.toString());
        myWriter.write(HORIZONTAL_RULE);
        myWriter.append(LINE_SEPARATOR).append(LINE_SEPARATOR);

        myWriter.write("FoundNames as Set: ");
        myWriter.append(LINE_SEPARATOR);
        var nameList = ntrState.getListOfReferences(MappingKind.NAME).toSortedList().toImmutable();
        myWriter.write(nameList.toString() + LINE_SEPARATOR);
        myWriter.write(HORIZONTAL_RULE);
        myWriter.append(LINE_SEPARATOR).append(LINE_SEPARATOR);

        myWriter.write("FoundTypes as Set: ");
        myWriter.append(LINE_SEPARATOR);
        var typeList = ntrState.getListOfReferences(MappingKind.TYPE).toSortedList().toImmutable();
        myWriter.write(typeList.toString() + LINE_SEPARATOR);
        myWriter.write(HORIZONTAL_RULE);
        myWriter.append(LINE_SEPARATOR).append(LINE_SEPARATOR);

        myWriter.write("Instances of the Recommendation State: ");
        myWriter.append(LINE_SEPARATOR);

        var comRecommendedInstanceByName = getRecommendedInstancesComparator();
        var recommendedInstances = recommendationState.getRecommendedInstances().toSortedList(comRecommendedInstanceByName).toImmutable();

        for (RecommendedInstance ri : recommendedInstances) {
            myWriter.write(ri.toString() + LINE_SEPARATOR);
        }
        myWriter.write(HORIZONTAL_RULE);
        myWriter.append(LINE_SEPARATOR).append(LINE_SEPARATOR);

        myWriter.write("Instances of the Connection State: ");
        myWriter.append(LINE_SEPARATOR);

        var compInstByUID = getInstanceLinkComparator();
        var instanceMappings = Lists.immutable.withAll(connectionState.getInstanceLinks()).toSortedList(compInstByUID).toImmutable();

        for (InstanceLink imap : instanceMappings) {

            myWriter.write(imap.toString() + LINE_SEPARATOR);
        }

        myWriter.write(HORIZONTAL_RULE);
        myWriter.append(LINE_SEPARATOR).append(LINE_SEPARATOR);

        myWriter.write("Relations of the Recommendation State: ");
        myWriter.append(LINE_SEPARATOR);

        myWriter.write(HORIZONTAL_RULE);
        myWriter.append(LINE_SEPARATOR).append(LINE_SEPARATOR);

        myWriter.write("Relations of the Connection State: ");
        myWriter.append(LINE_SEPARATOR);

        myWriter.write(HORIZONTAL_RULE);
        myWriter.append(LINE_SEPARATOR).append(LINE_SEPARATOR);

        logger.info(SUCCESS_WRITE);
    }

    /**
     * * Writes the states into a file.
     *
     * @param resultFile          the result file
     * @param extractionState     the extraction state, containing the extracted elements of the model
     * @param ntrState            the name type relation state, containing the mappings found in the text, sorted in name, type or name_or_type
     * @param recommendationState the supposing state, containing the supposing mappings for instances, as well as relations
     * @param connectionState     containing all instances and relations, matched by supposed mappings
     */
    public static void writeStatesToFile(File resultFile, LegacyModelExtractionState extractionState, TextState ntrState, //
            RecommendationState recommendationState, ConnectionState connectionState) {
        var fileCreated = createFileIfNonExistent(resultFile);
        if (!fileCreated) {
            return;
        }

        try (var myWriter = new FileWriter(resultFile, StandardCharsets.UTF_8)) {
            writeStates(myWriter, extractionState, ntrState, recommendationState, connectionState);

        } catch (IOException e) {
            logger.error(GENERIC_ERROR);
            logger.debug(e.getMessage(), e.getCause());
        }

    }

    /**
     * Write model instances in csv file.
     *
     * @param destination the destination
     * @param modelState  the model state
     * @param name        the name
     */
    private static void writeModelInstancesInCsvFile(File destination, LegacyModelExtractionState modelState, String name) {
        var dataLines = getInstancesFromModelState(modelState, name);
        writeDataLinesInFile(destination, dataLines);
    }

    private static ImmutableList<String[]> getInstancesFromModelState(LegacyModelExtractionState modelState, String name) {
        MutableList<String[]> dataLines = Lists.mutable.empty();

        dataLines.add(new String[] { "Found Model Elements in " + name + ":", "", "" });
        dataLines.add(new String[] { "" });
        dataLines.add(new String[] { "UID", "Name", "Type" });

        for (ModelInstance instance : modelState.getInstances()) {
            dataLines.add(new String[] { instance.getUid(), instance.getFullName(), instance.getFullType() });
        }

        return dataLines.toImmutable();
    }

    /**
     * Write trace links in csv file.
     *
     * @param resultFile      the result file
     * @param connectionState the connection state
     */
    private static void writeTraceLinksInCsvFile(File resultFile, ConnectionState connectionState) {
        var dataLines = getLinksAsDataLinesOfConnectionState(connectionState);
        writeDataLinesInFile(resultFile, dataLines);
    }

    /**
     * Write noun mappings in csv file.
     *
     * @param resultFile the result file
     * @param textState  the text state
     */
    private static void writeNounMappingsInCsvFile(File resultFile, TextState textState) {
        var dataLines = getMappingsAsDataLinesOfTextState(textState);
        writeDataLinesInFile(resultFile, dataLines);
    }

    private static ImmutableList<String[]> getMappingsAsDataLinesOfTextState(TextState textState) {
        MutableList<String[]> dataLines = Lists.mutable.empty();

        dataLines.add(new String[] { "Found NounMappings: ", "", "", "" });
        dataLines.add(new String[] { "" });
        dataLines.add(new String[] { "Reference", "Name", "Type" });

        if (textState.getNounMappings().isEmpty()) {
            for (NounMapping mapping : textState.getNounMappings()) {

                var kind = mapping.getKind();

                var nameProb = Double.toString(kind == MappingKind.NAME ? mapping.getProbability() : 0);
                var typeProb = Double.toString(kind == MappingKind.TYPE ? mapping.getProbability() : 0);

                dataLines.add(new String[] { mapping.getReference(), nameProb, typeProb });

            }
            return dataLines.toImmutable();
        }

        for (NounMapping mapping : textState.getNounMappings()) {

            var distribution = mapping.getDistribution();
            var nameProb = Double.toString(distribution.get(MappingKind.NAME).getConfidence());
            var typeProb = Double.toString(distribution.get(MappingKind.TYPE).getConfidence());

            dataLines.add(new String[] { mapping.getReference(), nameProb, typeProb });

        }
        return dataLines.toImmutable();
    }

    private static ImmutableList<String[]> getLinksAsDataLinesOfConnectionState(ConnectionState connectionState) {
        MutableList<String[]> dataLines = Lists.mutable.empty();

        dataLines.add(new String[] { "#Found TraceLinks: ", "" });
        dataLines.add(new String[] { "" });
        dataLines.add(new String[] { "modelElementID", "sentence" });

        Set<TraceLink<SentenceEntity, ArchitectureEntity>> tracelinks = new LinkedHashSet<>(connectionState.getTraceLinks().castToCollection());
        for (var tracelink : tracelinks) {
            var modelElementUid = tracelink.getSecondEndpoint().getId();
            // sentence offset is 1 because real sentences are 1-indexed
            var sentenceNumber = Integer.toString(tracelink.getFirstEndpoint().getSentence().getSentenceNumber() + 1);
            dataLines.add(new String[] { modelElementUid, sentenceNumber });
        }

        return dataLines.toImmutable();
    }

    /**
     * Write data lines in file.
     *
     * @param file      the file
     * @param dataLines the data lines
     */
    public static void writeDataLinesInFile(File file, ImmutableList<String[]> dataLines) {

        try (var pw = new FileWriter(file, StandardCharsets.UTF_8)) {
            dataLines.collect(FilePrinter::convertToCSV).forEach(s -> {
                try {
                    pw.append(s).append("\n");
                } catch (IOException e) {
                    logger.error(e.getMessage(), e);
                }
            });
        } catch (IOException e) {
            logger.error(GENERIC_ERROR);
            logger.debug(e.getMessage(), e);
        }

    }

    /**
     * Writes the given text to the file with the given name/path. Truncates existing files, creates the file if not existent and writes in UTF-8.
     *
     * @param filename the name/path of the file
     * @param text     the text to write
     */
    public static void writeToFile(String filename, String text) {
        var file = Paths.get(filename);
        writeToFile(file, text);
    }

    /**
     * Writes the given text to the given file (as path). Truncates existing files, creates the file if not existent and writes in UTF-8.
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

    private static void writeInconsistenciesToFile(File file, InconsistencyState inconsistencyState) {
        var inconsistencies = inconsistencyState.getInconsistencies();

        try (var pw = new FileWriter(file, StandardCharsets.UTF_8)) {
            inconsistencies.flatCollect(Inconsistency::toFileOutput)
                    .asLazy()
                    .collect(FilePrinter::convertToCSV)
                    .distinct()
                    .toSortedList(getInconsistencyStringComparator())
                    .forEach(s -> {
                        try {
                            pw.append(s).append("\n");
                        } catch (IOException e) {
                            logger.error(e.getMessage(), e);
                        }
                    });
        } catch (IOException e) {
            logger.error(GENERIC_ERROR);
            logger.debug(e.getMessage(), e);
        }
    }

    private static Comparator<? super String> getInconsistencyStringComparator() {
        return (i, j) -> {
            var values1 = i.split(DELIMITER, -1);
            var values2 = j.split(DELIMITER, -1);
            var name1 = values1[2];
            var name2 = values2[2];
            var wordComparisonResult = name1.compareTo(name2);
            if (wordComparisonResult != 0) {
                return wordComparisonResult;
            }
            var word1SentenceNo = -2;
            var word2SentenceNo = -1;
            try {
                word1SentenceNo = Integer.parseInt(values1[1]);
                word2SentenceNo = Integer.parseInt(values2[1]);
            } catch (NumberFormatException e) {
                // when there is no sentence number, it is intended that the Inconsistency is sorted at the beginning
                logger.trace("Could not parse sentence number for one of the following: {}, {}", values1[1], values2[1]);
            }

            var compareValue = word1SentenceNo - word2SentenceNo;
            if (compareValue == 0) {
                var word1 = values1[3];
                var word2 = values2[3];
                compareValue = word1.compareTo(word2);
            }
            return compareValue;
        };
    }

    private static String convertToCSV(String[] data) {
        return Stream.of(data).map(FilePrinter::escapeSpecialCharacters).collect(Collectors.joining(DELIMITER));
    }

    private static String escapeSpecialCharacters(String in) {
        var data = in;
        var escapedData = data.replaceAll("\\R", " ");
        if (data.contains(DELIMITER) || data.contains("\"") || data.contains("'")) {
            data = data.replace("\"", "\"\"");
            escapedData = "\"" + data + "\"";
        }
        return escapedData;
    }

    private static Comparator<RecommendedInstance> getRecommendedInstancesComparator() {
        return Comparator.comparing(RecommendedInstance::getName);
    }

    private static Comparator<InstanceLink> getInstanceLinkComparator() {
        return Comparator.comparing(i -> i.getModelInstance().getUid());
    }

    public static void writeInconsistencyOutput(File file, ArDoCoResult arDoCoResult) {
        MutableList<String> allInconsistencies = Lists.mutable.empty();
        allInconsistencies.addAll(arDoCoResult.getInconsistentSentences().collect(InconsistentSentence::getInfoString).toList());
        allInconsistencies.addAll(arDoCoResult.getAllModelInconsistencies().collect(ModelInconsistency::getReason).toList());
        Supplier<List<String>> outputExtractor = () -> allInconsistencies;
        writeOutput(file, "Inconsistencies", outputExtractor);
    }

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

    public static void writeTraceLinksAsCsv(ArDoCoResult arDoCoResult, File outputDir) {
        String name = arDoCoResult.getProjectName();
        String header;

        var sadSamTls = Lists.immutable.ofAll(arDoCoResult.getAllTraceLinks());
        if (!sadSamTls.isEmpty()) {
            var sadSamTlr = outputDir.toPath().resolve("sadSamTlr_" + name + ".csv");
            header = "modelElementID,sentence";
            var traceLinkStrings = TraceLinkUtilities.getSadSamTraceLinksAsStringList(sadSamTls);
            writeTraceLinksToCsv(sadSamTlr, header, traceLinkStrings);
        }

        var samCodeTls = Lists.immutable.ofAll(arDoCoResult.getSamCodeTraceLinks());
        if (!samCodeTls.isEmpty()) {
            var samCodeTlr = outputDir.toPath().resolve("samCodeTlr_" + name + ".csv");
            header = "sentenceID,codeID";
            var traceLinkStrings = TraceLinkUtilities.getSamCodeTraceLinksAsStringList(samCodeTls);
            writeTraceLinksToCsv(samCodeTlr, header, traceLinkStrings);
        }

        var sadCodeTls = Lists.immutable.ofAll(arDoCoResult.getSadCodeTraceLinks());
        if (!sadCodeTls.isEmpty()) {
            var sadCodeTlr = outputDir.toPath().resolve("sadCodeTlr_" + name + ".csv");
            header = "modelElementID,codeId";
            var traceLinkStrings = TraceLinkUtilities.getSadCodeTraceLinksAsStringList(sadCodeTls);
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
            logger.warn("An exception occurred when writing trace links to CSV file.", e);
        }
    }

}
