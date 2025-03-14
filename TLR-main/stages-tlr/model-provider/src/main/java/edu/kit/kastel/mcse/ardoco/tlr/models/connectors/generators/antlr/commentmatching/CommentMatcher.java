package edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.commentmatching;

import java.util.List;

import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.Element;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.Comment;

public abstract class CommentMatcher {

    protected CommentMatcher() {
    }

    public void matchComments(List<Comment> comments, List<Element> allElements) {
        for (Comment comment : comments) {
            Element closestElement = findClosestElement(comment, allElements);

            if (closestElement != null) {
                setCommentToElement(closestElement, comment);
            }
        }
    }

    private Element findClosestElement(Comment comment, List<Element> allElements) {
        int closestLineDifferenceSoFar = Integer.MAX_VALUE;
        Element closestElement = null;

        for (Element element : allElements) {
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

    private boolean hasSamePath(Comment comment, Element element) {
        return element.getIdentifier().path().equals(comment.getPath());
    }

    private void setCommentToElement(Element element, Comment comment) {
        element.setComment(comment.getText());
    }

    protected abstract int calculateDistance(Comment comment, Element element);
}
