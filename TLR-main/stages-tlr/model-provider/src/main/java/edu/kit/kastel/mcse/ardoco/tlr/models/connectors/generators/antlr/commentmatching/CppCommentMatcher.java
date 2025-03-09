package edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.commentmatching;

import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.Element;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.Comment;

public class CppCommentMatcher extends CommentMatcher {

    public CppCommentMatcher() {
        super();
    }

    @Override
    protected int calculateDistance(Comment comment, Element element) {
        int elementStartLine = element.getStartLine();
        int commentStartLine = comment.getStartLine();
        int commentEndLine = comment.getEndLine();

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