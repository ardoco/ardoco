package edu.kit.kastel.mcse.ardoco.secdreqan;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.kit.kastel.mcse.ardoco.core.common.JsonHandling;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public abstract class RequirementsExtractor {

    private static final Logger logger = LoggerFactory.getLogger(RequirementsExtractor.class);

    protected static String outputDir = "../target";

    public static List<Requirement> extractRequirementsFromJSONL(File inputFile) {
        File preprocessedFile = convertJSONLFileToJSON(inputFile, outputDir);
        return extractRequirmentsFromJSON(preprocessedFile);
    }

    public static List<File> writeRequirementsTextsToFiles(File inputFile) {
        return writeRequirementsTextsToFiles(inputFile, outputDir);
    }

    public static List<File> writeRequirementsTextsToFiles(File inputFile, String outputDir) {
        List<Requirement> requirements = extractRequirmentsFromJSON(inputFile);
        List<File> outputFiles = new ArrayList<>();

        new File(String.valueOf(outputDir)).mkdirs();
        for (Requirement requirement : requirements) {
            String fileName = "requirement_" + requirement.getUID() + ".txt";
            File outputFile = new File(outputDir, fileName);
            writeToFile(outputFile, requirement.getText());
            outputFiles.add(outputFile);
        }
        return outputFiles;
    }

    public static File convertJSONLFileToJSON(File inputFile, String outputDir) {
        List<String> lines;
        try {
            lines = Files.readAllLines(inputFile.toPath());
        } catch (IOException e) {
            throw new IllegalArgumentException("Could not access JSONL-File " + inputFile.getName());
        }

        String json = "[" + String.join(",", lines) + "]";

        var inputFileName = inputFile.getName();
        String inputFileNameWithoutExtension = inputFileName.substring(0, inputFileName.lastIndexOf('.'));
        File outputFile = new File(outputDir + "/preprocessedJSONLFiles", inputFileNameWithoutExtension + ".json");
        writeToFile(outputFile, json);
        return outputFile;
    }

    public static List<Requirement> extractRequirmentsFromJSON(File inputFile) {

        ObjectMapper objectMapper = JsonHandling.createObjectMapper();
        try {
            return objectMapper.readValue(inputFile, new TypeReference<>() {
            });
        } catch (IOException e) {
            throw new IllegalArgumentException("Input file was not found at " + inputFile.getAbsolutePath());
        }
    }

    private static void writeToFile(File outputFile, String text) {
        try {
            outputFile.getParentFile().mkdirs();
            FileWriter fileWriter = new FileWriter(outputFile);
            fileWriter.write(text);
            fileWriter.close();
            logger.debug("Wrote " + outputFile.getName() + " to " + outputFile.getAbsolutePath());

        } catch (IOException e) {
            throw new IllegalArgumentException("outputDir not available");
        }
    }
}
