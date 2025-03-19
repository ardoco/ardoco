package edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.management.python3;

import java.util.List;

import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.Element;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.ClassElement;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.PackageElement;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.ElementIdentifier;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.Type;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.VariableElement;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.management.ElementStorage;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.management.ElementStorageRegistry;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.management.commentmatching.Python3CommentMatcher;

/**
 * Registry for storing elements of a Python3 codebase.
 * Defines the set of element objects that can be stored and provides methods to
 * add and retrieve specific elements.
 */
public class Python3ElementStorageRegistry extends ElementStorageRegistry {

    public Python3ElementStorageRegistry() {
        super(new Python3CommentMatcher());
    }

    public Python3ElementStorageRegistry(List<VariableElement> variables, List<Element> functions,
            List<ClassElement> classes,
            List<Element> modules, List<PackageElement> packages) {
        this();
        addVariables(variables);
        addFunctions(functions);
        addClasses(classes);
        addModules(modules);
        addPackages(packages);
    }

    @Override
    protected void registerStorage() {
        registerStorage(Type.VARIABLE, new ElementStorage<VariableElement>(VariableElement.class));
        registerStorage(Type.FUNCTION, new ElementStorage<Element>(Element.class));
        registerStorage(Type.CLASS, new ElementStorage<ClassElement>(ClassElement.class));
        registerStorage(Type.MODULE, new ElementStorage<Element>(Element.class));
        registerStorage(Type.PACKAGE, new ElementStorage<PackageElement>(PackageElement.class));
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

    public void addModule(Element module) {
        addElement(Type.MODULE, module);
    }

    public void addPackage(PackageElement pack) {
        addElement(Type.PACKAGE, pack);
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

    public void addModules(List<Element> modules) {
        addElements(Type.MODULE, modules);
    }

    public void addPackages(List<PackageElement> packages) {
        addElements(Type.PACKAGE, packages);
    }

    public VariableElement getVariable(ElementIdentifier identifier) {
        return getElement(identifier, VariableElement.class);
    }

    public Element getFunction(ElementIdentifier identifier) {
        return getElement(identifier, Element.class);
    }

    public ClassElement getClass(ElementIdentifier identifier) {
        return getElement(identifier, ClassElement.class);
    }

    public Element getModule(ElementIdentifier identifier) {
        return getElement(identifier, Element.class);
    }

    public PackageElement getPackage(ElementIdentifier identifier) {
        return getElement(identifier, PackageElement.class);
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

    public List<Element> getModules() {
        return getElements(Type.MODULE, Element.class);
    }

    public List<PackageElement> getPackages() {
        return getElements(Type.PACKAGE, PackageElement.class);
    }

    public boolean isVariableElement(Element element) {
        return containsElement(Type.VARIABLE, element);
    }

    public boolean isFunctionElement(Element element) {
        return containsElement(Type.FUNCTION, element);
    }

    public boolean isClassElement(Element element) {
        return containsElement(Type.CLASS, element);
    }

    public boolean isModuleElement(Element element) {
        return containsElement(Type.MODULE, element);
    }

    public boolean isPackageElement(Element element) {
        return containsElement(Type.PACKAGE, element);
    }

    public List<VariableElement> getVariablesWithParentIdentifier(ElementIdentifier parentIdentifier) {
        return getContentOfIdentifier(Type.VARIABLE, parentIdentifier);
    }

    public List<Element> getFunctionsWithParentIdentifier(ElementIdentifier parentIdentifier) {
        return getContentOfIdentifier(Type.FUNCTION, parentIdentifier);
    }

    public List<ClassElement> getClassesWithParentIdentifier(ElementIdentifier parentIdentifier) {
        return getContentOfIdentifier(Type.CLASS, parentIdentifier);
    }

    public List<Element> getModulesWithParentIdentifier(ElementIdentifier parentIdentifier) {
        return getContentOfIdentifier(Type.MODULE, parentIdentifier);
    }

    public List<PackageElement> getPackagesWithParentIdentifier(ElementIdentifier parentIdentifier) {
        return getContentOfIdentifier(Type.PACKAGE, parentIdentifier);
    }

}
