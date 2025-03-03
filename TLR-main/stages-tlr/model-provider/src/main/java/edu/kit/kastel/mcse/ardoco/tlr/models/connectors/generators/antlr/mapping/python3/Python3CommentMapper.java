package edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.mapping.python3;

import java.util.ArrayList;
import java.util.List;

import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.BasicElement;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.CommentElement;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.ControlElement;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.ClassElement;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.python3.Python3VariableElement;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.mapping.CommentMapper;

public class Python3CommentMapper extends CommentMapper {
    private List<Python3VariableElement> variables;
    private List<ControlElement> controls;
    private List<ClassElement> classes;

    public Python3CommentMapper(List<Python3VariableElement> variables, List<ControlElement> controls,
            List<ClassElement> classes, List<CommentElement> comments) {
        super(comments);
        this.variables = variables;
        this.controls = controls;
        this.classes = classes;
        this.allElements = new ArrayList<>() {
            {
                addAll(variables);
                addAll(controls);
                addAll(classes);
            }
        };
    }

    public List<Python3VariableElement> getVariables() {
        return variables;
    }

    public List<ControlElement> getControls() {
        return controls;
    }

    public List<ClassElement> getClasses() {
        return classes;
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
            throw new IllegalStateException("No element found for comment.");
        }
    }

    private int calculateDifference(int elementStartLine, int commentStartLine, int commentEndLine) {
        // Comments Ideally just after Element
        if (commentStartLine == elementStartLine + 1) {
            return 0;
        }

        // Comment before element
        if (commentEndLine < elementStartLine) {
            return Integer.MAX_VALUE;
        }

        return Math.abs(elementStartLine - commentEndLine);
    }

    private boolean tryAddingToVariable(BasicElement element, String text) {
        for (Python3VariableElement variable : variables) {
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
        for (ClassElement classElement : classes) {
            if (classElement.equals(element)) {
                classElement.setComment(text);
                return true;
            }
        }
        return false;
    }
}
