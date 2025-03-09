package edu.kit.kastel.mcse.ardoco.tlr.models.generators.antlr.elements;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.ClassElement;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.Element;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.Parent;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.Type;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.VariableElement;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.management.CppElementManager;

public class CppElementManagerTest {
    private CppElementManager elementManager;

    @Test
    void addVariablesTest() {
        elementManager = new CppElementManager();
        List<VariableElement> variables = getCorrectVariablesList();
        for (VariableElement variable : variables) {
            elementManager.addVariable(variable);
        }

        Assertions.assertEquals(3, elementManager.getVariables().size());
        Assertions.assertEquals(variables.get(0), elementManager.getVariables().get(0));
        Assertions.assertEquals(variables.get(1), elementManager.getVariables().get(1));
        Assertions.assertEquals(variables.get(2), elementManager.getVariables().get(2));
    }

    @Test
    void addVariablesNullTest() {
        elementManager = new CppElementManager();
        List<VariableElement> variables = getNullVariables();
        for (VariableElement variable : variables) {
            elementManager.addVariable(variable);
        }

        Assertions.assertEquals(2, elementManager.getVariables().size());
        Assertions.assertEquals(variables.get(0), elementManager.getVariables().get(0));
        Assertions.assertEquals(variables.get(2), elementManager.getVariables().get(1));
    }

    @Test
    void addFunctionsTest() {
        elementManager = new CppElementManager();
        List<Element> functions = getCorrectFunctionsList();
        for (Element function : functions) {
            elementManager.addFunction(function);
        }

        Assertions.assertEquals(3, elementManager.getFunctions().size());
        Assertions.assertEquals(functions.get(0), elementManager.getFunctions().get(0));
        Assertions.assertEquals(functions.get(1), elementManager.getFunctions().get(1));
        Assertions.assertEquals(functions.get(2), elementManager.getFunctions().get(2));
    }

    @Test
    void addFunctionsNullTest() {
        elementManager = new CppElementManager();
        List<Element> functions = getNullFunctions();
        for (Element function : functions) {
            elementManager.addFunction(function);
        }

        Assertions.assertEquals(2, elementManager.getFunctions().size());
        Assertions.assertEquals(functions.get(0), elementManager.getFunctions().get(0));
        Assertions.assertEquals(functions.get(2), elementManager.getFunctions().get(1));
    }

    @Test
    void addClassesTest() {
        elementManager = new CppElementManager();
        List<ClassElement> classes = getCorrectClassesList();
        for (ClassElement clazz : classes) {
            elementManager.addClass(clazz);
        }

        Assertions.assertEquals(3, elementManager.getClasses().size());
        Assertions.assertEquals(classes.get(0), elementManager.getClasses().get(0));
        Assertions.assertEquals(classes.get(1), elementManager.getClasses().get(1));
        Assertions.assertEquals(classes.get(2), elementManager.getClasses().get(2));
    }

    @Test
    void addClassesNullTest() {
        elementManager = new CppElementManager();
        List<ClassElement> classes = getNullClasses();
        for (ClassElement clazz : classes) {
            elementManager.addClass(clazz);
        }

        Assertions.assertEquals(2, elementManager.getClasses().size());
        Assertions.assertEquals(classes.get(0), elementManager.getClasses().get(0));
        Assertions.assertEquals(classes.get(2), elementManager.getClasses().get(1));
    }

    @Test
    void addNamespacesTest() {
        elementManager = new CppElementManager();
        List<Element> namespaces = getCorrectNamespacesList();
        for (Element namespace : namespaces) {
            elementManager.addNamespace(namespace);
        }

        Assertions.assertEquals(3, elementManager.getNamespaces().size());
        Assertions.assertEquals(namespaces.get(0), elementManager.getNamespaces().get(0));
        Assertions.assertEquals(namespaces.get(1), elementManager.getNamespaces().get(1));
        Assertions.assertEquals(namespaces.get(2), elementManager.getNamespaces().get(2));
    }

    @Test
    void addNamespacesNullTest() {
        elementManager = new CppElementManager();
        List<Element> namespaces = getNullNamespaces();
        for (Element namespace : namespaces) {
            elementManager.addNamespace(namespace);
        }

        Assertions.assertEquals(2, elementManager.getNamespaces().size());
        Assertions.assertEquals(namespaces.get(0), elementManager.getNamespaces().get(0));
        Assertions.assertEquals(namespaces.get(2), elementManager.getNamespaces().get(1));
    }

    @Test
    void getVariableTest() {
        elementManager = new CppElementManager();
        List<VariableElement> variables = getCorrectVariablesList();
        for (VariableElement variable : variables) {
            elementManager.addVariable(variable);
        }
        Parent parent0 = new Parent (variables.get(0).getName(), variables.get(0).getPath(), Type.VARIABLE);
        Parent parent1 = new Parent (variables.get(1).getName(), variables.get(1).getPath(), Type.VARIABLE);
        Parent parent2 = new Parent (variables.get(2).getName(), variables.get(2).getPath(), Type.VARIABLE);

        Assertions.assertEquals(3, elementManager.getVariables().size());
        Assertions.assertEquals(variables.get(0), elementManager.getVariable(parent0));
        Assertions.assertEquals(variables.get(1), elementManager.getVariable(parent1));
        Assertions.assertEquals(variables.get(2), elementManager.getVariable(parent2));
    }

    @Test
    void getVariableNullTest() {
        elementManager = new CppElementManager();
        List<VariableElement> variables = getNullVariables();
        for (VariableElement variable : variables) {
            elementManager.addVariable(variable);
        }
        Parent parent0 = new Parent (variables.get(0).getName(), variables.get(0).getPath(), Type.VARIABLE);
        Parent parent2 = new Parent (variables.get(2).getName(), variables.get(2).getPath(), Type.VARIABLE);

        Assertions.assertEquals(2, elementManager.getVariables().size());
        Assertions.assertEquals(variables.get(0), elementManager.getVariable(parent0));
        Assertions.assertEquals(variables.get(2), elementManager.getVariable(parent2));
    }

    @Test
    void getNamespaceTest() {
        elementManager = new CppElementManager();
        List<Element> namespaces = getCorrectNamespacesList();
        for (Element namespace : namespaces) {
            elementManager.addNamespace(namespace);
        }

        Parent parent0 = new Parent (namespaces.get(0).getName(), namespaces.get(0).getPath(), Type.NAMESPACE);
        Parent parent1 = new Parent (namespaces.get(1).getName(), namespaces.get(1).getPath(), Type.NAMESPACE);
        Parent parent2 = new Parent (namespaces.get(2).getName(), namespaces.get(2).getPath(), Type.NAMESPACE);

        Assertions.assertEquals(3, elementManager.getNamespaces().size());
        Assertions.assertEquals(namespaces.get(0), elementManager.getNamespace(parent0));
        Assertions.assertEquals(namespaces.get(1), elementManager.getNamespace(parent1));
        Assertions.assertEquals(namespaces.get(2), elementManager.getNamespace(parent2));
    }

    @Test
    void getNamespaceNullTest() {
        elementManager = new CppElementManager();
        List<Element> namespaces = getNullNamespaces();
        for (Element namespace : namespaces) {
            elementManager.addNamespace(namespace);
        }

        Parent parent0 = new Parent (namespaces.get(0).getName(), namespaces.get(0).getPath(), Type.NAMESPACE);
        Parent parent2 = new Parent (namespaces.get(2).getName(), namespaces.get(2).getPath(), Type.NAMESPACE);

        Assertions.assertEquals(2, elementManager.getNamespaces().size());
        Assertions.assertEquals(namespaces.get(0), elementManager.getNamespace(parent0));
        Assertions.assertEquals(namespaces.get(2), elementManager.getNamespace(parent2));
    }

    @Test
    void getClassTest() {
        elementManager = new CppElementManager();
        List<ClassElement> classes = getCorrectClassesList();
        for (ClassElement clazz : classes) {
            elementManager.addClass(clazz);
        }

        Parent parent0 = new Parent (classes.get(0).getName(), classes.get(0).getPath(), Type.CLASS);
        Parent parent1 = new Parent (classes.get(1).getName(), classes.get(1).getPath(), Type.CLASS);
        Parent parent2 = new Parent (classes.get(2).getName(), classes.get(2).getPath(), Type.CLASS);

        Assertions.assertEquals(3, elementManager.getClasses().size());
        Assertions.assertEquals(classes.get(0), elementManager.getClass(parent0));
        Assertions.assertEquals(classes.get(1), elementManager.getClass(parent1));
        Assertions.assertEquals(classes.get(2), elementManager.getClass(parent2));
    }

    @Test
    void getClassNullTest() {
        elementManager = new CppElementManager();
        List<ClassElement> classes = getNullClasses();
        for (ClassElement clazz : classes) {
            elementManager.addClass(clazz);
        }

        Parent parent0 = new Parent (classes.get(0).getName(), classes.get(0).getPath(), Type.CLASS);
        Parent parent2 = new Parent (classes.get(2).getName(), classes.get(2).getPath(), Type.CLASS);

        Assertions.assertEquals(2, elementManager.getClasses().size());
        Assertions.assertEquals(classes.get(0), elementManager.getClass(parent0));
        Assertions.assertEquals(classes.get(2), elementManager.getClass(parent2));
    }

    @Test
    void getFunctionTest() {
        elementManager = new CppElementManager();
        List<Element> functions = getCorrectFunctionsList();
        for (Element function : functions) {
            elementManager.addFunction(function);
        }

        Parent parent0 = new Parent (functions.get(0).getName(), functions.get(0).getPath(), Type.FUNCTION);
        Parent parent1 = new Parent (functions.get(1).getName(), functions.get(1).getPath(), Type.FUNCTION);
        Parent parent2 = new Parent (functions.get(2).getName(), functions.get(2).getPath(), Type.FUNCTION);

        Assertions.assertEquals(3, elementManager.getFunctions().size());
        Assertions.assertEquals(functions.get(0), elementManager.getFunction(parent0));
        Assertions.assertEquals(functions.get(1), elementManager.getFunction(parent1));
        Assertions.assertEquals(functions.get(2), elementManager.getFunction(parent2));
    }

    @Test
    void getFunctionNullTest() {
        elementManager = new CppElementManager();
        List<Element> functions = getNullFunctions();
        for (Element function : functions) {
            elementManager.addFunction(function);
        }

        Parent parent0 = new Parent (functions.get(0).getName(), functions.get(0).getPath(), Type.FUNCTION);
        Parent parent2 = new Parent (functions.get(2).getName(), functions.get(2).getPath(), Type.FUNCTION);

        Assertions.assertEquals(2, elementManager.getFunctions().size());
        Assertions.assertEquals(functions.get(0), elementManager.getFunction(parent0));
        Assertions.assertEquals(functions.get(2), elementManager.getFunction(parent2));
    }

    @Test
    void getVariablesWithParentSimpleTest() {
        elementManager = new CppElementManager();
        List<VariableElement> variables = getCorrectVariablesList();
        for (VariableElement variable : variables) {
            elementManager.addVariable(variable);
        }

        Parent parentOfVars = new Parent("parentOfVars", "path", Type.FUNCTION);

        List<VariableElement> elements = elementManager.getVariablesWithParent(parentOfVars);

        Assertions.assertEquals(3, elements.size());
        Assertions.assertEquals(variables.get(0), elements.get(0));
        Assertions.assertEquals(variables.get(1), elements.get(1));
        Assertions.assertEquals(variables.get(2), elements.get(2));
    }

    @Test
    void getVariablesWithParentNullTest() {
        elementManager = new CppElementManager();
        List<VariableElement> variables = getNullVariables();
        for (VariableElement variable : variables) {
            elementManager.addVariable(variable);
        }

        Parent parentOfVars = new Parent("parentOfVars", "path", Type.FUNCTION);

        List<VariableElement> elements = elementManager.getVariablesWithParent(parentOfVars);

        Assertions.assertEquals(0, elements.size());
    }

    @Test
    void getVariablesWrongParentTest() {
        elementManager = new CppElementManager();
        List<VariableElement> variables = getCorrectVariablesList();
        for (VariableElement variable : variables) {
            elementManager.addVariable(variable);
        }

        Parent parentOfVars = new Parent("wrongParent", "path", Type.FUNCTION);

        List<VariableElement> elements = elementManager.getVariablesWithParent(parentOfVars);

        Assertions.assertEquals(0, elements.size());
    }

    @Test 
    void getVariablesWithDifferentParentsTest() {
        elementManager = new CppElementManager();
        List<VariableElement> variables = getCorrectVariablesList();
        VariableElement diffVar = new VariableElement("diffVar", "path", "int", new Parent("diffVarParent", "path", Type.FUNCTION));
        variables.add(diffVar);
        for (VariableElement variable : variables) {
            elementManager.addVariable(variable);
        }

        Parent parentOfVars = new Parent("parentOfVars", "path", Type.FUNCTION);
        Parent parentOfVars2 = new Parent("diffVarParent", "path", Type.FUNCTION);

        List<VariableElement> elements = elementManager.getVariablesWithParent(parentOfVars);
        List<VariableElement> elements2 = elementManager.getVariablesWithParent(parentOfVars2);

        Assertions.assertEquals(3, elements.size());
        Assertions.assertEquals(1, elements2.size());
        Assertions.assertEquals(variables.get(0), elements.get(0));
        Assertions.assertEquals(variables.get(1), elements.get(1));
        Assertions.assertEquals(variables.get(2), elements.get(2));
        Assertions.assertEquals(diffVar, elements2.get(0));
    }

    @Test
    void getFunctionsWithParentSimpleTest() {
        elementManager = new CppElementManager();
        List<Element> functions = getCorrectFunctionsList();
        for (Element function : functions) {
            elementManager.addFunction(function);
        }

        Parent parentOfFc = new Parent("parentOfFc", "path", Type.CLASS);

        List<Element> elements = elementManager.getFunctionsWithParent(parentOfFc);

        Assertions.assertEquals(3, elements.size());
        Assertions.assertEquals(functions.get(0), elements.get(0));
        Assertions.assertEquals(functions.get(1), elements.get(1));
        Assertions.assertEquals(functions.get(2), elements.get(2));
    }

    @Test
    void getFunctionsWithParentNullTest() {
        elementManager = new CppElementManager();
        List<Element> functions = getNullFunctions();
        for (Element function : functions) {
            elementManager.addFunction(function);
        }

        Parent parentOfFc = new Parent("parentOfFc", "path", Type.CLASS);

        List<Element> elements = elementManager.getFunctionsWithParent(parentOfFc);

        Assertions.assertEquals(0, elements.size());
    }

    @Test
    void getFunctionsWithDifferentParentsTest() {
        elementManager = new CppElementManager();
        List<Element> functions = getCorrectFunctionsList();
        Element diffFc = new Element("diffFc", "path", new Parent("diffFcParent", "path", Type.CLASS));
        functions.add(diffFc);
        for (Element function : functions) {
            elementManager.addFunction(function);
        }

        Parent parentOfFc = new Parent("parentOfFc", "path", Type.CLASS);
        Parent parentOfFc2 = new Parent("diffFcParent", "path", Type.CLASS);

        List<Element> elements = elementManager.getFunctionsWithParent(parentOfFc);
        List<Element> elements2 = elementManager.getFunctionsWithParent(parentOfFc2);

        Assertions.assertEquals(3, elements.size());
        Assertions.assertEquals(1, elements2.size());
        Assertions.assertEquals(functions.get(0), elements.get(0));
        Assertions.assertEquals(functions.get(1), elements.get(1));
        Assertions.assertEquals(functions.get(2), elements.get(2));
        Assertions.assertEquals(diffFc, elements2.get(0));
    }

    @Test
    void getClassWithParentSimpleTest() {
        elementManager = new CppElementManager();
        List<ClassElement> classes = getCorrectClassesList();
        for (ClassElement clazz : classes) {
            elementManager.addClass(clazz);
        }

        Parent parentOfCs = new Parent("parentOfCs", "path", Type.NAMESPACE);

        List<ClassElement> elements = elementManager.getClassesWithParent(parentOfCs);

        Assertions.assertEquals(3, elements.size());
        Assertions.assertEquals(classes.get(0), elements.get(0));
        Assertions.assertEquals(classes.get(1), elements.get(1));
        Assertions.assertEquals(classes.get(2), elements.get(2));
    }

    @Test
    void getClassWithParentNullTest() {
        elementManager = new CppElementManager();
        List<ClassElement> classes = getNullClasses();
        for (ClassElement clazz : classes) {
            elementManager.addClass(clazz);
        }

        Parent parentOfCs = new Parent("parentOfCs", "path", Type.NAMESPACE);

        List<ClassElement> elements = elementManager.getClassesWithParent(parentOfCs);

        Assertions.assertEquals(0, elements.size());
    }

    @Test
    void getClassWithDifferentParentsTest() {
        elementManager = new CppElementManager();
        List<ClassElement> classes = getCorrectClassesList();
        ClassElement diffCs = new ClassElement("diffCs", "path", new Parent("diffCsParent", "path", Type.NAMESPACE));
        classes.add(diffCs);
        for (ClassElement clazz : classes) {
            elementManager.addClass(clazz);
        }

        Parent parentOfCs = new Parent("parentOfCs", "path", Type.NAMESPACE);
        Parent parentOfCs2 = new Parent("diffCsParent", "path", Type.NAMESPACE);

        List<ClassElement> elements = elementManager.getClassesWithParent(parentOfCs);
        List<ClassElement> elements2 = elementManager.getClassesWithParent(parentOfCs2);

        Assertions.assertEquals(3, elements.size());
        Assertions.assertEquals(1, elements2.size());
        Assertions.assertEquals(classes.get(0), elements.get(0));
        Assertions.assertEquals(classes.get(1), elements.get(1));
        Assertions.assertEquals(classes.get(2), elements.get(2));
        Assertions.assertEquals(diffCs, elements2.get(0));
    }

    @Test
    void getNamespaceWithParentSimpleTest() {
        elementManager = new CppElementManager();
        List<Element> namespaces = getCorrectNamespacesList();
        for (Element namespace : namespaces) {
            elementManager.addNamespace(namespace);
        }

        Parent parentOfNs = new Parent("parentOfNs", "path", Type.FILE);

        List<Element> elements = elementManager.getNamespacesWithParent(parentOfNs);

        Assertions.assertEquals(3, elements.size());
        Assertions.assertEquals(namespaces.get(0), elements.get(0));
        Assertions.assertEquals(namespaces.get(1), elements.get(1));
        Assertions.assertEquals(namespaces.get(2), elements.get(2));
    }

    @Test
    void getNamespaceWithParentNullTest() {
        elementManager = new CppElementManager();
        List<Element> namespaces = getNullNamespaces();
        for (Element namespace : namespaces) {
            elementManager.addNamespace(namespace);
        }

        Parent parentOfNs = new Parent("parentOfNs", "path", Type.FILE);

        List<Element> elements = elementManager.getNamespacesWithParent(parentOfNs);

        Assertions.assertEquals(0, elements.size());
    }

    @Test
    void getNamespaceWithDifferentParentsTest() {
        elementManager = new CppElementManager();
        List<Element> namespaces = getCorrectNamespacesList();
        Element diffNs = new Element("diffNs", "path", new Parent("diffNsParent", "path", Type.FILE));
        namespaces.add(diffNs);
        for (Element namespace : namespaces) {
            elementManager.addNamespace(namespace);
        }

        Parent parentOfNs = new Parent("parentOfNs", "path", Type.FILE);
        Parent parentOfNs2 = new Parent("diffNsParent", "path", Type.FILE);

        List<Element> elements = elementManager.getNamespacesWithParent(parentOfNs);
        List<Element> elements2 = elementManager.getNamespacesWithParent(parentOfNs2);

        Assertions.assertEquals(3, elements.size());
        Assertions.assertEquals(1, elements2.size());
        Assertions.assertEquals(namespaces.get(0), elements.get(0));
        Assertions.assertEquals(namespaces.get(1), elements.get(1));
        Assertions.assertEquals(namespaces.get(2), elements.get(2));
        Assertions.assertEquals(diffNs, elements2.get(0));
    }


    private List<Element> getCorrectFunctionsList() {
        List<Element> functions = new ArrayList<>();
        String path = "path";
        Parent parent = new Parent("parentOfFc", path, Type.CLASS);
        
        functions.add(new Element("a", path, parent));
        functions.add(new Element("b", path, parent));
        functions.add(new Element("c", path, parent));
        return functions;
    }



    private List<Element> getNullFunctions() {
        List<Element> functions = new ArrayList<>();
        functions.add(new Element("a", "path", null));
        functions.add(null);
        functions.add(new Element("c", "path", null));
        return functions;
    }

    private List<ClassElement> getCorrectClassesList() {
        List<ClassElement> classes = new ArrayList<>();
        String path = "path";
        Parent parent = new Parent("parentOfCs", path, Type.NAMESPACE);
        
        classes.add(new ClassElement("a", path, parent));
        classes.add(new ClassElement("b", path, parent));
        classes.add(new ClassElement("c", path, parent));
        return classes;
    }

    private List<ClassElement> getNullClasses() {
        List<ClassElement> classes = new ArrayList<>();
        classes.add(new ClassElement("a", "path", null));
        classes.add(null);
        classes.add(new ClassElement("c", "path", null));
        return classes;
    }

    private List<Element> getCorrectNamespacesList() {
        List<Element> namespaces = new ArrayList<>();
        String path = "path";
        Parent parent = new Parent("parentOfNs", path, Type.FILE);
        
        namespaces.add(new Element("a", path, parent));
        namespaces.add(new Element("b", path, parent));
        namespaces.add(new Element("c", path, parent));
        return namespaces;
    }

    private List<Element> getNullNamespaces() {
        List<Element> namespaces = new ArrayList<>();
        namespaces.add(new Element("a", "path", null));
        namespaces.add(null);
        namespaces.add(new Element("c", "path", null));
        return namespaces;
    }

    private List<VariableElement> getCorrectVariablesList() {
        List<VariableElement> variables = new ArrayList<>();
        String path = "path";
        String dataType = "int";
        Parent parent = new Parent("parentOfVars", "path", Type.FUNCTION);
        variables.add(new VariableElement("a", path, dataType, parent));
        variables.add(new VariableElement("b", path, dataType, parent));
        variables.add(new VariableElement("c", path, dataType, parent));
        return variables;
    }

    private List<VariableElement>  getNullVariables() {
        List<VariableElement> variables = new ArrayList<>();
        variables.add(new VariableElement("a", "path", "string", null));
        variables.add(null);
        variables.add(new VariableElement("c", "path", "int", null));
        return variables;
    }
}
