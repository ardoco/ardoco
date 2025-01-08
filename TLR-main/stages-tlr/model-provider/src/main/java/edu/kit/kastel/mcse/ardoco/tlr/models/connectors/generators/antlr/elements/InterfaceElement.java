package edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements;

public class InterfaceElement extends BasicElement {
    private final Parent parent;
    private final String packageName;

    public InterfaceElement(String name, Parent parent, String packageName) {
        super(name);
        this.parent = parent;
        this.packageName = packageName;
    }

    public Parent getParent() {
        return parent;
    }

    public String getPackageName() {
        return packageName;
    }
    
}
