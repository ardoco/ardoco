package edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements;

public class VariableElement extends Element {
    private final String dataType;

    public VariableElement(String name, String path, String dataType, Parent parent) {
        super(name, path, parent);
        this.dataType = dataType;
    }

    public VariableElement(String name, String path, String dataType, Parent parent, int startLine, int endLine) {
        super(name, path, parent, startLine, endLine);
        this.dataType = dataType;
    }

    public String getDataType() {
        return dataType;
    }

}
