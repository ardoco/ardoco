package edu.kit.kastel.mcse.ardoco.secdreqan;

import edu.kit.kastel.mcse.ardoco.core.tests.eval.GoldStandardProject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.List;
import java.util.Objects;

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

        InputStream is;
        try {
            is = new FileInputStream(resource);
        } catch (FileNotFoundException e) {
            throw new IllegalArgumentException("Resource not found: " + System.getProperty("user.dir") + "/" + resource);
        }
        try {
            File temporaryFile = File.createTempFile("SecDReqAn", ".tmp");
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

    public static List<File> loadFilesFromFolder(String directory) {
        File folder = new File(directory);
        List<File> listOfFiles = List.of(Objects.requireNonNull(folder.listFiles()));
        for (File file : listOfFiles) {
            if (file.isDirectory()) {
                throw new UnsupportedOperationException("Only files expected but also a directory found!");
            }
        }
        return listOfFiles;
    }
}
