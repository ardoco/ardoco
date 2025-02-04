package edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.python3;

import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.BasicElement;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.Parent;

public class Python3VariableElement extends BasicElement {
    private String type;
    private final Parent parent;
    private final String value;
    private boolean typeHasBeenSet = false;


    public Python3VariableElement(String name, String path, String type, Parent parent, String value) {
        super(name, path);
        this.type = type;
        this.parent = parent;
        this.value = value;
    }

    public String getType() {
        return type;
    }

    public Parent getParent() {
        return parent;
    }

    public String getValue() {
        return value;
    }

    public boolean setType(String type) {
        if (!typeHasBeenSet) {
            this.type = type;
            typeHasBeenSet = true;
            return true;
        }
        return false;
    }

    
}
