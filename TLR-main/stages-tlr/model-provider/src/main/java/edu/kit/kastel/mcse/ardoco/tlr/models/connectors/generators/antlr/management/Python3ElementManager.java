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
    private final List<VariableElement> variables;
    private final List<Element> functions;
    private final List<ClassElement> classes;
    private final List<Element> modules;
    private final List<PackageElement> packages;

    public Python3ElementManager() {
        this.variables = new ArrayList<>();
        this.functions = new ArrayList<>();
        this.classes = new ArrayList<>();
        this.modules = new ArrayList<>();
        this.packages = new ArrayList<>();
    }

    public Python3ElementManager(List<VariableElement> variables, List<Element> functions, List<ClassElement> classes,
            List<Element> modules, List<PackageElement> packages) {
        this.variables = variables;
        this.functions = functions;
        this.classes = classes;
        this.modules = modules;
        this.packages = packages;
    }

    @Override
    public List<Element> getContentOfParent(Parent parent) {
        return getBasicElementsWithParent(getAllElements(), parent);
    }

    public void addVariable(VariableElement variable) {
        if (variable != null && !this.variables.contains(variable)) {
            this.variables.add(variable);
        }
    }

    public void addVariables(List<VariableElement> variables) {
        for (VariableElement variable : variables) {
            addVariable(variable);
        }
    }

    public void addFunction(Element function) {
        if (function != null && !this.functions.contains(function)) {
            this.functions.add(function);
        }
    }

    public void addClass(ClassElement clazz) {
        if (clazz != null && !this.classes.contains(clazz)) {
            this.classes.add(clazz);
        }
    }

    public void addModule(Element module) {
        if (module != null && !this.modules.contains(module)) {
            this.modules.add(module);
        }
    }

    public void addPackage(PackageElement pack) {
        if (pack != null && !this.packages.contains(pack)) {
            this.packages.add(pack);
        }
    }

    public void addFunctions(List<Element> functions) {
        for (Element function : functions) {
            addFunction(function);
        }
    }

    public void addClasses(List<ClassElement> classes) {
        for (ClassElement clazz : classes) {
            addClass(clazz);
        }
    }

    public void addModules(List<Element> modules) {
        for (Element module : modules) {
            addModule(module);
        }
    }

    public PackageElement getPackage(Parent parent) {
        for (PackageElement pack : packages) {
            if (elementIsParent(pack, parent)) {
                return pack;
            }
        }
        return null;
    }

    public Element getModule(Parent parent) {
        for (Element module : modules) {
            if (elementIsParent(module, parent)) {
                return module;
            }
        }
        return null;
    }

    public ClassElement getClass(Parent parent) {
        for (ClassElement clazz : classes) {
            if (elementIsParent(clazz, parent)) {
                return clazz;
            }
        }
        return null;
    }

    public Element getFunction(Parent parent) {
        for (Element control : functions) {
            if (elementIsParent(control, parent)) {
                return control;
            }
        }
        return null;
    }

    public VariableElement getVariable(Parent parent) {
        for (VariableElement variable : variables) {
            if (elementIsParent(variable, parent)) {
                return variable;
            }
        }
        return null;
    }

    public List<VariableElement> getVariables() {
        return variables;
    }

    public List<Element> getFunctions() {
        return functions;
    }

    public List<ClassElement> getClasses() {
        return classes;
    }

    public List<Element> getModules() {
        return modules;
    }

    public List<PackageElement> getPackages() {
        return packages;
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
        List<VariableElement> variablesWithMatchingParent = new ArrayList<>();

        for (VariableElement variable : variables) {
            if (elementParentMatchesParent(variable, parent)) {
                variablesWithMatchingParent.add(variable);
            }
        }
        return variablesWithMatchingParent;
    }

    public List<Element> getControlsWithParent(Parent parent) {
        return getBasicElementsWithParent(functions, parent);
    }

    public List<ClassElement> getClassesWithParent(Parent parent) {
        List<ClassElement> classesWithMatchingParent = new ArrayList<>();

        for (ClassElement clazz : classes) {
            if (elementParentMatchesParent(clazz, parent)) {
                classesWithMatchingParent.add(clazz);
            }
        }
        return classesWithMatchingParent;
    }

    public List<Element> getModulesWithParent(Parent parent) {
        List<Element> modulesWithMatchingParent = new ArrayList<>();

        for (Element module : modules) {
            if (elementParentMatchesParent(module, parent)) {
                modulesWithMatchingParent.add(module);
            }
        }
        return modulesWithMatchingParent;
    }

    public List<PackageElement> getPackagesWithParent(Parent parent) {
        List<PackageElement> packagesWithMatchingParent = new ArrayList<>();

        for (PackageElement pack : packages) {
            if (elementParentMatchesParent(pack, parent)) {
                packagesWithMatchingParent.add(pack);
            }
        }
        return packagesWithMatchingParent;
    }

    @Override
    protected List<Element> getAllElements() {
        List<Element> elements = new ArrayList<>();
        elements.addAll(variables);
        elements.addAll(functions);
        elements.addAll(classes);
        elements.addAll(modules);
        elements.addAll(packages);
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
