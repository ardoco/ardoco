package edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.java;

import java.util.ArrayList;
import java.util.List;

import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.BasicElement;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.Parent;

public class JavaClassElement extends BasicElement {
    private final Parent parent;
    private String extendsClass;
    private final List<String> implementedInterfaces;


    public JavaClassElement(String name, String path, Parent parent) {
        super(name, path);
        this.parent = parent;
        this.extendsClass = "";
        this.implementedInterfaces = new ArrayList<String>();
    }

   public JavaClassElement(String name, String path, Parent parent, String extendsClass, List<String> implementedInterfaces) {
        super(name, path);
        this.parent = parent;
        this.extendsClass = extendsClass;
        this.implementedInterfaces = implementedInterfaces;
    }

    public Parent getParent() {
        return parent;
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
