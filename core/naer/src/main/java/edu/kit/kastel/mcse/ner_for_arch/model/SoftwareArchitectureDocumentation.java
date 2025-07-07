package edu.kit.kastel.mcse.ner_for_arch.model;

import org.apache.commons.text.similarity.JaccardSimilarity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Represents a textual software architecture documentation (SAD).
 */
public class SoftwareArchitectureDocumentation {
    private final Logger logger = LoggerFactory.getLogger(SoftwareArchitectureDocumentation.class);

    /**
     * lines of the SAD text
     */
    private final String[] lines;
    /**
     * path to the SAD (if available - otherwise it is null)
     */
    private final Path filePath;

    /**
     * Constructs a {@link SoftwareArchitectureDocumentation} instance by loading the content of the specified file into memory.
     *
     * @param filePath the path to the SAD file; needs to be in a one-sentence-per-line format!
     */
    public SoftwareArchitectureDocumentation(Path filePath) {
        if (filePath == null) {
            logger.error("filePath is null");
            throw new IllegalArgumentException("filePath is null");
        }
        if (!Files.isRegularFile(filePath)) {
            logger.error("filePath is not a regular file");
            throw new IllegalArgumentException("filePath is not a regular file");
        }

        this.filePath = filePath;
        try {
            this.lines = Files.readAllLines(filePath).toArray(new String[0]);
        } catch (IOException e) {
            logger.error("error reading SAD file: {}", filePath);
            throw new IllegalArgumentException("error reading SAD file: " + filePath);
        }

        checkTextFormat();
    }

    /**
     * Constructs a {@link SoftwareArchitectureDocumentation} instance by loading the content of the given text into memory.
     *
     * @param sadText the SAD text; must be in a one-sentence-per-line format!
     */
    public SoftwareArchitectureDocumentation(String sadText) {
        logger.warn("no filePath given");
        if (sadText == null) {
            logger.error("sadText is null");
            throw new IllegalArgumentException("sadText is null");
        }
        if (sadText.isEmpty()) logger.warn("sadText is empty");


        this.filePath = null;
        this.lines = sadText.split("\\R");

        checkTextFormat();
    }

    /**
     * check weather the SAD text is in the required one-sentence-per-line format
     */
    private void checkTextFormat() {
        for (String line : lines) {
            if (line == null) {
                logAndThrow("line is null");
            } else {
                String trimmed = line.trim();

                if (trimmed.isEmpty()) {
                    logAndThrow("empty line detected");
                }

                // check: ends with a sentence terminator
                if (!trimmed.matches(".*[.!?]$")) {
                    logAndThrow("line does not end with a sentence terminator: \"" + trimmed + "\"");
                }
            }

        }

    }

    private void logAndThrow(String message) {
        logger.error("invalid SAD text format: {}", message);
        throw new IllegalArgumentException("invalid SAD text format: " + message);
    }

    /**
     * Retrieves the complete text of the SAD.
     *
     * @return a string representation of the text, with lines concatenated by the current system's line separator
     */
    public String getText() {
        return String.join(System.lineSeparator(), lines);
    }

    /**
     * Returns the line with the specified line number.
     *
     * @param lineNumber the line number/index of the line to retrieve (starting at 1)
     * @return the requested line
     */
    public String getLine(int lineNumber) {
        lineNumber--; //to keep everything consistent, we start indexing at 1 (so we need to increment the line number to be able to handle it correctly with our internal array)
        if (lineNumber < 0 || lineNumber >= lines.length) {
            logger.error("line number {} out of range", lineNumber + 1);
            throw new IllegalArgumentException("line number " + (lineNumber + 1) + " out of range");
        }
        return lines[lineNumber];
    }

    public String[] getLines() {
        return lines;
    }

    public int getLineCount() {
        return lines.length;
    }

    public Path getFilePath() {
        return filePath;
    }

    /**
     * Determines the line number in the document that most closely matches the specified text line.
     * The Jaccard similarity metric is used for he comparison.
     *
     * @param textLine the text line to search for; comparison is case-insensitive
     * @return the line number (1-indexed) with the highest similarity to the provided text, or -1 if no line meets the similarity threshold (0.95 or higher)
     */
    public int getLineNumber(String textLine) {
        JaccardSimilarity jaccard = new JaccardSimilarity();
        int bestLineNumber = -1;
        double bestScore = 0.0;
        int currentLine = 0;

        for (String line : lines) {
            currentLine++;
            double score = jaccard.apply(line.toLowerCase(), textLine.toLowerCase());
            if (score > bestScore) {
                bestScore = score;
                bestLineNumber = currentLine;
            }
        }

        return bestScore >= 0.95 ? bestLineNumber : -1;
    }
}
