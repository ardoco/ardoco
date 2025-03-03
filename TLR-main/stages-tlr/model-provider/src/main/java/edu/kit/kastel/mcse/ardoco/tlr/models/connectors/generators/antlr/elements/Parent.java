package edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements;

import java.util.Objects;

public class Parent extends BasicElement {
    private final BasicType type;

    public Parent(String name, String path, BasicType parentType) {
        super(name, path);
        this.type = parentType;
    }

    public BasicType getType() {
        return type;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Parent) {
            Parent parent = (Parent) obj;
            return parent.getName().equals(this.getName()) && parent.getType().equals(this.getType())
                    && parent.getPath().equals(this.getPath());
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(getName(), getPath(), getType());
    }

}
