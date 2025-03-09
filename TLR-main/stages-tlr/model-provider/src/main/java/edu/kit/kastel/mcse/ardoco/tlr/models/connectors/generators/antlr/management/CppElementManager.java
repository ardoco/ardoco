package edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.management;

import java.util.ArrayList;
import java.util.List;

import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.Element;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.Type;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.commentmatching.CppCommentMatcher;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.ClassElement;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.Parent;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.VariableElement;

public class CppElementManager extends ElementManager {
    private List<VariableElement> variables;
    private List<Element> functions;
    private List<ClassElement> classes;
    private List<Element> namespaces;
    private List<Element> files;

    public CppElementManager() {
        this.variables = new ArrayList<>();
        this.functions = new ArrayList<>();
        this.classes = new ArrayList<>();
        this.namespaces = new ArrayList<>();
        this.files = new ArrayList<>();
    }

    public CppElementManager(List<VariableElement> variables, List<Element> functions, List<ClassElement> classes,
            List<Element> namespaces) {
        this.variables = variables;
        this.functions = functions;
        this.classes = classes;
        this.namespaces = namespaces;
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

    public void addNamespace(Element namespace) {
        if (namespace != null && !this.namespaces.contains(namespace)) {
            this.namespaces.add(namespace);
        }
    }

    public void addFile(Element file) {
        if (file != null && !this.files.contains(file)) {
            this.files.add(file);
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

    public void addClasses(List<ClassElement> classes) {
        for (ClassElement clazz : classes) {
            addClass(clazz);
        }
    }

    public void addNamespaces(List<Element> namespaces) {
        for (Element namespace : namespaces) {
            addNamespace(namespace);
        }
    }

    public void addFiles(List<Element> files) {
        for (Element file : files) {
            addFile(file);
        }
    }

    public Element getFile(Parent parent) {
        for (Element file : files) {
            if (elementIsParent(file, parent)) {
                return file;
            }
        }
        return null;
    }

    public Element getNamespace(Parent parent) {
        for (Element namespace : namespaces) {
            if (elementIsParent(namespace, parent)) {
                return namespace;
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

    public ClassElement getClass(Parent parent) {
        for (ClassElement clazz : classes) {
            if (elementIsParent(clazz, parent)) {
                return clazz;
            }
        }
        return null;
    }

    public Element getFunction(Parent parent) {
        for (Element function : functions) {
            if (elementIsParent(function, parent)) {
                return function;
            }
        }
        return null;
    }

    public List<Element> getFunctions() {
        return functions;
    }

    public List<Element> getNamespaces() {
        return namespaces;
    }

    public List<VariableElement> getVariables() {
        return variables;
    }

    public List<ClassElement> getClasses() {
        return classes;
    }

    public List<Element> getFiles() {
        return files;
    }

    public boolean isNamespaceElement(Element element) {
        return namespaces.contains(element);
    }

    public boolean isVariableElement(Element element) {
        return variables.contains(element);
    }

    public boolean isClassElement(Element element) {
        return classes.contains(element);
    }

    public boolean isFunctionElement(Element element) {
        return functions.contains(element);
    }

    public boolean isFileElement(Element element) {
        return files.contains(element);
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

    public List<ClassElement> getClassesWithParent(Parent parent) {
        List<ClassElement> classesWithMatchingParent = new ArrayList<>();

        for (ClassElement clazz : classes) {
            if (elementParentMatchesParent(clazz, parent)) {
                classesWithMatchingParent.add(clazz);
            }
        }
        return classesWithMatchingParent;
    }

    public List<Element> getNamespacesWithParent(Parent parent) {
        return getBasicElementsWithParent(namespaces, parent);
    }

    @Override
    protected List<Element> getAllElements() {
        List<Element> elements = new ArrayList<>();
        elements.addAll(variables);
        elements.addAll(functions);
        elements.addAll(classes);
        elements.addAll(namespaces);
        return elements;
    }

    @Override
    protected CppCommentMatcher buildCommentMatcher() {
        return new CppCommentMatcher();
    }

    @Override
    public Element getElement(Parent parent) {
        if (parent.getType() == Type.VARIABLE) {
            return getVariable(parent);
        } else if (parent.getType() == Type.FUNCTION) {
            return getFunction(parent);
        } else if (parent.getType() == Type.CLASS) {
            return getClass(parent);
        } else if (parent.getType() == Type.NAMESPACE) {
            return getNamespace(parent);
        } else if (parent.getType() == Type.FILE) {
            return getFile(parent);
        }
        return null;
    }

}
