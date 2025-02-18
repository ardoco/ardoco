package edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.mappers;

import java.util.ArrayList;
import java.util.List;

import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.BasicElement;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.CommentElement;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.ControlElement;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.python3.Python3ClassElement;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.python3.Python3VariableElement;

public class Python3CommentMapper {
    private List<Python3VariableElement> variables;
    private List<ControlElement> controls;
    private List<Python3ClassElement> classes;
    private List<CommentElement> comments;

    public Python3CommentMapper(List<Python3VariableElement> variables, List<ControlElement> controls, List<Python3ClassElement> classes, List<CommentElement> comments) {
        this.variables = variables;
        this.controls = controls;
        this.classes = classes;
        this.comments = comments;
    }

    public void mapComments() {
        List<BasicElement> allElements = new ArrayList<>();
        allElements.addAll(variables);
        allElements.addAll(controls);
        allElements.addAll(classes);

        for (CommentElement comment : comments) {
            BasicElement closestElement = findClosestElement(comment, allElements);

            if (closestElement != null) {
                setCommentToElement(closestElement, comment);
            } else {
                throw new IllegalStateException("No element found for comment.");
            }
        }
    }

    public List<Python3VariableElement> getVariables() {
        return variables;
    }

    public List<ControlElement> getControls() {
        return controls;
    }

    public List<Python3ClassElement> getClasses() {
        return classes;
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

        // comment just after beginning of element
        if (commentStartLine == elementStartLine + 1) {
            return 0;
        }

        if (elementEndLine < commentStartLine) {
            return Integer.MAX_VALUE;
        }

        return Math.abs(elementStartLine - commentEndLine);
    }

    private void setCommentToElement(BasicElement element, CommentElement comment) {
        boolean found = false;

        for (Python3VariableElement variable : variables) {
            if (variable.equals(element)) {
                variable.setComment(comment.getText());
                found = true;
                break;
            }
        }

        if (!found) {
            for (ControlElement control : controls) {
                if (control.equals(element)) {
                    control.setComment(comment.getText());
                    found = true;
                    break;
                }
            }
        }

        if (!found) {
            for (Python3ClassElement classElement : classes) {
                if (classElement.equals(element)) {
                    classElement.setComment(comment.getText());
                    found = true;
                    break;
                }
            }
        }
        
        if (!found) {
            throw new IllegalStateException("No element found for comment.");
        }
    }
    
    
}
