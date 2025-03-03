package edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.mappers;

import java.util.ArrayList;
import java.util.List;

import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.BasicElement;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.ClassElement;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.CommentElement;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.ControlElement;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.VariableElement;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.cpp.NamespaceElement;

public class CppCommentMapper {
    private List<VariableElement> variables;
    private List<ControlElement> controls;
    private List<ClassElement> classes;
    private List<NamespaceElement> namespaces;
    private List<CommentElement> comments;

    public CppCommentMapper(List<VariableElement> variables, List<ControlElement> controls, List<ClassElement> classes, List<NamespaceElement> namespaces, List<CommentElement> comments) {
        this.variables = variables;
        this.controls = controls;
        this.classes = classes;
        this.namespaces = namespaces;
        this.comments = comments;
    }

    public void mapComments() {
        List<BasicElement> allElements = new ArrayList<>();
        allElements.addAll(variables);
        allElements.addAll(controls);
        allElements.addAll(classes);
        allElements.addAll(namespaces);

        for (CommentElement comment: comments) {
            BasicElement closestElement = findClosestElement(comment, allElements);

            if (closestElement != null) {
                setCommentToElement(closestElement, comment);
            }
        }
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

    private BasicElement findClosestElement(CommentElement comment, List<BasicElement> elements) {
        BasicElement closestElement = null;
        int minDistance = Integer.MAX_VALUE;

        for (BasicElement element: elements) {
            if (element.getPath().equals(comment.getPath())) {
                int distance = getLineDifference(comment, element, minDistance);
                if (distance < minDistance) {
                    minDistance = distance;
                    closestElement = element;
                }
                if (minDistance == 0) {
                    return element;
                }
            }
        }
        return closestElement;
    }

    private int getLineDifference(CommentElement comment, BasicElement element, int minDistance) {
        int commentStartLine = comment.getStartLine();
        int commentEndLine = comment.getEndLine();
        int elementStartLine = element.getStartLine();
        int elementEndLine = element.getEndLine();

        // comment just before element
        if (elementStartLine == commentEndLine + 1) {
            return 0;
        }

        if (elementEndLine < commentStartLine) {
            return Integer.MAX_VALUE;
        }

        return Math.abs(elementStartLine - commentEndLine);
    }
    
    private void setCommentToElement(BasicElement element, CommentElement comment) {
        boolean found = false;
        String text = comment.getText();

        for (VariableElement variable: variables) {
            if (variable.equals(element)) {
                variable.setComment(text);
                found = true;
                break;
            }
        }

        if (!found) {
            for (ControlElement control: controls) {
                if (control.equals(element)) {
                    control.setComment(text);
                    found = true;
                    break;
                }
            }
        }

        if (!found) {
            for (ClassElement clazz: classes) {
                if (clazz.equals(element)) {
                    clazz.setComment(text);
                    found = true;
                    break;
                }
            }
        }

        if (!found) {
            for (NamespaceElement namespace: namespaces) {
                if (namespace.equals(element)) {
                    namespace.setComment(text);
                    found = true;
                    break;
                }
            }
        }
    }
    
}
