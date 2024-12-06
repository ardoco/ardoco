package edu.kit.kastel.mcse.ardoco.secdreqan;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.kit.kastel.mcse.ardoco.core.common.JsonHandling;
import org.assertj.core.api.Assertions;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Scanner;

public class RequirementsExtractorTest {

    protected File jsonInputFile = new File("../evaluation/EVerest_dataset.json");
    protected File jsonlInputFile = new File("../evaluation/EVerest_Components_R1.jsonl");
    protected Path directory;

    @BeforeEach
    public void checkFileExistence(){
        Assumptions.assumeTrue(jsonInputFile.exists());
        Assumptions.assumeTrue(jsonlInputFile.exists());
    }


    @Test
    @DisplayName("JSONL Requirements Extraction")
    public void testExtractRequirementsFromJSONL() {
        List<Requirement> requirements = RequirementsExtractor.extractRequirementsFromJSONL(jsonlInputFile);
        Assertions.assertThat(requirements).isNotNull().isNotEmpty();
    }

    @Test
    @DisplayName("JSON Requirements Extraction")
    public void testExtractRequirementsFromJSON() {
        List<Requirement> requirements = RequirementsExtractor.extractRequirmentsFromJSON(jsonInputFile);
        Assertions.assertThat(requirements).isNotNull().isNotEmpty();
    }

    @Test
    @DisplayName("Requirements Text Writer")
    public void testWriteRequirementsTextsToFiles() throws IOException {

        List<Requirement> requirements = RequirementsExtractor.extractRequirmentsFromJSON(jsonInputFile);

        this.directory = Files.createTempDirectory("RequirementsTest_" + this.getClass().getName());
        RequirementsExtractor.writeRequirementsTextsToFiles(jsonInputFile, String.valueOf(this.directory));

        Scanner scanner = new Scanner(new File(this.directory.toFile(), "requirement_0.txt"));
        StringBuilder requirementsText = new StringBuilder();
        while (scanner.hasNextLine()) {
            requirementsText.append(scanner.nextLine());
        }

        Assertions.assertThat(requirementsText.toString()).hasToString(requirements.getFirst().getText());
    }

    @Test
    @DisplayName("JSONL Preprocessing")
    public void testPreprocessJSONLFile() throws IOException {

        this.directory = Files.createTempDirectory("RequirementsTest_" + this.getClass().getName());
        File preprocessedFile = RequirementsExtractor.convertJSONLFileToJSON(jsonlInputFile, String.valueOf(this.directory));
        String writtenString = Files.readString(Path.of(preprocessedFile.getAbsolutePath()));

        ObjectMapper objectMapper = JsonHandling.createObjectMapper();
        try {
            objectMapper.readTree(writtenString);
        } catch (JsonProcessingException e) {
            Assert.fail(String.format("JsonFile %s not valid.", preprocessedFile.getAbsolutePath()));
        }
    }

}
