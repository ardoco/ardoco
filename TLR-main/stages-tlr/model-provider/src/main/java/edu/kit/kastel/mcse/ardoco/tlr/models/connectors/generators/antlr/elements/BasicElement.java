package edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements;

public class BasicElement {
    private final String path;
    private final String name;
    private int fromLine;
    private int toLine;
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

    public int getFromLine() {
        return fromLine;
    }

    public int getToLine() {
        return toLine;
    }

    public String getComment() {
        return comment;
    }

    public void setFromLine(int fromLine) {
        this.fromLine = fromLine;
    }

    public void setToLine(int toLine) {
        this.toLine = toLine;
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
}
