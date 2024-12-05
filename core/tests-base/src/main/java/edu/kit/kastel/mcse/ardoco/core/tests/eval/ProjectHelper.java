/* Licensed under MIT 2023-2024. */
package edu.kit.kastel.mcse.ardoco.core.tests.eval;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;

/**
 * Helper class for {@link GoldStandardProject} implementations.
 */
public class ProjectHelper {

    private ProjectHelper() {
        throw new IllegalAccessError();
    }

    /**
     * Load a resource to a temporary file
     *
     * @param resource the resource path
     * @return the file if loaded or null if not possible
     */
    public static File loadFileFromResources(String resource) {
        InputStream is = ProjectHelper.class.getResourceAsStream(resource);
        if (is == null)
            throw new IllegalArgumentException("Resource not found: " + resource);
        try {
            File temporaryFile = File.createTempFile("ArDoCo", ".tmp");
            temporaryFile.deleteOnExit();
            try (FileOutputStream fos = new FileOutputStream(temporaryFile)) {
                try (is) {
                    is.transferTo(fos);
                }
            }
            return temporaryFile;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
