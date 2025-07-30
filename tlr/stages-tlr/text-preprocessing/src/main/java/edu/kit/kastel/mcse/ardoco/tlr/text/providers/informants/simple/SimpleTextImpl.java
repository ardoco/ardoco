package edu.kit.kastel.mcse.ardoco.tlr.text.providers.informants.simple;

import java.util.ArrayList;
import java.util.List;

import edu.kit.kastel.mcse.ardoco.core.api.text.SimpleText;

public class SimpleTextImpl implements SimpleText {

    private final String text;
    private final List<String> lines;

    public SimpleTextImpl(String text) {
        this.lines = createLinesFromText(text);
        this.text = createCleanTextFromLines(lines);
    }

    @Override
    public String getText() {
        return text;
    }

    private String createCleanTextFromLines(List<String> lines) {
        return String.join(System.lineSeparator(), lines);
    }

    private static List<String> createLinesFromText(String text) {
        List<String> lines = new ArrayList<>();
        var splitText = text.split("\\R");
        for (var line : splitText) {
            line = line.trim();
            if (!line.isBlank()) {
                if (!line.matches(".*[.!?]$")) {
                    line = line + ".";
                }
                lines.add(line);
            }
        }
        return lines;
    }

    @Override
    public List<String> getLines() {
        return this.lines;
    }
}
