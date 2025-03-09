package edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.management.java;

import java.util.List;

import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.commentmatching.JavaCommentMatcher;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.Element;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.PackageElement;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.Parent;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.Type;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.VariableElement;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.java.JavaClassElement;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.management.ElementManager;

public class JavaElementManager extends ElementManager {

    public JavaElementManager() {
        super(new JavaElementStorageRegistry(), new JavaCommentMatcher());
    }


    public JavaElementManager(List<VariableElement> variables, List<Element> functions, List<JavaClassElement> classes,
            List<Element> interfaces, List<Element> compilationUnits, List<PackageElement> packages) {
        this();
        addElements(Type.VARIABLE, variables);
        addElements(Type.FUNCTION, functions);
        addElements(Type.CLASS, classes);
        addElements(Type.INTERFACE, interfaces);
        addElements(Type.COMPILATIONUNIT, compilationUnits);
        addElements(Type.PACKAGE, packages);
    }

    public void addVariable(VariableElement variable) {
        elementStorageRegistry.addElement(Type.VARIABLE, variable);
    }

    public void addFunction(Element function) {
        elementStorageRegistry.addElement(Type.FUNCTION, function);
    }

    public void addClass(JavaClassElement clazz) {
        elementStorageRegistry.addElement(Type.CLASS, clazz);
    }

    public void addInterface(Element interfaceElement) {
        elementStorageRegistry.addElement(Type.INTERFACE, interfaceElement);
    }

    public void addCompilationUnit(Element compilationUnit) {
        elementStorageRegistry.addElement(Type.COMPILATIONUNIT, compilationUnit);
    }

    public void addPackage(PackageElement packageElement) {
        elementStorageRegistry.addElement(Type.PACKAGE, packageElement);
    }

    public void addVariables(List<VariableElement> variables) {
        elementStorageRegistry.addElements(Type.VARIABLE, variables);
    }

    public void addFunctions(List<Element> functions) {
        elementStorageRegistry.addElements(Type.FUNCTION, functions);
    }

    public void addClasses(List<JavaClassElement> classes) {
        elementStorageRegistry.addElements(Type.CLASS, classes);
    }

    public void addInterfaces(List<Element> interfaces) {
        elementStorageRegistry.addElements(Type.INTERFACE, interfaces);
    }

    public void addCompilationUnits(List<Element> compilationUnits) {
        elementStorageRegistry.addElements(Type.COMPILATIONUNIT, compilationUnits);
    }

    public void addPackages(List<PackageElement> packages) {
        elementStorageRegistry.addElements(Type.PACKAGE, packages);
    }

    public VariableElement getVariable(Parent parent) {
        return elementStorageRegistry.getElement(parent, VariableElement.class);
    }

    public Element getFunction(Parent parent) {
        return elementStorageRegistry.getElement(parent, Element.class);
    }

    public JavaClassElement getClass(Parent parent) {
        return elementStorageRegistry.getElement(parent, JavaClassElement.class);
    }

    public Element getInterface(Parent parent) {
        return elementStorageRegistry.getElement(parent, Element.class);
    }

    public Element getCompilationUnitElement(Parent parent) {
        return elementStorageRegistry.getElement(parent, Element.class);
    }

    public PackageElement getPackage(Parent parent) {
        return elementStorageRegistry.getElement(parent, PackageElement.class);
    }

    public List<VariableElement> getVariables() {
        return elementStorageRegistry.getElements(Type.VARIABLE, VariableElement.class);
    }

    public List<Element> getFunctions() {
        return elementStorageRegistry.getElements(Type.FUNCTION, Element.class);
    }

    public List<JavaClassElement> getClasses() {
        return elementStorageRegistry.getElements(Type.CLASS, JavaClassElement.class);
    }

    public List<Element> getInterfaces() {
        return elementStorageRegistry.getElements(Type.INTERFACE, Element.class);
    }

    public List<Element> getCompilationUnits() {
        return elementStorageRegistry.getElements(Type.COMPILATIONUNIT, Element.class);
    }

    public List<PackageElement> getPackages() {
        return elementStorageRegistry.getElements(Type.PACKAGE, PackageElement.class);
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

    public boolean isInterfaceElement(Element element) {
        return isElement(Type.INTERFACE, element);
    }

    public boolean isCompilationUnitElement(Element element) {
        return isElement(Type.COMPILATIONUNIT, element);
    }

    public boolean isPackageElement(Element element) {
        return isElement(Type.PACKAGE, element);
    }

    public List<VariableElement> getVariablesWithParent(Parent parent) {
        return elementStorageRegistry.getContentOfParent(Type.VARIABLE, parent);
    }

    public List<Element> getFunctionsWithParent(Parent parent) {
        return elementStorageRegistry.getContentOfParent(Type.FUNCTION, parent);
    }

    public List<JavaClassElement> getClassesWithParent(Parent parent) {
        return elementStorageRegistry.getContentOfParent(Type.CLASS, parent);
    }

    public List<Element> getInterfacesWithParent(Parent parent) {
        return elementStorageRegistry.getContentOfParent(Type.INTERFACE, parent);
    }

    public List<Element> getCompilationUnitsWithParent(Parent parent) {
        return elementStorageRegistry.getContentOfParent(Type.COMPILATIONUNIT, parent);
    }

    public List<PackageElement> getPackagesWithParent(Parent parent) {
        return elementStorageRegistry.getContentOfParent(Type.PACKAGE, parent);
    }
}
