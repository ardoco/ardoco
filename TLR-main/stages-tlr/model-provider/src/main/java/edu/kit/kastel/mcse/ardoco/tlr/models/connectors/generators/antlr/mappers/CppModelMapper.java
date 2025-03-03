package edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.mappers;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.CodeModel;
import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.code.ClassUnit;
import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.code.CodeAssembly;
import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.code.CodeItem;
import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.code.CodeItemRepository;
import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.code.ProgrammingLanguage;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.BasicType;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.ClassElement;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.CommentElement;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.ControlElement;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.Parent;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.VariableElement;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.cpp.NamespaceElement;

public class CppModelMapper {
    private ProgrammingLanguage language = ProgrammingLanguage.CPP;
    private List<VariableElement> variables = new ArrayList<>();
    private List<ControlElement> controls = new ArrayList<>();
    private List<ClassElement> classes = new ArrayList<>();
    private List<NamespaceElement> namespaces = new ArrayList<>();
    private CodeModel codeModel;
    private final CodeItemRepository codeItemRepository;

    public CppModelMapper(CodeItemRepository codeItemRepository, List<VariableElement> variables, List<ControlElement> controls, List<ClassElement> classes, List<NamespaceElement> namespaces, List<CommentElement> comments) {
        CppCommentMapper commentMapper = new CppCommentMapper(variables, controls, classes, namespaces, comments);
        commentMapper.mapComments();
        this.variables = commentMapper.getVariables();
        this.controls = commentMapper.getControls();
        this.classes = commentMapper.getClasses();
        this.namespaces = commentMapper.getNamespaces();
        this.codeItemRepository = codeItemRepository;
    }

    public CodeModel getCodeModel() {
        return codeModel;
    }

    public void mapToCodeModel() {
        SortedSet<CodeItem> content = new TreeSet<>();
        List<Parent> rootParents = findRootParents();

        for (Parent parent : rootParents) {
            content.add(buildCodeAssembly(parent));
        }

        codeModel = new CodeModel(codeItemRepository, content);
    }

    private List<Parent> findRootParents() {
        List<Parent> rootParents = new ArrayList<>();
        for (NamespaceElement namespace : namespaces) {
            Parent candidate = namespace.getParent();
            if (candidate.getType() == BasicType.FILE && !rootParents.contains(candidate)) {
                rootParents.add(candidate);
            }
        }
        for (ClassElement clazz : classes) {
            Parent candidate = clazz.getParent();
            if (candidate.getType() == BasicType.FILE && !rootParents.contains(candidate)) {
                rootParents.add(candidate);
            }
        }
        for (ControlElement control : controls) {
            Parent candidate = control.getParent();
            if (candidate.getType() == BasicType.FILE && !rootParents.contains(candidate)) {
                rootParents.add(candidate);
            }
        }
        for (VariableElement variable : variables) {
            Parent candidate = variable.getParent();
            if (candidate.getType() == BasicType.FILE && !rootParents.contains(candidate)) {
                rootParents.add(candidate);
            }
        }
        return rootParents;
    }

    private CodeAssembly buildCodeAssembly(Parent parent) {
        List<NamespaceElement> namespaces = getAllNamespacesWithParent(parent);
        List<ClassElement> classes = getAllClassesWithParent(parent);
        List<ControlElement> controls = getAllControlsWithParent(parent);
        List<VariableElement> variables = getAllVariablesWithParent(parent);

        String name = parent.getPath().substring(parent.getPath().lastIndexOf("/") + 1);
        SortedSet<CodeItem> content = new TreeSet<>();

        for (NamespaceElement namespace : namespaces) {
            content.add(buildCodeAssembly(namespace));
        }

        for (ClassElement clazz : classes) {
            content.add(buildClassUnit(clazz));
        }

        for (ControlElement control : controls) {
            content.add(buildControlElement(control));
        }

        // variables not implemented yet

        return new CodeAssembly(codeItemRepository, name, content);
    }

    private CodeAssembly buildCodeAssembly(NamespaceElement namespaceElement) {
        String name = namespaceElement.getName();
        String path = namespaceElement.getPath();
        String comment = namespaceElement.getComment();
        Parent comparable = new Parent(name, path, BasicType.NAMESPACE);
        List<NamespaceElement> namespaces = getAllNamespacesWithParent(comparable);
        List<ClassElement> classesOfNamespace = getAllClassesWithParent(comparable);
        List<ControlElement> controlsOfNamespace = getAllControlsWithParent(comparable);
        List<VariableElement> variablesOfNamespace = getAllVariablesWithParent(comparable);
        SortedSet<CodeItem> content = new TreeSet<>();

        for (NamespaceElement namespace : namespaces) {
            content.add(buildCodeAssembly(namespace));
        }
        for (ClassElement clazz : classesOfNamespace) {
            content.add(buildClassUnit(clazz));
        }
        for (ControlElement control : controlsOfNamespace) {
            content.add(buildControlElement(control));
        }

        // variables not implemented yet

        CodeAssembly codeAssembly = new CodeAssembly(codeItemRepository, name, content);
        if (comment != null) {
            codeAssembly.setComment(comment);
        }
        return codeAssembly;
    }

    private ClassUnit buildClassUnit(ClassElement classElement) {
        String name = classElement.getName();
        String path = classElement.getPath();
        String comment = classElement.getComment();
        Parent comparable = new Parent(name, path, BasicType.CLASS);
        List<ControlElement> controlsOfClass = getAllControlsWithParent(comparable);
        List<ClassElement> innerClasses = getAllClassesWithParent(comparable);
        List<VariableElement> variablesOfClass = getAllVariablesWithParent(comparable);
        SortedSet<CodeItem> content = new TreeSet<>();

        // variables not implemented yet
        for (ControlElement control : controlsOfClass) {
            content.add(buildControlElement(control));
        }

        for (ClassElement innerClass : innerClasses) {
            content.add(buildClassUnit(innerClass));
        }
        ClassUnit classUnit = new ClassUnit(codeItemRepository, name, content);
        if (comment != null) {
            classUnit.setComment(comment);
        }
        return classUnit;
    }

    private edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.code.ControlElement buildControlElement(ControlElement control) {
        String name = control.getName();
        String path = control.getPath();
        String comment = control.getComment();
        Parent comparable = new Parent(name, path, BasicType.CONTROL);
        List<VariableElement> contentOfControl = getAllVariablesWithParent(comparable);
        edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.code.ControlElement controlElement = new edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.code.ControlElement(codeItemRepository, name);
        if (comment != null) {
            controlElement.setComment(comment);
        }
        return controlElement;
    }

    private List<NamespaceElement> getAllNamespacesWithParent(Parent parent) {
        List<NamespaceElement> result = new ArrayList<>();
        for (NamespaceElement namespace : namespaces) {
            if (namespace.getParent().equals(parent)) {
                result.add(namespace);
            }
        }
        return result;
    }

    private List<ClassElement> getAllClassesWithParent(Parent parent) {
        List<ClassElement> result = new ArrayList<>();
        for (ClassElement clazz : classes) {
            if (clazz.getParent().equals(parent)) {
                result.add(clazz);
            }
        }
        return result;
    }

    private List<ControlElement> getAllControlsWithParent(Parent parent) {
        List<ControlElement> result = new ArrayList<>();
        for (ControlElement control : controls) {
            if (control.getParent().equals(parent)) {
                result.add(control);
            }
        }
        return result;
    }

    private List<VariableElement> getAllVariablesWithParent(Parent parent) {
        List<VariableElement> result = new ArrayList<>();
        for (VariableElement variable : variables) {
            if (variable.getParent().equals(parent)) {
                result.add(variable);
            }
        }
        return result;
    }










    
}
