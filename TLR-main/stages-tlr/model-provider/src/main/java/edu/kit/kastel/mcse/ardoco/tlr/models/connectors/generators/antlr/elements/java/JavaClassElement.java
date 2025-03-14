package edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.java;

import java.util.ArrayList;
import java.util.List;

import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.Element;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.ElementIdentifier;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.Type;

public class JavaClassElement extends Element {
    private static final Type type = Type.CLASS;
    private String extendsClass;
    private final List<String> implementedInterfaces;

    public JavaClassElement(String name, String path, ElementIdentifier parentIdentifier, int startLine, int endLine) {
        super(name, path, type, parentIdentifier, startLine, endLine);
        this.extendsClass = "";
        this.implementedInterfaces = new ArrayList<String>();
    }

    public JavaClassElement(String name, String path, ElementIdentifier parentIdentifier, String extendsClass,
            List<String> implementedInterfaces, int startLine, int endLine) {
        super(name, path, type, parentIdentifier, startLine, endLine);
        this.extendsClass = extendsClass;
        this.implementedInterfaces = implementedInterfaces;
    }

    public String getExtendsClass() {
        return extendsClass;
    }

    public void addImplementedInterfaces(List<String> interfaces) {
        this.implementedInterfaces.addAll(interfaces);
    }

    public List<String> getImplementedInterfaces() {
        return implementedInterfaces;
    }

}
