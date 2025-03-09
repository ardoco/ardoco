package edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.management.cpp;

import java.util.List;

import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.Element;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.Type;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.commentmatching.CppCommentMatcher;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.ClassElement;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.Parent;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.VariableElement;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.management.ElementManager;

public class CppElementManager extends ElementManager {


    public CppElementManager() {
        super(new CppElementStorageRegistry(), new CppCommentMatcher());
    }

    public CppElementManager(List<VariableElement> variables, List<Element> functions, List<ClassElement> classes,
            List<Element> namespaces, List<Element> files) {
        this();
        addElements(Type.VARIABLE, variables);
        addElements(Type.FUNCTION, functions);
        addElements(Type.CLASS, classes);
        addElements(Type.NAMESPACE, namespaces);
        addElements(Type.FILE, files);
    }

    public void addVariable(VariableElement variable) {
        elementStorageRegistry.addElement(Type.VARIABLE, variable);
    }

    public void addFunction(Element function) {
        elementStorageRegistry.addElement(Type.FUNCTION, function);
    }

    public void addClass(ClassElement clazz) {
        elementStorageRegistry.addElement(Type.CLASS, clazz);
    }

    public void addNamespace(Element namespace) {
        elementStorageRegistry.addElement(Type.NAMESPACE, namespace);
    }

    public void addFile(Element file) {
        elementStorageRegistry.addElement(Type.FILE, file);
    }

    public void addVariables(List<VariableElement> variables) {
        elementStorageRegistry.addElements(Type.VARIABLE, variables);
    }

    public void addFunctions(List<Element> functions) {
        elementStorageRegistry.addElements(Type.FUNCTION, functions);
    }

    public void addClasses(List<ClassElement> classes) {
        elementStorageRegistry.addElements(Type.CLASS, classes);
    }

    public void addNamespaces(List<Element> namespaces) {
        elementStorageRegistry.addElements(Type.NAMESPACE, namespaces);
    }

    public void addFiles(List<Element> files) {
        elementStorageRegistry.addElements(Type.FILE, files);
    }

    public VariableElement getVariable(Parent parent) {
        return elementStorageRegistry.getElement(parent, VariableElement.class);
    }

    public Element getFunction(Parent parent) {
        return elementStorageRegistry.getElement(parent, Element.class);
    }

    public ClassElement getClass(Parent parent) {
        return elementStorageRegistry.getElement(parent, ClassElement.class);
    }

    public Element getNamespace(Parent parent) {
        return elementStorageRegistry.getElement(parent, Element.class);
    }

    public Element getFile(Parent parent) {
        return elementStorageRegistry.getElement(parent, Element.class);
    }

    public List<VariableElement> getVariables() {
        return elementStorageRegistry.getElements(Type.VARIABLE, VariableElement.class);
    }

    public List<Element> getFunctions() {
        return elementStorageRegistry.getElements(Type.FUNCTION, Element.class);
    }

    public List<ClassElement> getClasses() {
        return elementStorageRegistry.getElements(Type.CLASS, ClassElement.class);
    }

    public List<Element> getNamespaces() {
        return elementStorageRegistry.getElements(Type.NAMESPACE, Element.class);
    }

    public List<Element> getFiles() {
        return elementStorageRegistry.getElements(Type.FILE, Element.class);
    }

    public boolean isNamespaceElement(Element element) {
        return isElement(Type.NAMESPACE, element);
    }

    public boolean isVariableElement(Element element) {
        return isElement(Type.VARIABLE, element);
    }

    public boolean isClassElement(Element element) {
        return isElement(Type.CLASS, element);
    }

    public boolean isFunctionElement(Element element) {
        return isElement(Type.FUNCTION, element);
    }

    public boolean isFileElement(Element element) {
        return isElement(Type.FILE, element);
    }

    public List<VariableElement> getVariablesWithParent(Parent parent) {
        return elementStorageRegistry.getContentOfParent(Type.VARIABLE, parent);
    }

    public List<Element> getFunctionsWithParent(Parent parent) {
        return elementStorageRegistry.getContentOfParent(Type.FUNCTION, parent);
    }

    public List<ClassElement> getClassesWithParent(Parent parent) {
        return elementStorageRegistry.getContentOfParent(Type.CLASS, parent);
    }

    public List<Element> getNamespacesWithParent(Parent parent) {
        return elementStorageRegistry.getContentOfParent(Type.NAMESPACE, parent);
    }
}
