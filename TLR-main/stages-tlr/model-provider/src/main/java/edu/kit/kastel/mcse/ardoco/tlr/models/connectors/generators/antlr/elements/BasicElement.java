package edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements;

import java.util.Objects;

public class BasicElement {
    private final String path;
    private final String name;
    private int startLine;
    private int endLine;
    private String comment;

    public BasicElement(String name, String path) {
        this.name = name;
        this.path = path;
        this.comment = "";

    }

    public String getName() {
        return name;
    }

    public String getPath() {
        return path;
    }

    public int getStartLine() {
        return startLine;
    }

    public int getEndLine() {
        return endLine;
    }

    public String getComment() {
        return comment;
    }

    public void setStartLine(int fromLine) {
        this.startLine = fromLine;
    }

    public void setEndLine(int toLine) {
        this.endLine = toLine;
    }

    public String setComment(String comment) {
        return this.comment += comment + "\n";
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof BasicElement) {
            BasicElement basicElement = (BasicElement) obj;
            return basicElement.getName().equals(this.getName()) && basicElement.getPath().equals(this.getPath());
        }
        return false;
    }

    @Override 
    public int hashCode() {
        return Objects.hash(name, path);
    }
}
