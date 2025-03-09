package edu.kit.kastel.mcse.ardoco.tlr.models.generators.antlr.elements;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.ClassElement;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.Element;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.PackageElement;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.Parent;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.Type;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.VariableElement;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.java.JavaClassElement;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.management.CppElementManager;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.management.ElementManager;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.management.JavaElementManager;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.management.Python3ElementManager;

public class ElementManagerTest {
    private ElementManager elementManager;

    @Test
    void getRootParentsJavaTest() {
        elementManager = new JavaElementManager(getCorrectVariablesList(), getCorrectFunctionsList(), getCorrectClassList(), getCorrectInterfaceList(), getCorrectCompilationUnitList(), getCorrectPackageList());
        
        List<Parent> rootParents = elementManager.getRootParents();

        Assertions.assertEquals(1, rootParents.size());
    }

    @Test
    void getRootParentsCppTest() {
        elementManager = new CppElementManager(getCorrectVariablesList(), getCorrectFunctionsList(), getCorrectClassesCppList(), getCorrectNamespacesList(), new ArrayList<>());

        List<Parent> rootParents = elementManager.getRootParents();

        Assertions.assertEquals(1, rootParents.size());
    }

    @Test
    void getRootParentsPythonTest() {
        elementManager = new Python3ElementManager(getCorrectPythonVariablesList(), getCorrectFunctionsList(), getCorrectClassesList(), new ArrayList<>(), getCorrectPackageList());

        List<Parent> rootParents = elementManager.getRootParents();

        Assertions.assertEquals(1, rootParents.size());
    }




    private List<VariableElement> getCorrectVariablesList() {
        List<VariableElement> variables = new ArrayList<>();
        String path = "path";
        String dataType = "int";
        Parent parent = new Parent("a", "path", Type.FUNCTION);
        variables.add(new VariableElement("a", path, dataType, parent));
        variables.add(new VariableElement("b", path, dataType, parent));
        variables.add(new VariableElement("c", path, dataType, parent));
        return variables;
    }

    private List<Element> getCorrectFunctionsList() {
        List<Element> functions = new ArrayList<>();
        String path = "path";
        Parent parent = new Parent("a", path, Type.CLASS);
        
        functions.add(new Element("a", path, parent));
        functions.add(new Element("b", path, parent));
        functions.add(new Element("c", path, parent));
        return functions;
    }

    private List<JavaClassElement> getCorrectClassList() {
        List<JavaClassElement> classes = new ArrayList<>();
        String path = "path";
        Parent parent = new Parent("a", path, Type.COMPILATIONUNIT);
        classes.add(new JavaClassElement("a", path, parent, 0, 0));
        classes.add(new JavaClassElement("b", path, parent, 0, 0));
        classes.add(new JavaClassElement("c", path, parent, 0, 0));
        return classes;
    }

    private List<Element> getCorrectInterfaceList() {
        List<Element> interfaces = new ArrayList<>();
        String path = "path";
        Parent parent = new Parent("a", path, Type.COMPILATIONUNIT);
        interfaces.add(new Element("a", path, parent));
        interfaces.add(new Element("b", path, parent));
        interfaces.add(new Element("c", path, parent));
        return interfaces;
    }

    private List<Element> getCorrectCompilationUnitList() {
        List<Element> compilationUnits = new ArrayList<>();
        String path = "path";
        Parent parent = new Parent("a", path, Type.PACKAGE);
        compilationUnits.add(new Element("a", path, parent));
        compilationUnits.add(new Element("b", path, parent));
        compilationUnits.add(new Element("c", path, parent));
        return compilationUnits;
    }

    private List<PackageElement> getCorrectPackageList() {
        List<PackageElement> packages = new ArrayList<>();
        String path = "path";
        packages.add(new PackageElement("a", path));
        packages.add(new PackageElement("b", path));
        packages.add(new PackageElement("c", path));
        return packages;
    }

    private List<VariableElement> getCorrectPythonVariablesList() {
        List<VariableElement> variables = new ArrayList<>();
        String path = "path";
        String dataType = "string";
        Parent parent = new Parent("a", "path", Type.FUNCTION);

        variables.add(new VariableElement("var1", path, dataType, parent));
        variables.add(new VariableElement("var2", path, dataType, parent));
        variables.add(new VariableElement("var3", path, dataType, parent));
        return variables;
    }

    private List<ClassElement> getCorrectClassesList() {
        List<ClassElement> classes = new ArrayList<>();
        String path = "path";
        Parent parent = new Parent("a", path, Type.PACKAGE);
        
        classes.add(new ClassElement("a", path, parent));
        classes.add(new ClassElement("b", path, parent));
        classes.add(new ClassElement("c", path, parent));
        return classes;
    }    

    private List<ClassElement> getCorrectClassesCppList() {
        List<ClassElement> classes = new ArrayList<>();
        String path = "path";
        Parent parent = new Parent("a", path, Type.NAMESPACE);
        
        classes.add(new ClassElement("a", path, parent));
        classes.add(new ClassElement("b", path, parent));
        classes.add(new ClassElement("c", path, parent));
        return classes;
    }

    private List<Element> getCorrectNamespacesList() {
        List<Element> namespaces = new ArrayList<>();
        String path = "path";
        Parent parent = null;
        
        namespaces.add(new Element("a", path, parent));
        namespaces.add(new Element("b", path, parent));
        namespaces.add(new Element("c", path, parent));
        return namespaces;
    }
}
