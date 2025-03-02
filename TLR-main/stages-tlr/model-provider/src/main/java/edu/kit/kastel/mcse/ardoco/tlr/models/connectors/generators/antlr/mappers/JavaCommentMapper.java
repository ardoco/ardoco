package edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.mappers;

import java.util.ArrayList;
import java.util.List;

import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.BasicElement;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.CommentElement;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.ControlElement;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.InterfaceElement;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.VariableElement;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.java.JavaClassElement;

public class JavaCommentMapper {
    private List<VariableElement> variables;
    private List<ControlElement> controls;
    private List<JavaClassElement> classes;
    private List<InterfaceElement> interfaces;
    private List<CommentElement> comments;


    public JavaCommentMapper(List<VariableElement> variables, List<ControlElement> controls, List<JavaClassElement> classes, List<InterfaceElement> interfaces, List<CommentElement> comments) {
        this.variables = variables;
        this.controls = controls;
        this.classes = classes;
        this.interfaces = interfaces;
        this.comments = comments;
    }

    public void mapComments() {
        List<BasicElement> allElements = new ArrayList<>();
        allElements.addAll(variables);
        allElements.addAll(controls);
        allElements.addAll(classes);
        allElements.addAll(interfaces);

        for (CommentElement comment : comments) {
            BasicElement closestElement = findClosestElement(comment, allElements);

            if (closestElement != null) {
                setCommentToElement(closestElement, comment);
            } else {
                throw new IllegalStateException("No element found for comment.");
            }
        }
    }

    public List<VariableElement> getVariables() {
        return variables;
    }

    public List<ControlElement> getControls() {
        return controls;
    }

    public List<JavaClassElement> getClasses() {
        return classes;
    }

    public List<InterfaceElement> getInterfaces() {
        return interfaces;
    }

    private BasicElement findClosestElement(CommentElement comment, List<BasicElement> elements) {
        int lineDifference = Integer.MAX_VALUE;
        BasicElement closestElement = null;
        for (BasicElement element : elements) {
            if (element.getPath().equals(comment.getPath())) {
                int lineDifferenceForElement = getLineDifference(comment, element, lineDifference);
                if (lineDifferenceForElement < lineDifference) {
                    lineDifference = lineDifferenceForElement;
                    closestElement = element;
                }
                if (lineDifferenceForElement == 0) {
                    return element;
                }
            }
        }
        return closestElement;
    }

    private int getLineDifference(CommentElement comment, BasicElement element, int closestLineDifferenceSoFar) {
        int elementStartLine = element.getStartLine();
        int elementEndLine = element.getEndLine();
        int commentStartLine = comment.getStartLine();
        int commentEndLine = comment.getEndLine();

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
        for (VariableElement var : variables) {
            if (var.equals(element)) {
                var.setComment(text);
                found = true;
            }
        }
        if (!found) {
            for (ControlElement control : controls) {
                if (control.equals(element)) {
                    control.setComment(text);
                    found = true;
                }
            }
        }
        if (!found) {
            for (JavaClassElement clazz : classes) {
                if (clazz.equals(element)) {
                    clazz.setComment(text);
                    found = true;
                }
            }
        }
        if (!found) {
            for (InterfaceElement interf : interfaces) {
                if (interf.equals(element)) {
                    interf.setComment(text);
                    found = true;
                }
            }
        }

        if (!found) {
            throw new IllegalStateException("Element not found in any list.");
        }
    }

    
}
