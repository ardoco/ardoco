package edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements;

public class Parent extends BasicElement {
    private final BasicType type;

    public Parent(String name, BasicType parentType) {
        super(name);
        this.type = parentType;
    }

    public BasicType getType() {
        return type;
    }
    
}
