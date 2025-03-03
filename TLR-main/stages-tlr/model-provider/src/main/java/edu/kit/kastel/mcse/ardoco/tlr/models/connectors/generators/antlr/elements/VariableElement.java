package edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements;

public class VariableElement extends BasicElement {
    private final String type;
    private final Parent parent;

    public VariableElement(String name, String path, String type, Parent parent) {
        super(name, path);
        this.type = type;
        this.parent = parent;
    }

    public String getType() {
        return type;
    }

    public Parent getParent() {
        return parent;
    }

}
