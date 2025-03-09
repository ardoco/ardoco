package edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.management;

import java.util.ArrayList;
import java.util.List;

import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.Element;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.Type;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.commentmatching.CppCommentMatcher;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.ClassElement;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.Parent;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.VariableElement;

public class CppElementManager extends ElementManager {
    private ElementStorage<VariableElement> variables;
    private ElementStorage<Element> functions;
    private ElementStorage<ClassElement> classes;
    private ElementStorage<Element> namespaces;
    private ElementStorage<Element> files;

    public CppElementManager() {
        this.variables = new ElementStorage<>();
        this.functions = new ElementStorage<>();
        this.classes = new ElementStorage<>();
        this.namespaces = new ElementStorage<>();
        this.files = new ElementStorage<>();
    }

    public CppElementManager(List<VariableElement> variables, List<Element> functions, List<ClassElement> classes,
            List<Element> namespaces, List<Element> files) {
        this();
        addVariables(variables);
        addFunctions(functions);
        addClasses(classes);
        addNamespaces(namespaces);
        addFiles(files);
    }

    @Override
    public List<Element> getContentOfParent(Parent parent) {
        List<Element> elements = new ArrayList<>();
        elements.addAll(getVariablesWithParent(parent));
        elements.addAll(getFunctionsWithParent(parent));
        elements.addAll(getClassesWithParent(parent));
        elements.addAll(getNamespacesWithParent(parent));
        return elements;
    }

    public void addVariable(VariableElement variable) {
        variables.addElement(variable);
    }

    public void addFunction(Element function) {
        functions.addElement(function);
    }

    public void addClass(ClassElement clazz) {
        classes.addElement(clazz);
    }

    public void addNamespace(Element namespace) {
        namespaces.addElement(namespace);
    }

    public void addFile(Element file) {
        files.addElement(file);
    }

    public void addVariables(List<VariableElement> variables) {
        this.variables.addElements(variables);
    }

    public void addFunctions(List<Element> functions) {
        this.functions.addElements(functions);
    }

    public void addClasses(List<ClassElement> classes) {
        this.classes.addElements(classes);
    }

    public void addNamespaces(List<Element> namespaces) {
        this.namespaces.addElements(namespaces);
    }

    public void addFiles(List<Element> files) {
        this.files.addElements(files);
    }

    public Element getFile(Parent parent) {
        return this.files.getElement(parent);
    }

    public Element getNamespace(Parent parent) {
        return this.namespaces.getElement(parent);
    }

    public VariableElement getVariable(Parent parent) {
        return this.variables.getElement(parent);
    }

    public ClassElement getClass(Parent parent) {
        return this.classes.getElement(parent);
    }

    public Element getFunction(Parent parent) {
        return this.functions.getElement(parent);
    }

    public List<Element> getFunctions() {
        return functions.getElements();
    }

    public List<Element> getNamespaces() {
        return namespaces.getElements();
    }

    public List<VariableElement> getVariables() {
        return variables.getElements();
    }

    public List<ClassElement> getClasses() {
        return classes.getElements();
    }

    public List<Element> getFiles() {
        return files.getElements();
    }

    public boolean isNamespaceElement(Element element) {
        return namespaces.contains(element);
    }

    public boolean isVariableElement(Element element) {
        return variables.contains(element);
    }

    public boolean isClassElement(Element element) {
        return classes.contains(element);
    }

    public boolean isFunctionElement(Element element) {
        return functions.contains(element);
    }

    public boolean isFileElement(Element element) {
        return files.contains(element);
    }

    public List<VariableElement> getVariablesWithParent(Parent parent) {
        return variables.getContentOfParent(parent);
    }

    public List<Element> getFunctionsWithParent(Parent parent) {
        return functions.getContentOfParent(parent);
    }

    public List<ClassElement> getClassesWithParent(Parent parent) {
        return classes.getContentOfParent(parent);
    }

    public List<Element> getNamespacesWithParent(Parent parent) {
        return namespaces.getContentOfParent(parent);
    }

    @Override
    protected List<Element> getAllElements() {
        List<Element> elements = new ArrayList<>();
        elements.addAll(variables.getElements());
        elements.addAll(functions.getElements());
        elements.addAll(classes.getElements());
        elements.addAll(namespaces.getElements());
        return elements;
    }

    @Override
    protected CppCommentMatcher buildCommentMatcher() {
        return new CppCommentMatcher();
    }

    @Override
    public Element getElement(Parent parent) {
        if (parent.getType() == Type.VARIABLE) {
            return getVariable(parent);
        } else if (parent.getType() == Type.FUNCTION) {
            return getFunction(parent);
        } else if (parent.getType() == Type.CLASS) {
            return getClass(parent);
        } else if (parent.getType() == Type.NAMESPACE) {
            return getNamespace(parent);
        } else if (parent.getType() == Type.FILE) {
            return getFile(parent);
        }
        return null;
    }

}
