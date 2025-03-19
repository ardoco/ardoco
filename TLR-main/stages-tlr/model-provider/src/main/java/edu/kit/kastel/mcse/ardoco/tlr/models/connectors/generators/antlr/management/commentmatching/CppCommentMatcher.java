package edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.management.commentmatching;

import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.Element;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.Comment;

/**
 * Responsible for matching comments from C++ code to structural elements.
 * Defines the rules for what is considered the closest distance between a
 * comment and an element.
 */
public class CppCommentMatcher extends CommentMatcher {

    public CppCommentMatcher() {
        super();
    }

    @Override
    protected int calculateDistance(Comment comment, Element element) {
        int elementStartLine = element.getStartLine();
        int commentStartLine = comment.startLine();
        int commentEndLine = comment.endLine();

        int lineDifference = calculateDifference(elementStartLine, commentStartLine, commentEndLine);

        return lineDifference;
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

}