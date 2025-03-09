package edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.management;

import java.util.ArrayList;
import java.util.List;

import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.Element;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.commentmatching.Python3CommentMatcher;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.ClassElement;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.PackageElement;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.Parent;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.Type;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.VariableElement;

public class Python3ElementManager extends ElementManager {
    private final ElementStorage<VariableElement> variables;
    private final ElementStorage<Element> functions;
    private final ElementStorage<ClassElement> classes;
    private final ElementStorage<Element> modules;
    private final ElementStorage<PackageElement> packages;

    public Python3ElementManager() {
        this.variables = new ElementStorage<>();
        this.functions = new ElementStorage<>();
        this.classes = new ElementStorage<>();
        this.modules = new ElementStorage<>();
        this.packages = new ElementStorage<>();
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

    @Override
    public List<Element> getContentOfParent(Parent parent) {
        List<Element> elements = new ArrayList<>();
        elements.addAll(getVariablesWithParent(parent));
        elements.addAll(getFunctionsWithParent(parent));
        elements.addAll(getClassesWithParent(parent));
        elements.addAll(getModulesWithParent(parent));
        elements.addAll(getPackagesWithParent(parent));
        return elements;
    }

    public void addVariable(VariableElement variable) {
        this.variables.addElement(variable);
    }

    public void addVariables(List<VariableElement> variables) {
        this.variables.addElements(variables);
    }

    public void addFunction(Element function) {
        this.functions.addElement(function);
    }

    public void addClass(ClassElement clazz) {
        this.classes.addElement(clazz);
    }

    public void addModule(Element module) {
        this.modules.addElement(module);
    }

    public void addPackage(PackageElement pack) {
        this.packages.addElement(pack);
    }

    public void addFunctions(List<Element> functions) {
        this.functions.addElements(functions);
    }

    public void addClasses(List<ClassElement> classes) {
        this.classes.addElements(classes);
    }

    public void addModules(List<Element> modules) {
        this.modules.addElements(modules);
    }

    public void addPackages(List<PackageElement> packages) {
        this.packages.addElements(packages);
    }

    public PackageElement getPackage(Parent parent) {
        return this.packages.getElement(parent);
    }

    public Element getModule(Parent parent) {
        return this.modules.getElement(parent);
    }

    public ClassElement getClass(Parent parent) {
        return this.classes.getElement(parent);
    }

    public Element getFunction(Parent parent) {
        return this.functions.getElement(parent);
    }

    public VariableElement getVariable(Parent parent) {
        return this.variables.getElement(parent);
    }

    public List<VariableElement> getVariables() {
        return variables.getElements();
    }

    public List<Element> getFunctions() {
        return functions.getElements();
    }

    public List<ClassElement> getClasses() {
        return classes.getElements();
    }

    public List<Element> getModules() {
        return modules.getElements();
    }

    public List<PackageElement> getPackages() {
        return packages.getElements();
    }

    public boolean isModuleElement(Element element) {
        return modules.contains(element);
    }

    public boolean isPackageElement(Element element) {
        return packages.contains(element);
    }

    public boolean isClassElement(Element element) {
        return classes.contains(element);
    }

    public boolean isFunctionElement(Element element) {
        return functions.contains(element);
    }

    public boolean isVariableElement(Element element) {
        return variables.contains(element);
    }

    public List<VariableElement> getVariablesWithParent(Parent parent) {
        return this.variables.getContentOfParent(parent);
    }

    public List<Element> getFunctionsWithParent(Parent parent) {
        return this.functions.getContentOfParent(parent);
    }

    public List<ClassElement> getClassesWithParent(Parent parent) {
        return this.classes.getContentOfParent(parent);
    }

    public List<Element> getModulesWithParent(Parent parent) {
        return this.modules.getContentOfParent(parent);
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
        elements.addAll(modules.getElements());
        elements.addAll(packages.getElements());
        return elements;
    }

    @Override
    protected Python3CommentMatcher buildCommentMatcher() {
        return new Python3CommentMatcher();
    }

    @Override
    public Element getElement(Parent parent) {
        if (parent.getType() == Type.VARIABLE) {
            return getVariable(parent);
        } else if (parent.getType() == Type.FUNCTION) {
            return getFunction(parent);
        } else if (parent.getType() == Type.CLASS) {
            return getClass(parent);
        } else if (parent.getType() == Type.MODULE) {
            return getModule(parent);
        } else if (parent.getType() == Type.PACKAGE) {
            return getPackage(parent);
        } else {
            return null;
        }
    }

}
