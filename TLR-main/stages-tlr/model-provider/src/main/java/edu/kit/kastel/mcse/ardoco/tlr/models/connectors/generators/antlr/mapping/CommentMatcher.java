package edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.mapping;

import java.util.List;

import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.BasicElement;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.CommentElement;

public abstract class CommentMatcher {

    protected CommentMatcher() {
    }

    public void matchComments(List<CommentElement> comments, List<BasicElement> allElements) {
        for (CommentElement comment : comments) {
            BasicElement closestElement = findClosestElement(comment, allElements);

            if (closestElement != null) {
                setCommentToElement(closestElement, comment);
            }
        }
    }

    private BasicElement findClosestElement(CommentElement comment, List<BasicElement> allElements) {
        int closestLineDifferenceSoFar = Integer.MAX_VALUE;
        BasicElement closestElement = null;

        for (BasicElement element : allElements) {
            if (hasSamePath(comment, element)) {
                int calculatedLineDifference = calculateDistance(comment, element);

                if (calculatedLineDifference < closestLineDifferenceSoFar) {
                    closestLineDifferenceSoFar = calculatedLineDifference;
                    closestElement = element;
                }
            }
        }
        return closestElement;
    }

    private boolean hasSamePath(CommentElement comment, BasicElement element) {
        return element.getPath().equals(comment.getPath());
    }

    private void setCommentToElement(BasicElement element, CommentElement comment) {
        element.setComment(comment.getText());
    }

    protected abstract int calculateDistance(CommentElement comment, BasicElement element);
}
