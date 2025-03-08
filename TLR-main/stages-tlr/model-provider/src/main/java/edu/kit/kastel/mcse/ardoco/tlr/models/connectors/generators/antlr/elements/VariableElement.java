package edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements;

public class VariableElement extends Element {
    private final String dataType;

    public VariableElement(String name, String path, String dataType, Parent parent) {
        super(name, path, parent);
        this.dataType = dataType;
    }

    public String getDataType() {
        return dataType;
    }

}
