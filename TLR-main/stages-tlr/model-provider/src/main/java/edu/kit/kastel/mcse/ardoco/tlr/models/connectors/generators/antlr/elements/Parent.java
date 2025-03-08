package edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements;

import java.util.Objects;

public class Parent {
    private final String name;
    private final String path;
    private final Type type;

    public Parent(String name, String path, Type type) {
        this.name = name;
        this.path = path;
        this.type = type;
    }

    public Type getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public String getPath() {
        return path;
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
