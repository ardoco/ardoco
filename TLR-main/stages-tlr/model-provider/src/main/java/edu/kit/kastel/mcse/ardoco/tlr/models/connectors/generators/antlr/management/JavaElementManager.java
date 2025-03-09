package edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.management;

import java.util.ArrayList;
import java.util.List;

import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.commentmatching.JavaCommentMatcher;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.Element;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.PackageElement;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.Parent;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.Type;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.VariableElement;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.java.JavaClassElement;

public class JavaElementManager extends ElementManager {
    private final ElementStorage<VariableElement> variables;
    private final ElementStorage<Element> functions;
    private final ElementStorage<JavaClassElement> classes;
    private final ElementStorage<Element> interfaces;
    private final ElementStorage<Element> compilationUnits;
    private final ElementStorage<PackageElement> packages;

    public JavaElementManager() {
        this.variables = new ElementStorage<>();
        this.functions = new ElementStorage<>();
        this.classes = new ElementStorage<>();
        this.interfaces = new ElementStorage<>();
        this.compilationUnits = new ElementStorage<>();
        this.packages = new ElementStorage<>();
    }

    public JavaElementManager(List<VariableElement> variables, List<Element> functions, List<JavaClassElement> classes,
            List<Element> interfaces, List<Element> compilationUnits, List<PackageElement> packages) {
        this();
        addVariables(variables);
        addFunctions(functions);
        addClasses(classes);
        addInterfaces(interfaces);
        addCompilationUnits(compilationUnits);
        addPackages(packages);
    }

    @Override
    public List<Element> getContentOfParent(Parent parent) {
        List<Element> elements = new ArrayList<>();
        elements.addAll(getVariablesWithParent(parent));
        elements.addAll(getFunctionsWithParent(parent));
        elements.addAll(getClassesWithParent(parent));
        elements.addAll(getInterfacesWithParent(parent));
        elements.addAll(getCompilationUnitsWithParent(parent));
        elements.addAll(getPackagesWithParent(parent));
        return elements;
    }

    public void addVariable(VariableElement variable) {
        variables.addElement(variable);
    }

    public void addFunction(Element function) {
        functions.addElement(function);
    }

    public void addClass(JavaClassElement clazz) {
        classes.addElement(clazz);
    }

    public void addInterface(Element interf) {
        interfaces.addElement(interf);
    }

    public void addCompilationUnit(Element compilationUnit) {
        compilationUnits.addElement(compilationUnit);
    }

    public void addPackage(PackageElement pack) {
        packages.addElement(pack);
    }

    public void addVariables(List<VariableElement> variables) {
        this.variables.addElements(variables);
    }

    public void addFunctions(List<Element> functions) {
        this.functions.addElements(functions);
    }

    public void addClasses(List<JavaClassElement> classes) {
        this.classes.addElements(classes);
    }

    public void addInterfaces(List<Element> interfaces) {
        this.interfaces.addElements(interfaces);
    }

    public void addCompilationUnits(List<Element> compilationUnits) {
        this.compilationUnits.addElements(compilationUnits);
    }

    public void addPackages(List<PackageElement> packages) {
        this.packages.addElements(packages);
    }

    public PackageElement getPackage(Parent parent) {
        return this.packages.getElement(parent);
    }

    public Element getCompilationUnitElement(Parent parent) {
        return this.compilationUnits.getElement(parent);
    }

    public JavaClassElement getClass(Parent parent) {
        return this.classes.getElement(parent);
    }

    public Element getInterface(Parent parent) {
        return this.interfaces.getElement(parent);
    }

    public Element getFunction(Parent parent) {
        return this.functions.getElement(parent);
    }

    public VariableElement getVariable(Parent parent) {
        return this.variables.getElement(parent);
    }

    public List<Element> getFunctions() {
        return this.functions.getElements();
    }

    public List<VariableElement> getVariables() {
        return this.variables.getElements();
    }

    public List<JavaClassElement> getClasses() {
        return this.classes.getElements();
    }

    public List<Element> getInterfaces() {
        return this.interfaces.getElements();
    }

    public List<Element> getCompilationUnits() {
        return this.compilationUnits.getElements();
    }

    public List<PackageElement> getPackages() {
        return this.packages.getElements();
    }

    public boolean isPackageElement(Element element) {
        return packages.contains(element);
    }

    public boolean isCompilationUnitElement(Element element) {
        return compilationUnits.contains(element);
    }

    public boolean isClassElement(Element element) {
        return classes.contains(element);
    }

    public boolean isElement(Element element) {
        return interfaces.contains(element);
    }

    public boolean isFunctionElement(Element element) {
        return functions.contains(element);
    }

    public boolean isVariableElement(Element element) {
        return variables.contains(element);
    }

    public boolean isInterfaceElement(Element element) {
        return interfaces.contains(element);
    }

    public List<VariableElement> getVariablesWithParent(Parent parent) {
        return this.variables.getContentOfParent(parent);
    }

    public List<Element> getFunctionsWithParent(Parent parent) {
        return this.functions.getContentOfParent(parent);
    }

    public List<JavaClassElement> getClassesWithParent(Parent parent) {
        return this.classes.getContentOfParent(parent);
    }

    public List<Element> getInterfacesWithParent(Parent parent) {
        return this.interfaces.getContentOfParent(parent);
    }

    public List<Element> getCompilationUnitsWithParent(Parent parent) {
        return this.compilationUnits.getContentOfParent(parent);
    }

    public List<PackageElement> getPackagesWithParent(Parent parent) {
        return this.packages.getContentOfParent(parent);
    }

    @Override
    protected List<Element> getAllElements() {
        List<Element> elements = new ArrayList<>();
        elements.addAll(variables.getElements());
        elements.addAll(functions.getElements());
        elements.addAll(classes.getElements());
        elements.addAll(interfaces.getElements());
        elements.addAll(compilationUnits.getElements());
        elements.addAll(packages.getElements());
        return elements;
    }

    @Override
    protected JavaCommentMatcher buildCommentMatcher() {
        return new JavaCommentMatcher();
    }

    @Override
    public Element getElement(Parent parent) {
        if (parent.getType() == Type.VARIABLE) {
            return getVariable(parent);
        } else if (parent.getType() == Type.FUNCTION) {
            return getFunction(parent);
        } else if (parent.getType() == Type.CLASS) {
            return getClass(parent);
        } else if (parent.getType() == Type.INTERFACE) {
            return getInterface(parent);
        } else if (parent.getType() == Type.COMPILATIONUNIT) {
            return getCompilationUnitElement(parent);
        } else if (parent.getType() == Type.PACKAGE) {
            return getPackage(parent);
        }
        return null;
    }
}
