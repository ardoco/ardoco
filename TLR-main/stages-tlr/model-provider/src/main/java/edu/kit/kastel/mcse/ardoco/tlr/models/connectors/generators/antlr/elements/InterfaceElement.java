package edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements;

public class InterfaceElement extends BasicElement {
    private final Parent parent;

    public InterfaceElement(String name, Parent parent) {
        super(name);
        this.parent = parent;
    }

    public Parent getParent() {
        return parent;
    } 
}