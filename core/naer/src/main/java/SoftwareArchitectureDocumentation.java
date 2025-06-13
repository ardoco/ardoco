import edu.kit.kastel.mcse.ardoco.core.api.text.Sentence;
import edu.kit.kastel.mcse.ardoco.core.api.text.Word;
import org.eclipse.collections.api.list.ImmutableList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

/**
 * represents a textual software architecture documentation (SAD)
 */
public class SoftwareArchitectureDocumentation implements edu.kit.kastel.mcse.ardoco.core.api.text.Text {
    private final Logger logger = LoggerFactory.getLogger(SoftwareArchitectureDocumentation.class);
    /**
     * text of the SAD (format must be: one-sentence-per-line)
     */
    private final String text;
    /**
     * path to the SAD (if available)
     */
    private final Optional<Path> filePath;

    public SoftwareArchitectureDocumentation(Path filePath) {
        if (filePath == null) {
            logger.error("filePath is null");
            throw new IllegalArgumentException("filePath is null");
        }
        if (!Files.isRegularFile(filePath)) {
            logger.error("filePath is not a regular file");
            throw new IllegalArgumentException("filePath is not a regular file");
        }

        this.filePath = Optional.of(filePath);
        try {
            this.text = Files.readString(filePath);
        } catch (IOException e) {
            logger.error("error reading SAD file: {}", filePath);
            throw new IllegalArgumentException("error reading SAD file: " + filePath);
        }

        checkTextFormat();
    }

    public SoftwareArchitectureDocumentation(String sadText) {
        logger.warn("no filePath given");
        if (sadText == null) {
            logger.error("sadText is null");
            throw new IllegalArgumentException("sadText is null");
        }
        if (sadText.isEmpty()) logger.warn("sadText is empty");


        this.filePath = Optional.empty();
        this.text = sadText;

        checkTextFormat();
    }

    /**
     * check weather the SAD text is in the required one-sentence-per-line format
     */
    private void checkTextFormat() {
        if (text == null) {
            logAndThrow("text is null");
        }
        if (text.isBlank()) {
            logAndThrow("text is blank");
        }

        String[] lines = text.split("\\R"); // splits on any line break

        for (String line : lines) {
            String trimmed = line.trim();

            if (trimmed.isEmpty()) {
                logAndThrow("empty line detected");
            }

            // check: ends with a sentence terminator
            if (!trimmed.matches(".*[.!?]$")) {
                logAndThrow("line does not end with a sentence terminator: \"" + trimmed + "\"");
            }

            //TODO Frage: Ist es ok den check einf weg zu lassen weil dann hat man wieder probleme wegen sowas wie z.B. "i.e." (wir speichern den text ja jetzt schon mit also is mir ja eig relativ egal wie die den aufsplitten)

            // check: no other sentence terminators inside the line (excluding the final one)
            /*String inner = trimmed.substring(0, trimmed.length() - 1);
            if (inner.matches(".*[.!?].*")) {
                logAndThrow("line contains multiple sentence terminators: \"" + trimmed + "\"");
            }*/
        }
    }

    private void logAndThrow(String message) {
        logger.error("invalid SAD text format: {}", message);
        throw new IllegalArgumentException("invalid SAD text format: " + message);
    }

    public String getText() {
        return text;
    }

    public Optional<Path> getFilePath() {
        return filePath;
    }

    @Override
    public ImmutableList<Word> words() {
        //TODO Frage: hier könnte man dann impl hinzufügen um es für Ardoco zu nutzen (oder erstmal ohne implements Text machen?)
        return null;
    }

    @Override
    public Word getWord(int i) {
        return null;
    }

    @Override
    public ImmutableList<Sentence> getSentences() {
        return null;

    }
}
