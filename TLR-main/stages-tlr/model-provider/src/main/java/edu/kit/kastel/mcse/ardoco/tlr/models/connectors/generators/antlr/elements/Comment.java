package edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements;

public class Comment {
    private String text;
    private int startLine;
    private int endLine;
    private String path;

    public Comment(String text, int startLine, int endLine, String path) {
        this.text = text;
        this.startLine = startLine;
        this.endLine = endLine;
        this.path = path;
    }

    public String getText() {
        return text;
    }

    public int getStartLine() {
        return this.startLine;
    }

    public int getEndLine() {
        return this.endLine;
    }

    public String getPath() {
        return path;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Comment) {
            Comment commentElement = (Comment) obj;
            return commentElement.getText().equals(this.getText())
                    && commentElement.getStartLine() == this.getStartLine()
                    && commentElement.getEndLine() == this.getEndLine()
                    && commentElement.getPath().equals(this.getPath());
        }
        return false;
    }

    @Override
    public int hashCode() {
        return this.text.hashCode() + this.startLine + this.endLine + this.path.hashCode();
    }

}
