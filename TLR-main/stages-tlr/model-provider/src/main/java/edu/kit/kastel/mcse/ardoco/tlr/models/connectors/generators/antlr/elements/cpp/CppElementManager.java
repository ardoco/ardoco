package edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.cpp;

import java.util.ArrayList;
import java.util.List;

import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.BasicElement;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.BasicType;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.ClassElement;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.ElementManager;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.Parent;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.VariableElement;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.mapping.cpp.CppCommentMapper;

public class CppElementManager extends ElementManager {
    private List<VariableElement> variables;
    private List<BasicElement> functions;
    private List<ClassElement> classes;
    private List<BasicElement> namespaces;
    
    
    public CppElementManager() {
        this.variables = new ArrayList<>();
        this.functions = new ArrayList<>();
        this.classes = new ArrayList<>();
        this.namespaces = new ArrayList<>();
    }

    @Override
    public List<BasicElement> getElementsWithParent(Parent parent) {
        return getBasicElementsWithParent(getAllElements(), parent);
    }

    @Override
    protected boolean isRootParent(Parent parent) {
        return parent.getType() == BasicType.FILE;
    }

    public void addVariables(List<VariableElement> variables) {
        for (VariableElement variable : variables) {
            if (!this.variables.contains(variable)) {
                this.variables.add(variable);
            }
        }
    }

    public void addFunctions(List<BasicElement> functions) {
        for (BasicElement function : functions) {
            if (!this.functions.contains(function)) {
                this.functions.add(function);
            }
        }
    }

    public void addClasses(List<ClassElement> classes) {
        for (ClassElement clazz : classes) {
            if (!this.classes.contains(clazz)) {
                this.classes.add(clazz);
            }
        }
    }

    public void addNamespaces(List<BasicElement> namespaces) {
        for (BasicElement namespace : namespaces) {
            if (!this.namespaces.contains(namespace)) {
                this.namespaces.add(namespace);
            }
        }
    }

    public BasicElement getNamespace(Parent parent) {
        for (BasicElement namespace : namespaces) {
            if (namespace.getParent().equals(parent)) {
                return namespace;
            }
        }
        return null;
    }

    public VariableElement getVariable(Parent parent) {
        for (VariableElement variable : variables) {
            if (variable.getParent().equals(parent)) {
                return variable;
            }
        }
        return null;
    }

    public ClassElement getClass(Parent parent) {
        for (ClassElement clazz : classes) {
            if (clazz.getParent().equals(parent)) {
                return clazz;
            }
        }
        return null;
    }

    public BasicElement getFunction(Parent parent) {
        for (BasicElement function : functions) {
            if (function.getParent().equals(parent)) {
                return function;
            }
        }
        return null;
    }

    public List<BasicElement> getFunctions() {
        return functions;
    }

    public List<BasicElement> getNamespaces() {
        return namespaces;
    }

    public List<VariableElement> getVariables() {
        return variables;
    }

    public List<ClassElement> getClasses() {
        return classes;
    }



    public boolean isNamespaceElement(BasicElement element) {
        return namespaces.contains(element);
    }

    public boolean isVariableElement(BasicElement element) {
        return variables.contains(element);
    }

    public boolean isClassElement(BasicElement element) {
        return classes.contains(element);
    }

    public boolean isFunctionElement(BasicElement element) {
        return functions.contains(element);
    }

    public List<VariableElement> getVariablesWithParent(Parent parent) {
        List <VariableElement> variablesWithMatchingParent = new ArrayList<>();
        for (VariableElement variable : variables) {
            if (variable.getParent().equals(parent)) {
                variablesWithMatchingParent.add(variable);
            }
        }
        return variablesWithMatchingParent;
    }

    public List<BasicElement> getfunctionsWithParent(Parent parent) {
        return getBasicElementsWithParent(functions, parent);
    }

    public List<ClassElement> getClassesWithParent(Parent parent) {
        List <ClassElement> classesWithMatchingParent = new ArrayList<>();
        for (ClassElement clazz : classes) {
            if (clazz.getParent().equals(parent)) {
                classesWithMatchingParent.add(clazz);
            }
        }
        return classesWithMatchingParent;
    }

    public List<BasicElement> getNamespacesWithParent(Parent parent) {
        return getBasicElementsWithParent(namespaces, parent);
    }

    @Override
    protected List<BasicElement> getAllElements() {
        List<BasicElement> elements = new ArrayList<>();
        elements.addAll(variables);
        elements.addAll(functions);
        elements.addAll(classes);
        elements.addAll(namespaces);
        return elements;
    }

    @Override
    protected CppCommentMapper buildCommentMatcher() {
        return new CppCommentMapper();
    }

}
