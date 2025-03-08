package edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.java;

import java.util.ArrayList;
import java.util.List;

import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.Element;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.Parent;

public class JavaClassElement extends Element {
    private String extendsClass;
    private final List<String> implementedInterfaces;

    public JavaClassElement(String name, String path, Parent parent) {
        super(name, path);
        setParent(parent);
        this.extendsClass = "";
        this.implementedInterfaces = new ArrayList<String>();
    }

    public JavaClassElement(String name, String path, Parent parent, String extendsClass,
            List<String> implementedInterfaces) {
        super(name, path);
        setParent(parent);
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
