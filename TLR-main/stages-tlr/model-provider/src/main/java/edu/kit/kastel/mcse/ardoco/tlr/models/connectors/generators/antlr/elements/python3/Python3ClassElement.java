package edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.python3;

import java.util.ArrayList;
import java.util.List;

import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.BasicElement;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.Parent;

public class Python3ClassElement extends BasicElement {
    private final Parent parent;
    private final List<String> isChildOfClasses;

    public Python3ClassElement(String name, Parent parent) {
        super(name);
        this.parent = parent;
        this.isChildOfClasses = new ArrayList<String>();
    }

    public Python3ClassElement(String name, Parent parent, List<String> isChildOfClasses) {
        super(name);
        this.parent = parent;
        this.isChildOfClasses = isChildOfClasses;
    }

    public Parent getParent() {
        return parent;
    }

    public List<String> getIsChildOfClasses() {
        return this.isChildOfClasses;
    }
    
}
