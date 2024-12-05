/* Licensed under MIT 2023-2024. */
package edu.kit.kastel.mcse.ardoco.id.cleanup;

import java.io.File;
import java.util.List;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import edu.kit.kastel.mcse.ardoco.core.common.RepositoryHandler;

/**
 * This is a "fake" test class that should clean up after everything else is run. Sometimes, we want to have, e.g.,
 * a certain state for the file system during all tests for all modules but want to clean up afterward.
 * One concrete example are code repositories that we check out initially and that we want to remove at the end for a clean state.
 */
class CleanupTest {

    private static final List<String> codeFolders = List.of("../temp/code", "temp/code");

    private static final List<String> folders = List.of("../temp/", "temp/");

    @Test
    void canTearDownTest() {
        for (var codeFolder : codeFolders) {
            File file = new File(codeFolder);
            boolean existingCheck = file.isDirectory() && file.canWrite();
            Assertions.assertTrue(existingCheck || !file.exists());
        }
    }

    @AfterAll
    static void tearDown() {
        removeFolders(codeFolders);
        removeFolders(folders);
    }

    private static void removeFolders(List<String> folders) {
        for (var folder : folders) {
            File file = new File(folder);
            if (file.exists()) {
                RepositoryHandler.removeRepository(folder);
            }
        }
    }
}
