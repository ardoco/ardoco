package edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.mapping.cpp;

import java.util.ArrayList;
import java.util.List;

import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.BasicElement;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.ClassElement;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.CommentElement;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.ControlElement;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.VariableElement;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.cpp.NamespaceElement;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.mapping.CommentMapper;

public class CppCommentMapper extends CommentMapper {
    private List<VariableElement> variables;
    private List<ControlElement> controls;
    private List<ClassElement> classes;
    private List<NamespaceElement> namespaces;

    public CppCommentMapper(List<VariableElement> variables, List<ControlElement> controls, List<ClassElement> classes,
            List<NamespaceElement> namespaces, List<CommentElement> comments) {
        super(comments);
        this.variables = variables;
        this.controls = controls;
        this.classes = classes;
        this.namespaces = namespaces;
        this.allElements = new ArrayList<>() {
            {
                addAll(variables);
                addAll(controls);
                addAll(classes);
                addAll(namespaces);
            }
        };
    }

    public List<VariableElement> getVariables() {
        return variables;
    }

    public List<ControlElement> getControls() {
        return controls;
    }

    public List<ClassElement> getClasses() {
        return classes;
    }

    public List<NamespaceElement> getNamespaces() {
        return namespaces;
    }

    @Override
    protected int calculateDistance(CommentElement comment, BasicElement element) {
        int elementStartLine = element.getStartLine();
        int commentStartLine = comment.getStartLine();
        int commentEndLine = comment.getEndLine();

        int lineDifference = calculateDifference(elementStartLine, commentStartLine, commentEndLine);

        return lineDifference;
    }

    @Override
    protected void setCommentToElement(BasicElement element, CommentElement comment) {
        String text = comment.getText();
        boolean commentAdded = false;

        commentAdded = tryAddingToVariable(element, text);

        if (!commentAdded) {
            commentAdded = tryAddingToControl(element, text);
        }

        if (!commentAdded) {
            commentAdded = tryAddingToClass(element, text);
        }

        if (!commentAdded) {
            commentAdded = tryAddingToNamespace(element, text);
        }
    }

    private int calculateDifference(int elementStartLine, int commentStartLine, int commentEndLine) {
        int lineDifference = Integer.MAX_VALUE;

        if (elementStartLine == commentEndLine + 1) {
            lineDifference = 0;
        } else if (commentStartLine <= elementStartLine) {
            lineDifference = Math.abs(elementStartLine - commentEndLine);
        }
        return lineDifference;
    }

    private boolean tryAddingToVariable(BasicElement element, String text) {
        for (VariableElement variable : variables) {
            if (variable.equals(element)) {
                variable.setComment(text);
                return true;
            }
        }
        return false;
    }

    private boolean tryAddingToControl(BasicElement element, String text) {
        for (ControlElement control : controls) {
            if (control.equals(element)) {
                control.setComment(text);
                return true;
            }
        }
        return false;
    }

    private boolean tryAddingToClass(BasicElement element, String text) {
        for (ClassElement clazz : classes) {
            if (clazz.equals(element)) {
                clazz.setComment(text);
                return true;
            }
        }
        return false;
    }

    private boolean tryAddingToNamespace(BasicElement element, String text) {
        for (NamespaceElement namespace : namespaces) {
            if (namespace.equals(element)) {
                namespace.setComment(text);
                return true;
            }
        }
        return false;
    }
}
