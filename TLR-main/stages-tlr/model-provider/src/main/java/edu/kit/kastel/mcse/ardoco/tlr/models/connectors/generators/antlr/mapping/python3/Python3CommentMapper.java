package edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.mapping.python3;

import java.util.ArrayList;
import java.util.List;

import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.BasicElement;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.CommentElement;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.ClassElement;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.python3.Python3VariableElement;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.mapping.CommentMatcher;

public class Python3CommentMapper extends CommentMatcher {


    public Python3CommentMapper() {
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
}