package edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.python3;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.BasicElement;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.ClassElement;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.ElementManager;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.PackageElement;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.Parent;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.mapping.python3.Python3CommentMapper;

public class Python3ElementManager extends ElementManager {
    private final List<Python3VariableElement> variables;
    private final List<BasicElement> functions;
    private final List<ClassElement> classes;
    private final List<Python3ModuleElement> modules;
    private final List<PackageElement> packages;


    public Python3ElementManager() {
        this.variables = new ArrayList<>();
        this.functions = new ArrayList<>();
        this.classes = new ArrayList<>();
        this.modules = new ArrayList<>();
        this.packages = new ArrayList<>();
    }

    @Override
    public List<BasicElement> getElementsWithParent(Parent parent) {
        return getBasicElementsWithParent(getAllElements(), parent);
    }

    @Override
    protected boolean isRootParent(Parent parent) {
        PackageElement pack = getPackage(parent);
        return pack != null && pack.getParent() == null;
    }

    public void addVariables(List<Python3VariableElement> variables) {
        for (Python3VariableElement variable : variables) {
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

    public void addModules(List<Python3ModuleElement> modules) {
        for (Python3ModuleElement module : modules) {
            if (!this.modules.contains(module)) {
                this.modules.add(module);
            }
        }
    }

    public void addPackage(PackageElement pack) {
        if (!packages.contains(pack)) {
            packages.add(pack);

        }
        packages.sort(Comparator.comparingInt(p -> p.getPackageNameParts("/").length));
    }



    public PackageElement getPackage(Parent parent) {
        for (PackageElement pack : packages) {
            if (pack.getName().equals(parent.getName()) && pack.getPath().equals(parent.getPath())) {
                return pack;
            }
        }
        return null;
    }

    public Python3ModuleElement getModule(Parent parent) {
        for (Python3ModuleElement module : modules) {
            if (module.getName().equals(parent.getName()) && module.getPath().equals(parent.getPath())) {
                return module;
            }
        }
        return null;
    }

    public ClassElement getClass(Parent parent) {
        for (ClassElement clazz : classes) {
            if (clazz.getName().equals(parent.getName()) && clazz.getPath().equals(parent.getPath())) {
                return clazz;
            }
        }
        return null;
    }

    public BasicElement getControl(Parent parent) {
        for (BasicElement control : functions) {
            if (control.getName().equals(parent.getName()) && control.getPath().equals(parent.getPath())) {
                return control;
            }
        }
        return null;
    }

    public Python3VariableElement getVariable(Parent parent) {
        for (Python3VariableElement variable : variables) {
            if (variable.getName().equals(parent.getName()) && variable.getPath().equals(parent.getPath())) {
                return variable;
            }
        }
        return null;
    }

    public List<Python3VariableElement> getVariables() {
        return variables;
    }

    public List<BasicElement> getFunctions() {
        return functions;
    }

    public List<ClassElement> getClasses() {
        return classes;
    }

    public List<Python3ModuleElement> getModules() {
        return modules;
    }

    public List<PackageElement> getPackages() {
        return packages;
    }

    public boolean isModuleElement(BasicElement element) {
        return modules.contains(element);
    }

    public boolean isPackageElement(BasicElement element) {
        return packages.contains(element);
    }

    public boolean isClassElement(BasicElement element) {
        return classes.contains(element);
    }

    public boolean isControlElement(BasicElement element) {
        return functions.contains(element);
    }

    public boolean isVariableElement(BasicElement element) {
        return variables.contains(element);
    }

    public List<Python3VariableElement> getVariablesWithParent(Parent parent) {
        List<Python3VariableElement> variablesWithMatchingParent = new ArrayList<>();
        for (Python3VariableElement variable : variables) {
            if (variable.getParent().equals(parent)) {
                variablesWithMatchingParent.add(variable);
            }
        }
        return variablesWithMatchingParent;
    }

    public List<BasicElement> getControlsWithParent(Parent parent) {
        return getBasicElementsWithParent(functions, parent);
    }

    public List<ClassElement> getClassesWithParent(Parent parent) {
        List<ClassElement> classesWithMatchingParent = new ArrayList<>();
        for (ClassElement clazz : classes) {
            if (clazz.getParent().equals(parent)) {
                classesWithMatchingParent.add(clazz);
            }
        }
        return classesWithMatchingParent;
    }

    public List<Python3ModuleElement> getModulesWithParent(Parent parent) {
        List<Python3ModuleElement> modulesWithMatchingParent = new ArrayList<>();
        for (Python3ModuleElement module : modules) {
            if (module.getParent().equals(parent)) {
                modulesWithMatchingParent.add(module);
            }
        }
        return modulesWithMatchingParent;
    }

    public List<PackageElement> getPackagesWithParent(Parent parent) {
        List<PackageElement> packagesWithMatchingParent = new ArrayList<>();
        for (PackageElement pack : packages) {
            if (pack.getParent().equals(parent)) {
                packagesWithMatchingParent.add(pack);
            }
        }
        return packagesWithMatchingParent;
    }

    @Override
    protected List<BasicElement> getAllElements() {
        List<BasicElement> elements = new ArrayList<>();
        elements.addAll(variables);
        elements.addAll(functions);
        elements.addAll(classes);
        elements.addAll(modules);
        elements.addAll(packages);
        return elements;
    }

    @Override
    protected Python3CommentMapper buildCommentMatcher() {
        return new Python3CommentMapper();
    }

}
