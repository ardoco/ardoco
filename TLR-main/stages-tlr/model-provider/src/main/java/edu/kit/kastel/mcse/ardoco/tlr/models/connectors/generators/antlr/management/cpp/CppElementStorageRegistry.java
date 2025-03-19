package edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.management.cpp;

import java.util.List;

import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.Element;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.Type;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.commentmatching.CppCommentMatcher;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.ClassElement;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.ElementIdentifier;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.VariableElement;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.management.ElementStorage;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.management.ElementStorageRegistry;

public class CppElementStorageRegistry extends ElementStorageRegistry {


    public CppElementStorageRegistry() {
        super(new CppCommentMatcher());
    }

    public CppElementStorageRegistry(List<VariableElement> variables, List<Element> functions, List<ClassElement> classes,
            List<Element> namespaces, List<Element> files) {
        this();
        addElements(Type.VARIABLE, variables);
        addElements(Type.FUNCTION, functions);
        addElements(Type.CLASS, classes);
        addElements(Type.NAMESPACE, namespaces);
        addElements(Type.FILE, files);
    }

    @Override
    protected void registerStorage() {
        registerStorage(Type.VARIABLE, new ElementStorage<VariableElement>(VariableElement.class));
        registerStorage(Type.FUNCTION, new ElementStorage<Element>(Element.class));
        registerStorage(Type.CLASS, new ElementStorage<ClassElement>(ClassElement.class));
        registerStorage(Type.NAMESPACE, new ElementStorage<Element>(Element.class));
        registerStorage(Type.FILE, new ElementStorage<Element>(Element.class));
    }


    public void addVariable(VariableElement variable) {
        addElement(Type.VARIABLE, variable);
    }

    public void addFunction(Element function) {
        addElement(Type.FUNCTION, function);
    }

    public void addClass(ClassElement clazz) {
        addElement(Type.CLASS, clazz);
    }

    public void addNamespace(Element namespace) {
        addElement(Type.NAMESPACE, namespace);
    }

    public void addFile(Element file) {
        addElement(Type.FILE, file);
    }

    public void addVariables(List<VariableElement> variables) {
        addElements(Type.VARIABLE, variables);
    }

    public void addFunctions(List<Element> functions) {
        addElements(Type.FUNCTION, functions);
    }

    public void addClasses(List<ClassElement> classes) {
        addElements(Type.CLASS, classes);
    }

    public void addNamespaces(List<Element> namespaces) {
        addElements(Type.NAMESPACE, namespaces);
    }

    public void addFiles(List<Element> files) {
        addElements(Type.FILE, files);
    }

    public VariableElement getVariable(ElementIdentifier parent) {
        return getElement(parent, VariableElement.class);
    }

    public Element getFunction(ElementIdentifier parent) {
        return getElement(parent, Element.class);
    }

    public ClassElement getClass(ElementIdentifier parent) {
        return getElement(parent, ClassElement.class);
    }

    public Element getNamespace(ElementIdentifier parent) {
        return getElement(parent, Element.class);
    }

    public Element getFile(ElementIdentifier parent) {
        return getElement(parent, Element.class);
    }

    public List<VariableElement> getVariables() {
        return getElements(Type.VARIABLE, VariableElement.class);
    }

    public List<Element> getFunctions() {
        return getElements(Type.FUNCTION, Element.class);
    }

    public List<ClassElement> getClasses() {
        return getElements(Type.CLASS, ClassElement.class);
    }

    public List<Element> getNamespaces() {
        return getElements(Type.NAMESPACE, Element.class);
    }

    public List<Element> getFiles() {
        return getElements(Type.FILE, Element.class);
    }

    public boolean isNamespaceElement(Element element) {
        return containsElement(Type.NAMESPACE, element);
    }

    public boolean isVariableElement(Element element) {
        return containsElement(Type.VARIABLE, element);
    }

    public boolean isClassElement(Element element) {
        return containsElement(Type.CLASS, element);
    }

    public boolean isFunctionElement(Element element) {
        return containsElement(Type.FUNCTION, element);
    }

    public boolean isFileElement(Element element) {
        return containsElement(Type.FILE, element);
    }

    public List<VariableElement> getVariablesWithParent(ElementIdentifier parent) {
        return getContentOfParent(Type.VARIABLE, parent);
    }

    public List<Element> getFunctionsWithParent(ElementIdentifier parent) {
        return getContentOfParent(Type.FUNCTION, parent);
    }

    public List<ClassElement> getClassesWithParent(ElementIdentifier parent) {
        return getContentOfParent(Type.CLASS, parent);
    }

    public List<Element> getNamespacesWithParent(ElementIdentifier parent) {
        return getContentOfParent(Type.NAMESPACE, parent);
    }
}
