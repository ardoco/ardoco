package edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.cpp;

import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.BasicElement;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.Parent;

public class NamespaceElement extends BasicElement{
    private final Parent parent;

    public NamespaceElement(String name, String path, Parent parent) {
        super(name, path);
        this.parent = parent;
    }

    public Parent getParent() {
        return parent;
    }


    
}
