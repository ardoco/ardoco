package edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements;

public class VariableElement extends Element {
    private static final Type type = Type.VARIABLE;
    private final String dataType;

    public VariableElement(String name, String path, String dataType, ElementIdentifier parentIdentifier) {
        super(name, path, type, parentIdentifier);
        this.dataType = dataType;
    }

    public VariableElement(String name, String path, String dataType, ElementIdentifier parentIdentifier, int startLine, int endLine) {
        super(name, path, type, parentIdentifier, startLine, endLine);
        this.dataType = dataType;
    }

    public String getDataType() {
        return dataType;
    }

}
