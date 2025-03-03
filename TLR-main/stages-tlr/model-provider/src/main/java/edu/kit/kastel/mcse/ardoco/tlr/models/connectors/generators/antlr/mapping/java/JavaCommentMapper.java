package edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.mapping.java;

import java.util.ArrayList;
import java.util.List;

import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.BasicElement;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.CommentElement;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.ControlElement;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.InterfaceElement;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.VariableElement;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.java.JavaClassElement;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.mapping.CommentMapper;

public class JavaCommentMapper extends CommentMapper {
    private List<VariableElement> variables;
    private List<ControlElement> controls;
    private List<JavaClassElement> classes;
    private List<InterfaceElement> interfaces;

    public JavaCommentMapper(List<VariableElement> variables, List<ControlElement> controls,
            List<JavaClassElement> classes, List<InterfaceElement> interfaces, List<CommentElement> comments) {
        super(comments);
        this.variables = variables;
        this.controls = controls;
        this.classes = classes;
        this.interfaces = interfaces;
        this.allElements = new ArrayList<>() {
            {
                addAll(variables);
                addAll(controls);
                addAll(classes);
                addAll(interfaces);
            }
        };
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
            commentAdded = tryAddingToInterface(element, text);
        }
    }

    private int calculateDifference(int elementStartLine, int commentStartLine, int commentEndLine) {
        int lineDifference = Integer.MAX_VALUE;
        // comment just before element
        if (elementStartLine == commentEndLine + 1) {
            lineDifference = 0;
        } else if (commentStartLine <= elementStartLine) {
            lineDifference = Math.abs(elementStartLine - commentEndLine);
        }
        return lineDifference;
    }

    private boolean tryAddingToVariable(BasicElement element, String text) {
        boolean commentAdded = false;
        for (VariableElement variable : variables) {
            if (variable.equals(element)) {
                variable.setComment(text);
                commentAdded = true;
                break;
            }
        }
        return commentAdded;
    }

    private boolean tryAddingToControl(BasicElement element, String text) {
        boolean commentAdded = false;
        for (ControlElement control : controls) {
            if (control.equals(element)) {
                control.setComment(text);
                commentAdded = true;
                break;
            }
        }
        return commentAdded;
    }

    private boolean tryAddingToClass(BasicElement element, String text) {
        boolean commentAdded = false;
        for (JavaClassElement clazz : classes) {
            if (clazz.equals(element)) {
                clazz.setComment(text);
                commentAdded = true;
                break;
            }
        }
        return commentAdded;
    }

    private boolean tryAddingToInterface(BasicElement element, String text) {
        boolean commentAdded = false;
        for (InterfaceElement interf : interfaces) {
            if (interf.equals(element)) {
                interf.setComment(text);
                commentAdded = true;
                break;
            }
        }
        return commentAdded;
    }
}
