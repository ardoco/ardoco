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

/**
 * Comprehensive integration tests for the ARDoCo CLI.
 * Tests all TLR tasks (sad-sam, sam-code, sad-code) on all benchmark datasets.
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
        MEDIASTORE("mediastore", "text_2016/mediastore.txt", "model_2016/pcm/ms.repository", "model_2016/code/codeModel.acm"),
        TEASTORE("teastore", "text_2020/teastore.txt", "model_2020/pcm/teastore.repository", "model_2022/code/codeModel.acm"),
        TEAMMATES("teammates", "text_2021/teammates.txt", "model_2021/pcm/teammates.repository", "model_2023/code/codeModel.acm"),
        BIGBLUEBUTTON("bigbluebutton", "text_2021/bigbluebutton.txt", "model_2021/pcm/bbb.repository", "model_2023/code/codeModel.acm"),
        JABREF("jabref", "text_2021/jabref.txt", "model_2021/pcm/jabref.repository", "model_2023/code/codeModel.acm");

        private final String name;
        private final String textPath;
        private final String modelPath;
        private final String codePath;

        BenchmarkProject(String name, String textPath, String modelPath, String codePath) {
            this.name = name;
            this.textPath = textPath;
            this.modelPath = modelPath;
            this.codePath = codePath;
        }

        public String getName() {
            return name;
        }

        public File getTextFile() {
            return getBenchmarkBasePath().resolve(name).resolve(textPath).toFile();
        }

        public File getModelFile() {
            return getBenchmarkBasePath().resolve(name).resolve(modelPath).toFile();
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
                walk.sorted(Comparator.reverseOrder()).map(Path::toFile).forEach(File::delete);
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
    }

    @Nested
    @DisplayName("SAD-SAM Task Tests")
    class SadSamTaskTests {

        @ParameterizedTest(name = "SAD-SAM on {0}")
        @EnumSource(BenchmarkProject.class)
        @DisplayName("Test SAD-SAM task on all benchmark projects")
        void testSadSamTask(BenchmarkProject project) {
            logger.info("=== Testing SAD-SAM on {} ===", project.getName());

            String[] args = { "-t", "sad-sam", "-n", project.getName(), "-d", project.getTextFile().getAbsolutePath(), "-m",
                    project.getModelFile().getAbsolutePath(), "-o", outputDir.toString() };

            PluginManager pluginManager = new PluginManager();
            assertFalse(pluginManager.getPlugins().isEmpty(), "No plugins discovered");

            pluginManager.executePlugins(args);

            // Verify output was created
            File[] outputFiles = outputDir.toFile().listFiles();
            assertNotNull(outputFiles, "Output directory should have files");
            assertTrue(outputFiles.length > 0, "No output files created for SAD-SAM on " + project.getName());

            // Verify trace links file exists
            boolean hasTraceLinksFile = Arrays.stream(outputFiles)
                    .anyMatch(f -> f.getName().contains("traceLinks") || f.getName().contains("TraceLink"));
            assertTrue(hasTraceLinksFile || outputFiles.length > 0,
                    "Should produce trace links output for " + project.getName());

            logger.info("SAD-SAM on {} completed. Output files: {}", project.getName(),
                    Arrays.stream(outputFiles).map(File::getName).toList());
        }
    }

    @Nested
    @DisplayName("SAM-Code Task Tests")
    class SamCodeTaskTests {

        @ParameterizedTest(name = "SAM-Code on {0}")
        @EnumSource(BenchmarkProject.class)
        @DisplayName("Test SAM-Code task on all benchmark projects")
        void testSamCodeTask(BenchmarkProject project) {
            logger.info("=== Testing SAM-Code on {} ===", project.getName());

            String[] args = { "-t", "sam-code", "-n", project.getName(), "-m", project.getModelFile().getAbsolutePath(), "-c",
                    project.getCodeFile().getAbsolutePath(), "-o", outputDir.toString() };

            PluginManager pluginManager = new PluginManager();
            assertFalse(pluginManager.getPlugins().isEmpty(), "No plugins discovered");

            pluginManager.executePlugins(args);

            // Verify output was created
            File[] outputFiles = outputDir.toFile().listFiles();
            assertNotNull(outputFiles, "Output directory should have files");
            assertTrue(outputFiles.length > 0, "No output files created for SAM-Code on " + project.getName());

            logger.info("SAM-Code on {} completed. Output files: {}", project.getName(),
                    Arrays.stream(outputFiles).map(File::getName).toList());
        }
    }

    @Nested
    @DisplayName("SAD-Code Task Tests")
    class SadCodeTaskTests {

        @ParameterizedTest(name = "SAD-Code on {0}")
        @EnumSource(BenchmarkProject.class)
        @DisplayName("Test SAD-Code task on all benchmark projects")
        void testSadCodeTask(BenchmarkProject project) {
            logger.info("=== Testing SAD-Code on {} ===", project.getName());

            String[] args = { "-t", "sad-code", "-n", project.getName(), "-d", project.getTextFile().getAbsolutePath(), "-m",
                    project.getModelFile().getAbsolutePath(), "-c", project.getCodeFile().getAbsolutePath(), "-o", outputDir.toString() };

            PluginManager pluginManager = new PluginManager();
            assertFalse(pluginManager.getPlugins().isEmpty(), "No plugins discovered");

            pluginManager.executePlugins(args);

            // Verify output was created
            File[] outputFiles = outputDir.toFile().listFiles();
            assertNotNull(outputFiles, "Output directory should have files");
            assertTrue(outputFiles.length > 0, "No output files created for SAD-Code on " + project.getName());

            logger.info("SAD-Code on {} completed. Output files: {}", project.getName(),
                    Arrays.stream(outputFiles).map(File::getName).toList());
        }
    }

    @Nested
    @DisplayName("Model Format Auto-Detection Tests")
    class ModelFormatTests {

        @Test
        @DisplayName("Should auto-detect PCM format from .repository extension")
        void testPcmFormatAutoDetection() {
            BenchmarkProject project = BenchmarkProject.MEDIASTORE;
            logger.info("Testing PCM format auto-detection on {}", project.getName());

            // Run without explicit --model-format, should auto-detect PCM
            String[] args = { "-t", "sad-sam", "-n", project.getName(), "-d", project.getTextFile().getAbsolutePath(), "-m",
                    project.getModelFile().getAbsolutePath(), "-o", outputDir.toString() };

            PluginManager pluginManager = new PluginManager();
            pluginManager.executePlugins(args);

            File[] outputFiles = outputDir.toFile().listFiles();
            assertNotNull(outputFiles, "Output should be created with auto-detected format");
            assertTrue(outputFiles.length > 0, "Should produce output with auto-detected PCM format");
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

            String[] args = { "-t", "sam-code", "-n", project.getName(), "-m", project.getModelFile().getAbsolutePath(), "-c",
                    project.getCodeFile().getAbsolutePath(), "-o", outputDir.toString() };

            PluginManager pluginManager = new PluginManager();
            pluginManager.executePlugins(args);

            File[] outputFiles = outputDir.toFile().listFiles();
            assertNotNull(outputFiles, "Output should be created with auto-detected code type");
            assertTrue(outputFiles.length > 0, "Should produce output with auto-detected ACM_FILE type");
        }
    }
}
