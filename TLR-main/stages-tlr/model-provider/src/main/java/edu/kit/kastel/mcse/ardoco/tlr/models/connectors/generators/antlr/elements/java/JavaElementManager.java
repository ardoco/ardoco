package edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.java;

import java.util.ArrayList;
import java.util.List;

import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.BasicElement;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.BasicType;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.ElementManager;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.InterfaceElement;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.PackageElement;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.Parent;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.VariableElement;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.mapping.java.JavaCommentMatcher;

public class JavaElementManager extends ElementManager {
    private final List<VariableElement> variables;
    private final List<BasicElement> functions;
    private final List<JavaClassElement> classes;
    private final List<InterfaceElement> interfaces;
    private final List<BasicElement> compilationUnits;
    private final List<PackageElement> packages;

    public JavaElementManager() {
        this.variables = new ArrayList<>();
        this.functions = new ArrayList<>();
        this.classes = new ArrayList<>();
        this.interfaces = new ArrayList<>();
        this.compilationUnits = new ArrayList<>();
        this.packages = new ArrayList<>();
    }

    @Override
    public List<BasicElement> getElementsWithParent(Parent parent) {
        return getBasicElementsWithParent(getAllElements(), parent);
    }

    @Override
    public boolean isRootParent(Parent parent) {
        PackageElement pack = getPackage(parent);
        return pack != null && pack.getParent() == null;
    }

    public void addVariables(List<VariableElement> variables) {
        for (VariableElement variable : variables) {
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

    public void addClasses(List<JavaClassElement> classes) {
        for (JavaClassElement clazz : classes) {
            if (!this.classes.contains(clazz)) {
                this.classes.add(clazz);
            }
        }
    }

    public void addInterfaces(List<InterfaceElement> interfaces) {
        for (InterfaceElement interf : interfaces) {
            if (!this.interfaces.contains(interf)) {
                this.interfaces.add(interf);
            }
        }
    }

    public void addCompilationUnit(BasicElement compilationUnit) {
        if (!this.compilationUnits.contains(compilationUnit)) {
            this.compilationUnits.add(compilationUnit);
        }
    }

    public void addPackage(PackageElement pack) {
        if (!packages.contains(pack)) {
            packages.add(pack);
        }
        updatePackageParents();
    }

    public PackageElement getPackage(Parent parent) {
        for (PackageElement pack : packages) {
            if (pack.getName().equals(parent.getName()) && pack.getPath().equals(parent.getPath())) {
                return pack;
            }
        }
        return null;
    }

    public BasicElement getCompilationUnitElement(Parent parent) {
        for (BasicElement compilationUnit : compilationUnits) {
            if (compilationUnit.getName().equals(parent.getName()) && compilationUnit.getPath().equals(parent.getPath())) {
                return compilationUnit;
            }
        }
        return null;
    }

    public JavaClassElement getClass(Parent parent) {
        for (JavaClassElement clazz : classes) {
            if (clazz.getName().equals(parent.getName()) && clazz.getPath().equals(parent.getPath())) {
                return clazz;
            }
        }
        return null;
    }

    public InterfaceElement getInterface(Parent parent) {
        for (InterfaceElement interf : interfaces) {
            if (interf.getName().equals(parent.getName()) && interf.getPath().equals(parent.getPath())) {
                return interf;
            }
        }
        return null;
    }

    public BasicElement getFunction(Parent parent) {
        for (BasicElement control : functions) {
            if (control.getName().equals(parent.getName()) && control.getPath().equals(parent.getPath())) {
                return control;
            }
        }
        return null;
    }

    public VariableElement getVariable(Parent parent) {
        for (VariableElement variable : variables) {
            if (variable.getName().equals(parent.getName()) && variable.getPath().equals(parent.getPath())) {
                return variable;
            }
        }
        return null;
    }

    public List<BasicElement> getFunctions() {
        return functions;
    }

    public List<VariableElement> getVariables() {
        return variables;
    }

    public List<JavaClassElement> getClasses() {
        return classes;
    }

    public List<InterfaceElement> getInterfaces() {
        return interfaces;
    }

    public List<BasicElement> getCompilationUnits() {
        return compilationUnits;
    }

    public List<PackageElement> getPackages() {
        return packages;
    }

    public boolean isPackageElement(BasicElement element) {
        return packages.contains(element);
    }

    public boolean isCompilationUnitElement(BasicElement element) {
        return compilationUnits.contains(element);
    }
    
    public boolean isClassElement(BasicElement element) {
        return classes.contains(element);
    }

    public boolean isInterfaceElement(BasicElement element) {
        return interfaces.contains(element);
    }

    public boolean isFunctionElement(BasicElement element) {
        return functions.contains(element);
    }

    public boolean isVariableElement(BasicElement element) {
        return variables.contains(element);
    }

    public List<VariableElement> getVariablesWithParent(Parent parent) {
        List<VariableElement> variablesWithMatchingParent = new ArrayList<>();
        for (VariableElement variable : variables) {
            if (variable.getParent().equals(parent)) {
                variablesWithMatchingParent.add(variable);
            }
        }
        return variablesWithMatchingParent;
    }

    public List<BasicElement> getFunctionsWithParent(Parent parent) {
        return getBasicElementsWithParent(functions, parent);
    }

    public List<JavaClassElement> getClassesWithParent(Parent parent) {
        List<JavaClassElement> classesWithMatchingParent = new ArrayList<>();
        for (JavaClassElement clazz : classes) {
            if (clazz.getParent().equals(parent)) {
                classesWithMatchingParent.add(clazz);
            }
        }
        return classesWithMatchingParent;
    }

    public List<InterfaceElement> getInterfacesWithParent(Parent parent) {
        List<InterfaceElement> interfacesWithMatchingParent = new ArrayList<>();
        for (InterfaceElement interf : interfaces) {
            if (interf.getParent().equals(parent)) {
                interfacesWithMatchingParent.add(interf);
            }
        }
        return interfacesWithMatchingParent;
    }

    public List<BasicElement> getCompilationUnitsWithParent(Parent parent) {
        List<BasicElement> compilationUnitsWithMatchingParent = new ArrayList<>();
        for (BasicElement compilationUnit : compilationUnits) {
            if (compilationUnit.getParent().equals(parent)) {
                compilationUnitsWithMatchingParent.add(compilationUnit);
            }
        }
        return compilationUnitsWithMatchingParent;
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
        elements.addAll(interfaces);
        elements.addAll(compilationUnits);
        elements.addAll(packages);
        return elements;
    }

    @Override
    protected JavaCommentMatcher buildCommentMatcher() {
        return new JavaCommentMatcher();
    }

    private void updatePackageParents() {
        for (PackageElement pack : packages) {
            updatePackageParent(pack);
        }
    }

    private void updatePackageParent(PackageElement pack) {
        for (PackageElement otherPack : packages) {
            if (pack.extendsPackage(otherPack)) {
                Parent parent = new Parent(otherPack.getName(), otherPack.getPath(), BasicType.PACKAGE);
                pack.updateParent(parent);
            }
        }
    }
}
