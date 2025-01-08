package edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements;

public class Parent extends BasicElement {
    private final BasicType parentType;

    public Parent(String name, BasicType parentType) {
        super(name);
        this.parentType = parentType;
    }

    public BasicType getParentType() {
        return parentType;
    }
    
}
