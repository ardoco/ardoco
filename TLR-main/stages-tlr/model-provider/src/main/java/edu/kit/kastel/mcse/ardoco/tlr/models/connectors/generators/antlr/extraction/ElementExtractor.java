package edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.extraction;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import org.antlr.v4.runtime.CommonTokenStream;

import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.management.ElementStorageRegistry;

/**
 * Is responsible for extracting elements from an directory. The extracted
 * elements are then stored in an ElementStorageRegistry.
 * The extraction process is implemented by the implementing class. The
 * extraction process is done by building a token stream from
 * a file via ANTLR and extracting the elements from the token stream.
 * After the extraction of structural elements, the CommentExtractor is called
 * to extract comments from the file and added to the
 * ElementStorageRegistry.
 */
public abstract class ElementExtractor {
    protected CommentExtractor commentExtractor;

    protected ElementExtractor() {
    }

    public abstract ElementStorageRegistry getElements();

    public void extract(String directoryPath) {
        List<Path> files = getFiles(directoryPath);
        for (Path file : files) {
            extractContent(file);
        }
    }

    protected void extractContent(Path file) {
        CommonTokenStream tokens;
        try {
            tokens = buildTokens(file);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        extractElements(tokens);
        extractComments(file, tokens);
    }

    protected void extractComments(Path file, CommonTokenStream tokens) {
        String path = file.toString();
        commentExtractor.extract(path, tokens);
    }

    protected abstract List<Path> getFiles(String directoryPath);

    protected abstract CommonTokenStream buildTokens(Path file) throws IOException;

    protected abstract void extractElements(CommonTokenStream tokens);
}
