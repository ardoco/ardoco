/* Licensed under MIT 2023-2025. */
package edu.kit.kastel.mcse.ardoco.core.execution;

import java.io.File;
import java.nio.file.Files;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.kit.kastel.mcse.ardoco.core.common.RepositoryHandler;
import edu.kit.kastel.mcse.ardoco.tlr.models.agents.CodeConfiguration;

public class CodeRunnerBaseTest extends RunnerBaseTest {

    private static final Logger logger = LoggerFactory.getLogger(CodeRunnerBaseTest.class);
    protected static final String inputCodeRepository = "https://github.com/ArDoCo/TeaStore.git";

    protected CodeConfiguration codeConfiguration = null;
    protected static String commitHash = "bdc49020a55cfa97eaabbb25744fefbc2697defa";

    @BeforeEach
    void setupCodeDirectories() throws Exception {
        if (codeConfiguration != null) {
            logger.debug("Already initialized");
            return;
        }

        var inputCodeModelDirectory = new File(directory.toFile(), "code");
        inputCodeModelDirectory.mkdir();
        var acmFile = new File(inputCodeModelDirectory, "codeModel.acm");
        this.getClass().getResourceAsStream("/code/teastore/codeModel.acm").transferTo(Files.newOutputStream(acmFile.toPath()));
        this.codeConfiguration = new CodeConfiguration(acmFile, CodeConfiguration.CodeConfigurationType.ACM_FILE);

        if (System.getenv("testCodeFull") != null) {
            var inputCodeModelDirectoryFull = new File(directory.toFile(), "code-full");
            inputCodeModelDirectoryFull.mkdir();
            this.codeConfiguration = new CodeConfiguration(inputCodeModelDirectoryFull, CodeConfiguration.CodeConfigurationType.DIRECTORY);
            var successfulClone = RepositoryHandler.shallowCloneRepository(inputCodeRepository, inputCodeModelDirectoryFull.getAbsolutePath(), commitHash);
            if (!successfulClone) {
                Assertions.fail("Could not clone repository.");
            }
        }

    }
}
