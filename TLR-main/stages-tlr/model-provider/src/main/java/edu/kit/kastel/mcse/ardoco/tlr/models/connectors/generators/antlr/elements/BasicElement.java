package edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements;

public class BasicElement {
    private final String path;
    private final String name;

    public BasicElement(String name, String path) {
        this.name = name;
        this.path = path;

    }

    public String getName() {
        return name;
    }

    public String getPath() {
        return path;
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
