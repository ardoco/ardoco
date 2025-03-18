package edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements;

import java.util.ArrayList;
import java.util.List;

public class ClassElement extends Element {
    private final List<String> inherits;
    private static final Type type = Type.CLASS;

    public ClassElement(String name, String path, ElementIdentifier parentIdentifier) {
        super(name, path, type, parentIdentifier);
        this.inherits = new ArrayList<>();
    }

    public ClassElement(String name, String path, ElementIdentifier parentIdentifier, int startLine, int endLine, List<String> inherits) {
        super(name, path, type, parentIdentifier, startLine, endLine);
        this.inherits = inherits;
    }

    public ClassElement(String name, String path, ElementIdentifier parentIdentifier, int startLine, int endLine) {
        super(name, path, type, parentIdentifier, startLine, endLine);
        this.inherits = new ArrayList<>();
    }

    public ClassElement(ElementIdentifier identifier, ElementIdentifier identifierOfParent, int startLine, int endLine, List<String> inherits) {
        super(identifier, identifierOfParent, startLine, endLine);
        this.inherits = inherits;
    }

    public List<String> getInherits() {
        return this.inherits;
    }

}
