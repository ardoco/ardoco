package edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.python3;

import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.BasicElement;

public class Python3ModuleElement extends BasicElement {
    private final String packageName;
    private final String pathString;

    public Python3ModuleElement(String name, String pathString, String packageName) {
        super(name);
        this.pathString = pathString;
        this.packageName = packageName;
    }

    public String getPathString() {
        return pathString;
    }

    public String getPackageName() {
        return packageName;
    }
    
}
