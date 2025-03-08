package edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements;

import java.util.Objects;

public class Element {
    private Parent parent;
    private final String path;
    private final String name;
    private int startLine;
    private int endLine;
    private String comment;

    public Element(String name, String path) {
        this.name = name;
        this.path = path;
        this.comment = "";
        this.startLine = -1;
        this.endLine = -1;

    }

    public Element(String name, String path, Parent parent) {
        this(name, path);
        this.parent = parent;
    }

    protected void setParent(Parent parent) {
        this.parent = parent;
    }

    public Parent getParent() {
        return parent;
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

    public void setComment(String comment) {
        if (this.comment != "") {
            this.comment += "\n";
        }

        this.comment += comment;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Element) {
            Element basicElement = (Element) obj;
            return Objects.equals(basicElement.getName(), this.getName())
                    && Objects.equals(basicElement.getPath(), this.getPath())
                    && Objects.equals(basicElement.getParent(), this.getParent())
                    && basicElement.getStartLine() == this.getStartLine()
                    && basicElement.getEndLine() == this.getEndLine();
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, path);
    }
}