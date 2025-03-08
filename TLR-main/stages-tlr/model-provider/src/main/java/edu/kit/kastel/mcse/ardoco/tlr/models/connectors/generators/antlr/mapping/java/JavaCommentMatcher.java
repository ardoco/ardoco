package edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.mapping.java;

import java.util.ArrayList;
import java.util.List;

import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.BasicElement;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.CommentElement;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.InterfaceElement;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.VariableElement;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.java.JavaClassElement;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.mapping.CommentMatcher;

public class JavaCommentMatcher extends CommentMatcher {


    public JavaCommentMatcher() {
        super();
    }

    @Override
    protected int calculateDistance(CommentElement comment, BasicElement element) {
        int elementStartLine = element.getStartLine();
        int commentStartLine = comment.getStartLine();
        int commentEndLine = comment.getEndLine();

        int lineDifference = calculateDifference(elementStartLine, commentStartLine, commentEndLine);

        return lineDifference;
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
}