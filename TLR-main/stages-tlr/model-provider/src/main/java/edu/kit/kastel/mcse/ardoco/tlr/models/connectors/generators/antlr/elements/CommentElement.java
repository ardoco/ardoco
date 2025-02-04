package edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements;

public class CommentElement extends BasicElement {
    private String comment;
    private Parent parent;

    public CommentElement(String name, String path, Parent parent, String comment) {
        super(name, path);
        this.comment = comment;
        this.parent = parent;
    }

    public String getComment() {
        return comment;
    }

    public Parent getParent() {
        return parent;
    }
    
}
