package edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements;

public class ControlElement extends BasicElement {
    private final Parent parent;

    public ControlElement(String name, String path, Parent parent) {
        super(name, path);
        this.parent = parent;
    }

    public Parent getParent() {
        return parent;
    }

}