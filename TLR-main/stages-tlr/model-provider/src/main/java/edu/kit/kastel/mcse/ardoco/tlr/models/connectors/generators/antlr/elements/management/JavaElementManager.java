package edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.management;

import java.util.ArrayList;
import java.util.List;

import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.Element;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.PackageElement;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.Parent;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.Type;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.VariableElement;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.java.JavaClassElement;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.mapping.java.JavaCommentMatcher;

public class JavaElementManager extends ElementManager {
    private final List<VariableElement> variables;
    private final List<Element> functions;
    private final List<JavaClassElement> classes;
    private final List<Element> interfaces;
    private final List<Element> compilationUnits;
    private final List<PackageElement> packages;

    public JavaElementManager() {
        this.variables = new ArrayList<>();
        this.functions = new ArrayList<>();
        this.classes = new ArrayList<>();
        this.interfaces = new ArrayList<>();
        this.compilationUnits = new ArrayList<>();
        this.packages = new ArrayList<>();
    }

    public JavaElementManager(List<VariableElement> variables, List<Element> functions, List<JavaClassElement> classes,
            List<Element> interfaces, List<Element> compilationUnits, List<PackageElement> packages) {
        this.variables = variables;
        this.functions = functions;
        this.classes = classes;
        this.interfaces = interfaces;
        this.compilationUnits = compilationUnits;
        this.packages = packages;
    }

    @Override
    public List<Element> getElementsWithParent(Parent parent) {
        return getBasicElementsWithParent(getAllElements(), parent);
    }

    public void addVariable(VariableElement variable) {
        if (variable != null && !this.variables.contains(variable)) {
            this.variables.add(variable);
        }
    }

    public void addFunction(Element function) {
        if (function != null && !this.functions.contains(function)) {
            this.functions.add(function);
        }
    }

    public void addClass(JavaClassElement clazz) {
        if (clazz != null && !this.classes.contains(clazz)) {
            this.classes.add(clazz);
        }
    }

    public void addInterface(Element interf) {
        if (interf != null && !this.interfaces.contains(interf)) {
            this.interfaces.add(interf);
        }
    }

    public void addCompilationUnit(Element compilationUnit) {
        if (compilationUnit != null && !this.compilationUnits.contains(compilationUnit)) {
            this.compilationUnits.add(compilationUnit);
        }
    }

    public void addPackage(PackageElement pack) {
        if (pack != null && !packages.contains(pack)) {
            packages.add(pack);
        }
    }

    public void addVariables(List<VariableElement> variables) {
        for (VariableElement variable : variables) {
            addVariable(variable);
        }
    }

    public void addFunctions(List<Element> functions) {
        for (Element function : functions) {
            addFunction(function);
        }
    }

    public void addClasses(List<JavaClassElement> classes) {
        for (JavaClassElement clazz : classes) {
            addClass(clazz);
        }
    }

    public void addInterfaces(List<Element> interfaces) {
        for (Element interf : interfaces) {
            addInterface(interf);
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

    public Element getCompilationUnitElement(Parent parent) {
        for (Element compilationUnit : compilationUnits) {
            if (elementIsParent(compilationUnit, parent)) {
                return compilationUnit;
            }
        }
        return null;
    }

    public JavaClassElement getClass(Parent parent) {
        for (JavaClassElement clazz : classes) {
            if (elementIsParent(clazz, parent)) {
                return clazz;
            }
        }
        return null;
    }

    public Element getInterface(Parent parent) {
        for (Element interf : interfaces) {
            if (elementIsParent(interf, parent)) {
                return interf;
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

    public List<Element> getFunctions() {
        return functions;
    }

    public List<VariableElement> getVariables() {
        return variables;
    }

    public List<JavaClassElement> getClasses() {
        return classes;
    }

    public List<Element> getInterfaces() {
        return interfaces;
    }

    public List<Element> getCompilationUnits() {
        return compilationUnits;
    }

    public List<PackageElement> getPackages() {
        return packages;
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
        List<VariableElement> variablesWithMatchingParent = new ArrayList<>();

        for (VariableElement variable : variables) {
            if (elementParentMatchesParent(variable, parent)) {
                variablesWithMatchingParent.add(variable);
            }
        }
        return variablesWithMatchingParent;
    }

    public List<Element> getFunctionsWithParent(Parent parent) {
        return getBasicElementsWithParent(functions, parent);
    }

    public List<JavaClassElement> getClassesWithParent(Parent parent) {
        List<JavaClassElement> classesWithMatchingParent = new ArrayList<>();

        for (JavaClassElement clazz : classes) {
            if (elementParentMatchesParent(clazz, parent)) {
                classesWithMatchingParent.add(clazz);
            }
        }
        return classesWithMatchingParent;
    }

    public List<Element> getInterfacesWithParent(Parent parent) {
        List<Element> interfacesWithMatchingParent = new ArrayList<>();
        for (Element interf : interfaces) {
            if (elementParentMatchesParent(interf, parent)) {
                interfacesWithMatchingParent.add(interf);
            }
        }
        return interfacesWithMatchingParent;
    }

    public List<Element> getCompilationUnitsWithParent(Parent parent) {
        List<Element> compilationUnitsWithMatchingParent = new ArrayList<>();

        for (Element compilationUnit : compilationUnits) {
            if (elementParentMatchesParent(compilationUnit, parent)) {
                compilationUnitsWithMatchingParent.add(compilationUnit);
            }
        }
        return compilationUnitsWithMatchingParent;
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
        elements.addAll(interfaces);
        elements.addAll(compilationUnits);
        elements.addAll(packages);
        return elements;
    }

    @Override
    protected JavaCommentMatcher buildCommentMatcher() {
        return new JavaCommentMatcher();
    }

    @Override
    protected Element getElement(Parent parent) {
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
