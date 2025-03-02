package edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements;

import java.util.ArrayList;
import java.util.List;

public class ClassElement extends BasicElement {
    private final Parent parent;
    private final List<String> inherits;

    public ClassElement(String name, String path, Parent parent) {
        super(name, path);
        this.parent = parent;
        this.inherits = new ArrayList<String>();
    }

    public ClassElement(String name, String path, Parent parent, List<String> inherits) {
        super(name, path);
        this.parent = parent;
        this.inherits = inherits;
    }

    public Parent getParent() {
        return parent;
    }

    public List<String> getInherits() {
        return this.inherits;
    }
    
}
