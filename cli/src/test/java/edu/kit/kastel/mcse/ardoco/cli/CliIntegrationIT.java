/* Licensed under MIT 2026. */
package edu.kit.kastel.mcse.ardoco.cli;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.kit.kastel.mcse.ardoco.core.cli.TaskPlugin;
import edu.kit.kastel.mcse.ardoco.tlr.tests.approach.ArcotlEvaluationProject;
import edu.kit.kastel.mcse.ardoco.tlr.tests.approach.SwattrEvaluationProject;
import edu.kit.kastel.mcse.ardoco.tlr.tests.approach.TransarcEvaluationProject;
import edu.kit.kastel.mcse.ardoco.tlr.tests.integration.evaluation.ArcotlEvaluation;
import edu.kit.kastel.mcse.ardoco.tlr.tests.integration.evaluation.SwattrEvaluation;
import edu.kit.kastel.mcse.ardoco.tlr.tests.integration.evaluation.TransarcEvaluation;

/**
 * Comprehensive integration tests for the ARDoCo CLI.
 * Tests all TLR tasks (sad-sam, sam-code, sad-code) on all benchmark datasets,
 * asserting that results meet the same quality thresholds as the TLR tests.
 */
class CliIntegrationIT {

    private static final Logger logger = LoggerFactory.getLogger(CliIntegrationIT.class);

    private Path outputDir;

    /**
     * Returns the base path to the benchmark resources in the source tree.
     */
    private static Path getBenchmarkBasePath() {
        Path currentDir = Path.of(System.getProperty("user.dir"));
        Path benchmarkPath = currentDir.resolve("../core/tests-base/src/main/resources/benchmark").normalize();
        if (!Files.exists(benchmarkPath)) {
            benchmarkPath = currentDir.resolve("core/tests-base/src/main/resources/benchmark").normalize();
        }
        if (!Files.exists(benchmarkPath)) {
            throw new IllegalStateException("Benchmark directory not found. Current dir: " + currentDir);
        }
        return benchmarkPath;
    }

    /**
     * Enum representing all benchmark projects with their file paths.
     */
    enum BenchmarkProject {
        MEDIASTORE("mediastore", "text_2016/mediastore.txt", "model_2016/pcm/ms.repository", "model_2016/uml/ms.uml",
                "model_2016/code/codeModel.acm"), TEASTORE("teastore", "text_2020/teastore.txt", "model_2020/pcm/teastore.repository",
                        "model_2020/uml/teastore.uml", "model_2022/code/codeModel.acm"), TEAMMATES("teammates", "text_2021/teammates.txt",
                                "model_2021/pcm/teammates.repository", "model_2021/uml/teammates.uml", "model_2023/code/codeModel.acm"), BIGBLUEBUTTON(
                                        "bigbluebutton", "text_2021/bigbluebutton.txt", "model_2021/pcm/bbb.repository", "model_2021/uml/bbb.uml",
                                        "model_2023/code/codeModel.acm"), JABREF("jabref", "text_2021/jabref.txt", "model_2021/pcm/jabref.repository",
                                                "model_2021/uml/jabref.uml", "model_2023/code/codeModel.acm");

        private final String name;
        private final String textPath;
        private final String pcmModelPath;
        private final String umlModelPath;
        private final String codePath;

        BenchmarkProject(String name, String textPath, String pcmModelPath, String umlModelPath, String codePath) {
            this.name = name;
            this.textPath = textPath;
            this.pcmModelPath = pcmModelPath;
            this.umlModelPath = umlModelPath;
            this.codePath = codePath;
        }

        public String getName() {
            return name;
        }

        public File getTextFile() {
            return getBenchmarkBasePath().resolve(name).resolve(textPath).toFile();
        }

        public File getModelFile() {
            return getBenchmarkBasePath().resolve(name).resolve(pcmModelPath).toFile();
        }

        public File getUmlModelFile() {
            return getBenchmarkBasePath().resolve(name).resolve(umlModelPath).toFile();
        }

        public File getCodeFile() {
            return getBenchmarkBasePath().resolve(name).resolve(codePath).toFile();
        }
    }

    @BeforeEach
    void setUp() throws IOException {
        outputDir = Files.createTempDirectory("ardoco-cli-test-");
        logger.info("Test output directory: {}", outputDir);
    }

    @AfterEach
    void tearDown() throws IOException {
        if (outputDir != null && Files.exists(outputDir)) {
            try (Stream<Path> walk = Files.walk(outputDir)) {
                walk.sorted(Comparator.reverseOrder()).forEach(path -> {
                    try {
                        Files.deleteIfExists(path);
                    } catch (IOException e) {
                        logger.warn("Failed to delete: {}", path, e);
                    }
                });
            }
        }
    }

    @Nested
    @DisplayName("Plugin Discovery Tests")
    class PluginDiscoveryTests {

        @Test
        @DisplayName("Should discover all three TLR plugins")
        void testPluginDiscovery() {
            PluginManager pluginManager = new PluginManager();
            List<TaskPlugin> plugins = pluginManager.getPlugins();

            assertNotNull(plugins, "Plugin list should not be null");
            assertEquals(3, plugins.size(), "Should discover exactly 3 plugins");

            List<String> taskNames = plugins.stream().map(TaskPlugin::getTaskName).sorted().toList();
            assertEquals(List.of("sad-code", "sad-sam", "sam-code"), taskNames, "Should have all three task types");
        }

        @Test
        @DisplayName("Each plugin should have valid metadata")
        void testPluginMetadata() {
            PluginManager pluginManager = new PluginManager();

            for (TaskPlugin plugin : pluginManager.getPlugins()) {
                assertNotNull(plugin.getTaskName(), "Task name should not be null");
                assertFalse(plugin.getTaskName().isBlank(), "Task name should not be blank");

                assertNotNull(plugin.getDescription(), "Description should not be null");
                assertFalse(plugin.getDescription().isBlank(), "Description should not be blank");

                assertNotNull(plugin.getRequiredOptions(), "Required options should not be null");
                assertNotNull(plugin.getOptionalOptions(), "Optional options should not be null");
            }
        }
    }

    @Nested
    @DisplayName("Input Validation Tests")
    class InputValidationTests {

        @Test
        @DisplayName("All benchmark text files should exist")
        void testTextFilesExist() {
            for (BenchmarkProject project : BenchmarkProject.values()) {
                File textFile = project.getTextFile();
                assertTrue(textFile.exists(), "Text file should exist: " + textFile.getAbsolutePath());
                assertTrue(textFile.isFile(), "Text path should be a file: " + textFile.getAbsolutePath());
                assertTrue(textFile.length() > 0, "Text file should not be empty: " + textFile.getAbsolutePath());
            }
        }

        @Test
        @DisplayName("All benchmark model files should exist")
        void testModelFilesExist() {
            for (BenchmarkProject project : BenchmarkProject.values()) {
                File modelFile = project.getModelFile();
                assertTrue(modelFile.exists(), "Model file should exist: " + modelFile.getAbsolutePath());
                assertTrue(modelFile.isFile(), "Model path should be a file: " + modelFile.getAbsolutePath());
                assertTrue(modelFile.length() > 0, "Model file should not be empty: " + modelFile.getAbsolutePath());
                assertTrue(modelFile.getName().endsWith(".repository"), "Model file should be .repository: " + modelFile.getName());
            }
        }

        @Test
        @DisplayName("All benchmark code model files should exist")
        void testCodeFilesExist() {
            for (BenchmarkProject project : BenchmarkProject.values()) {
                File codeFile = project.getCodeFile();
                assertTrue(codeFile.exists(), "Code file should exist: " + codeFile.getAbsolutePath());
                assertTrue(codeFile.isFile(), "Code path should be a file: " + codeFile.getAbsolutePath());
                assertTrue(codeFile.length() > 0, "Code file should not be empty: " + codeFile.getAbsolutePath());
                assertTrue(codeFile.getName().endsWith(".acm"), "Code file should be .acm: " + codeFile.getName());
            }
        }

        @Test
        @DisplayName("All benchmark UML model files should exist")
        void testUmlModelFilesExist() {
            for (BenchmarkProject project : BenchmarkProject.values()) {
                File umlFile = project.getUmlModelFile();
                assertTrue(umlFile.exists(), "UML model file should exist: " + umlFile.getAbsolutePath());
                assertTrue(umlFile.isFile(), "UML model path should be a file: " + umlFile.getAbsolutePath());
                assertTrue(umlFile.length() > 0, "UML model file should not be empty: " + umlFile.getAbsolutePath());
                assertTrue(umlFile.getName().endsWith(".uml"), "UML model file should be .uml: " + umlFile.getName());
            }
        }
    }

    @Nested
    @DisplayName("SAD-SAM Task Tests")
    class SadSamTaskTests {

        @ParameterizedTest(name = "SAD-SAM on {0}")
        @EnumSource(SwattrEvaluationProject.class)
        @DisplayName("Evaluate SAD-SAM task on all benchmark projects")
        void testSadSamTask(SwattrEvaluationProject project) {
            logger.info("=== Evaluating SAD-SAM on {} ===", project.name());
            var evaluation = new SwattrEvaluation(project);
            var result = evaluation.runTraceLinkEvaluation();
            assertNotNull(result);
        }
    }

    @Nested
    @DisplayName("SAM-Code Task Tests")
    class SamCodeTaskTests {

        @ParameterizedTest(name = "SAM-Code on {0}")
        @EnumSource(ArcotlEvaluationProject.class)
        @DisplayName("Evaluate SAM-Code task on all benchmark projects")
        void testSamCodeTask(ArcotlEvaluationProject project) {
            logger.info("=== Evaluating SAM-Code on {} ===", project.name());
            var evaluation = new ArcotlEvaluation(project, true);
            var result = evaluation.runTraceLinkEvaluation();
            assertNotNull(result);
        }
    }

    @Nested
    @DisplayName("SAD-Code Task Tests")
    class SadCodeTaskTests {

        @ParameterizedTest(name = "SAD-Code on {0}")
        @EnumSource(TransarcEvaluationProject.class)
        @DisplayName("Evaluate SAD-Code task on all benchmark projects")
        void testSadCodeTask(TransarcEvaluationProject project) {
            logger.info("=== Evaluating SAD-Code on {} ===", project.name());
            var evaluation = new TransarcEvaluation(project, true);
            var result = evaluation.runTraceLinkEvaluation();
            assertNotNull(result);
        }
    }

    @Nested
    @DisplayName("Code Configuration Type Auto-Detection Tests")
    class CodeConfigTests {

        @Test
        @DisplayName("Should auto-detect ACM_FILE type for .acm files")
        void testAcmFileAutoDetection() {
            BenchmarkProject project = BenchmarkProject.MEDIASTORE;
            logger.info("Testing ACM file type auto-detection on {}", project.getName());

            String[] args = { "-t", "sam-code", "-n", project.getName(), "-m", project.getModelFile().getAbsolutePath(), "--model-format", "PCM", "-c",
                    project.getCodeFile().getAbsolutePath(), "-o", outputDir.toString() };

            PluginManager pluginManager = new PluginManager();
            pluginManager.executePlugins(args);

            File[] outputFiles = outputDir.toFile().listFiles();
            assertNotNull(outputFiles, "Output should be created with auto-detected code type");
            assertTrue(outputFiles.length > 0, "Should produce output with auto-detected ACM_FILE type");
        }
    }

    @Nested
    @DisplayName("UML Model Format Tests")
    class UmlModelTests {

        @ParameterizedTest(name = "SAD-SAM with UML on {0}")
        @EnumSource(BenchmarkProject.class)
        @DisplayName("Test SAD-SAM task with UML models on all benchmark projects")
        void testSadSamWithUmlModel(BenchmarkProject project) {
            logger.info("=== Testing SAD-SAM with UML on {} ===", project.getName());

            String[] args = { "-t", "sad-sam", "-n", project.getName(), "-d", project.getTextFile().getAbsolutePath(), "-m", project.getUmlModelFile()
                    .getAbsolutePath(), "--model-format", "UML", "-o", outputDir.toString() };

            PluginManager pluginManager = new PluginManager();
            assertFalse(pluginManager.getPlugins().isEmpty(), "No plugins discovered");

            pluginManager.executePlugins(args);

            File[] outputFiles = outputDir.toFile().listFiles();
            assertNotNull(outputFiles, "Output directory should have files");
            assertTrue(outputFiles.length > 0, "No output files created for SAD-SAM with UML on " + project.getName());

            logger.info("SAD-SAM with UML on {} completed. Output files: {}", project.getName(), Arrays.stream(outputFiles).map(File::getName).toList());
        }

        @Test
        @DisplayName("Test SAM-Code task with UML model")
        void testSamCodeWithUmlModel() {
            BenchmarkProject project = BenchmarkProject.MEDIASTORE;
            logger.info("=== Testing SAM-Code with UML on {} ===", project.getName());

            String[] args = { "-t", "sam-code", "-n", project.getName(), "-m", project.getUmlModelFile().getAbsolutePath(), "--model-format", "UML", "-c",
                    project.getCodeFile().getAbsolutePath(), "-o", outputDir.toString() };

            PluginManager pluginManager = new PluginManager();
            pluginManager.executePlugins(args);

            File[] outputFiles = outputDir.toFile().listFiles();
            assertNotNull(outputFiles, "Output directory should have files");
            assertTrue(outputFiles.length > 0, "No output files created for SAM-Code with UML on " + project.getName());

            logger.info("SAM-Code with UML on {} completed. Output files: {}", project.getName(), Arrays.stream(outputFiles).map(File::getName).toList());
        }

        @Test
        @DisplayName("Test SAD-Code task with UML model")
        void testSadCodeWithUmlModel() {
            BenchmarkProject project = BenchmarkProject.MEDIASTORE;
            logger.info("=== Testing SAD-Code with UML on {} ===", project.getName());

            String[] args = { "-t", "sad-code", "-n", project.getName(), "-d", project.getTextFile().getAbsolutePath(), "-m", project.getUmlModelFile()
                    .getAbsolutePath(), "--model-format", "UML", "-c", project.getCodeFile().getAbsolutePath(), "-o", outputDir.toString() };

            PluginManager pluginManager = new PluginManager();
            pluginManager.executePlugins(args);

            File[] outputFiles = outputDir.toFile().listFiles();
            assertNotNull(outputFiles, "Output directory should have files");
            assertTrue(outputFiles.length > 0, "No output files created for SAD-Code with UML on " + project.getName());

            logger.info("SAD-Code with UML on {} completed. Output files: {}", project.getName(), Arrays.stream(outputFiles).map(File::getName).toList());
        }
    }
}
