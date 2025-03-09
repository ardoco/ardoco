package edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.management.python3;

import java.util.List;

import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.Element;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.commentmatching.Python3CommentMatcher;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.ClassElement;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.PackageElement;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.Parent;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.Type;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.VariableElement;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.management.ElementManager;

public class Python3ElementManager extends ElementManager {

    public Python3ElementManager() {
        super(new Python3ElementStorageRegistry(), new Python3CommentMatcher());
    }

    public Python3ElementManager(List<VariableElement> variables, List<Element> functions, List<ClassElement> classes,
            List<Element> modules, List<PackageElement> packages) {
        this();
        addVariables(variables);
        addFunctions(functions);
        addClasses(classes);
        addModules(modules);
        addPackages(packages);
    }

    public void addVariable(VariableElement variable) {
        this.elementStorageRegistry.addElement(Type.VARIABLE, variable);
    }

    public void addFunction(Element function) {
        this.elementStorageRegistry.addElement(Type.FUNCTION, function);
    }

    public void addClass(ClassElement clazz) {
        this.elementStorageRegistry.addElement(Type.CLASS, clazz);
    }

    public void addModule(Element module) {
        this.elementStorageRegistry.addElement(Type.MODULE, module);
    }

    public void addPackage(PackageElement pack) {
        this.elementStorageRegistry.addElement(Type.PACKAGE, pack);
    }

    public void addVariables(List<VariableElement> variables) {
        this.elementStorageRegistry.addElements(Type.VARIABLE, variables);
    }

    public void addFunctions(List<Element> functions) {
        this.elementStorageRegistry.addElements(Type.FUNCTION, functions);
    }

    public void addClasses(List<ClassElement> classes) {
        this.elementStorageRegistry.addElements(Type.CLASS, classes);
    }

    public void addModules(List<Element> modules) {
        this.elementStorageRegistry.addElements(Type.MODULE, modules);
    }

    public void addPackages(List<PackageElement> packages) {
        this.elementStorageRegistry.addElements(Type.PACKAGE, packages);
    }

    public VariableElement getVariable(Parent parent) {
        return this.elementStorageRegistry.getElement(parent, VariableElement.class);
    }

    public Element getFunction(Parent parent) {
        return this.elementStorageRegistry.getElement(parent, Element.class);
    }

    public ClassElement getClass(Parent parent) {
        return this.elementStorageRegistry.getElement(parent, ClassElement.class);
    }

    public Element getModule(Parent parent) {
        return this.elementStorageRegistry.getElement(parent, Element.class);
    }

    public PackageElement getPackage(Parent parent) {
        return this.elementStorageRegistry.getElement(parent, PackageElement.class);
    }

    public List<VariableElement> getVariables() {
        return this.elementStorageRegistry.getElements(Type.VARIABLE, VariableElement.class);
    }

    public List<Element> getFunctions() {
        return this.elementStorageRegistry.getElements(Type.FUNCTION, Element.class);
    }

    public List<ClassElement> getClasses() {
        return this.elementStorageRegistry.getElements(Type.CLASS, ClassElement.class);
    }

    public List<Element> getModules() {
        return this.elementStorageRegistry.getElements(Type.MODULE, Element.class);
    }

    public List<PackageElement> getPackages() {
        return this.elementStorageRegistry.getElements(Type.PACKAGE, PackageElement.class);
    }

    public boolean isVariableElement(Element element) {
        return isElement(Type.VARIABLE, element);
    }

    public boolean isFunctionElement(Element element) {
        return isElement(Type.FUNCTION, element);
    }

    public boolean isClassElement(Element element) {
        return isElement(Type.CLASS, element);
    }

    public boolean isModuleElement(Element element) {
        return isElement(Type.MODULE, element);
    }

    public boolean isPackageElement(Element element) {
        return isElement(Type.PACKAGE, element);
    }

    public List<VariableElement> getVariablesWithParent(Parent parent) {
        return this.elementStorageRegistry.getContentOfParent(Type.VARIABLE, parent);
    }

    public List<Element> getFunctionsWithParent(Parent parent) {
        return this.elementStorageRegistry.getContentOfParent(Type.FUNCTION, parent);
    }

    public List<ClassElement> getClassesWithParent(Parent parent) {
        return this.elementStorageRegistry.getContentOfParent(Type.CLASS, parent);
    }

    public List<Element> getModulesWithParent(Parent parent) {
        return this.elementStorageRegistry.getContentOfParent(Type.MODULE, parent);
    }

    public List<PackageElement> getPackagesWithParent(Parent parent) {
        return this.elementStorageRegistry.getContentOfParent(Type.PACKAGE, parent);
    }

}
