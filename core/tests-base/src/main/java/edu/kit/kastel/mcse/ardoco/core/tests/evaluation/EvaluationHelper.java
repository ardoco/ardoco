/* Licensed under MIT 2025. */
package edu.kit.kastel.mcse.ardoco.core.tests.evaluation;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;

public final class EvaluationHelper {
    private EvaluationHelper() {
        throw new IllegalAccessError("Utility class should not be instantiated");
    }

    /**
     * Load a resource to a temporary file
     *
     * @param resource the resource path
     * @return the file if loaded or null if not possible
     */
    public static File loadFileFromResources(String resource) {
        InputStream is = EvaluationHelper.class.getResourceAsStream(resource);
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
