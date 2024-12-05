/* Licensed under MIT 2023-2024. */
package edu.kit.kastel.mcse.ardoco.core.execution;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.kit.kastel.mcse.ardoco.core.execution.runner.ArDoCoRunner;

public class RunnerBaseTest {
    private static final Logger logger = LoggerFactory.getLogger(RunnerBaseTest.class);

    protected String outputDir = "../target/testout-" + this.getClass().getSimpleName();
    protected String inputText = null;
    protected String inputModelArchitecture = null;
    protected String inputModelArchitectureUml = null;
    protected String additionalConfigs = null;
    protected String projectName = "teastore";
    protected Path directory;

    @BeforeEach
    void setupDirectories() throws Exception {
        new File(outputDir).mkdirs();

        if (inputText != null) {
            logger.debug("Already initialized");
            return;
        }

        this.directory = Files.createTempDirectory("RunnerTest" + this.getClass().getName());
        var inputText = new File(directory.toFile(), "inputText.txt");
        var inputModelArchitecture = new File(directory.toFile(), "inputModelArchitecture.repository");
        var inputModelArchitectureUml = new File(directory.toFile(), "inputModelArchitecture.uml");
        var inputDiagram = new File(directory.toFile(), "teastore-paper.png");

        var additionalConfigs = new File(directory.toFile(), "additionalConfigs.txt");

        this.getClass().getResourceAsStream("/teastore.txt").transferTo(Files.newOutputStream(inputText.toPath()));
        this.getClass().getResourceAsStream("/teastore.repository").transferTo(Files.newOutputStream(inputModelArchitecture.toPath()));
        this.getClass().getResourceAsStream("/teastore.uml").transferTo(Files.newOutputStream(inputModelArchitectureUml.toPath()));
        this.getClass().getResourceAsStream("/additionalConfig.txt").transferTo(Files.newOutputStream(additionalConfigs.toPath()));
        this.getClass().getResourceAsStream("/teastore-paper.png").transferTo(Files.newOutputStream(inputDiagram.toPath()));

        this.inputText = inputText.getAbsolutePath();
        this.inputModelArchitecture = inputModelArchitecture.getAbsolutePath();
        this.inputModelArchitectureUml = inputModelArchitectureUml.getAbsolutePath();
        this.additionalConfigs = additionalConfigs.getAbsolutePath();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                // Recursively delete files from directory
                Files.walk(directory).sorted((a, b) -> -a.compareTo(b)).forEach(p -> {
                    try {
                        Files.delete(p);
                    } catch (IOException e) {
                        logger.warn("Error when cleaning up!", e);
                    }
                });
            } catch (IOException e) {
                logger.warn("Error when cleaning up!", e);
            }
        }));
    }

    @AfterEach
    void cleanUp() {
        for (File file : Objects.requireNonNull(new File(outputDir).listFiles())) {
            if (!file.getName().equals(".gitkeep")) {
                try {
                    Files.delete(file.toPath());
                } catch (IOException e) {
                    logger.warn("Error when cleaning up!", e);
                }
            }
        }
    }

    @SuppressWarnings("java:S5960")
    @Test
    @DisplayName("Test SetUp")
    void testInput() {
        File inputTextFile = new File(inputText);
        File inputModelArchitectureFile = new File(inputModelArchitecture);
        File inputModelArchitectureUmlFile = new File(inputModelArchitectureUml);
        File outputDirFile = new File(outputDir);
        File additionalConfigsFile = new File(additionalConfigs);

        Assertions.assertAll(//
                () -> Assertions.assertTrue(inputTextFile.exists()),//
                () -> Assertions.assertTrue(inputModelArchitectureFile.exists()),//
                () -> Assertions.assertTrue(inputModelArchitectureUmlFile.exists()),//
                () -> Assertions.assertTrue(outputDirFile.exists()),//
                () -> Assertions.assertTrue(additionalConfigsFile.exists())//
        );
    }

    protected void testRunnerAssertions(ArDoCoRunner runner) {
        Assertions.assertAll(//
                () -> Assertions.assertNotNull(runner),//
                () -> Assertions.assertTrue(runner.isSetUp())//
        );
    }
}
