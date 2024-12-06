package edu.kit.kastel.mcse.ardoco.secdreqan;

import edu.kit.kastel.mcse.ardoco.core.api.models.ArchitectureModelType;
import edu.kit.kastel.mcse.ardoco.core.execution.ConfigurationHelper;
import edu.kit.kastel.mcse.ardoco.core.execution.runner.ArDoCoRunner;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.function.Executable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class SecDReqAnRunnerTest {

    private static final Logger logger = LoggerFactory.getLogger(SecDReqAnRunnerTest.class);
    protected String outputDir = "../target/testout-" + this.getClass().getSimpleName();
    protected String inputText = null;
    protected String inputModelArchitecture = null;
    protected String additionalConfig = null;
    protected String projectName = "EVerest";
    protected Path directory;

    @BeforeEach
    void setupDirectories() throws Exception {
        new File(this.outputDir).mkdirs();
        if (this.inputText != null) {
            logger.debug("Already initialized");
        } else {
            this.directory = Files.createTempDirectory("RunnerTest" + this.getClass().getName());
            File inputTextFile = new File("../evaluation", "everest_dataset.json");
            File inputModelArchitecture = new File("../evaluation", "everest.repository");
            File additionalConfig = new File("src/test/resources", "additionalConfig.txt");

            Assumptions.assumeTrue(inputTextFile.exists());
            Assumptions.assumeTrue(inputModelArchitecture.exists());
            this.inputText = inputTextFile.getAbsolutePath();
            this.inputModelArchitecture = inputModelArchitecture.getAbsolutePath();
            this.additionalConfig = additionalConfig.getAbsolutePath();
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                try {
                    Files.walk(this.directory).sorted((a, b) -> {
                        return -a.compareTo(b);
                    }).forEach((p) -> {
                        try {
                            Files.delete(p);
                        } catch (IOException var2) {
                            IOException e = var2;
                            logger.warn("Error when cleaning up!", e);
                        }

                    });
                } catch (IOException var2) {
                    IOException e = var2;
                    logger.warn("Error when cleaning up!", e);
                }

            }));
        }
    }

    @Test
    @DisplayName("Test SetUp")
    void testInput() {
        File inputTextFile = new File(this.inputText);
        File inputModelArchitectureFile = new File(this.inputModelArchitecture);
        File outputDirFile = new File(this.outputDir);
        File additionalConfigsFile = new File(this.additionalConfig);
        Assertions.assertAll(new Executable[] { () -> {
            Assertions.assertTrue(inputTextFile.exists());
        }, () -> {
            Assertions.assertTrue(inputModelArchitectureFile.exists());
        }, () -> {
            Assertions.assertTrue(outputDirFile.exists());
        }, () -> {
            Assertions.assertTrue(additionalConfigsFile.exists());
        } });
    }

    @Test
    @DisplayName("Test ArDoCo for SAD-SAM-TLR (PCM)")
    void testSadSamTlrPcm() throws IOException {
        var runner = new SecDReqAnRunner(projectName);
        var additionalConfigsMap = ConfigurationHelper.loadAdditionalConfigs(new File(additionalConfig));
        runner.setUp(inputText, inputModelArchitecture, ArchitectureModelType.PCM, additionalConfigsMap, outputDir);

        testRunnerAssertions(runner);
        Assertions.assertNotNull(runner.run());
    }

    protected void testRunnerAssertions(ArDoCoRunner runner) {
        Assertions.assertAll(new Executable[] { () -> {
            Assertions.assertNotNull(runner);
        }, () -> {
            Assertions.assertTrue(runner.isSetUp());
        } });
    }

}
