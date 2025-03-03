package edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.mapping;

import java.util.List;

import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.BasicElement;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.CommentElement;

public abstract class CommentMapper {
    protected List<BasicElement> allElements;
    private List<CommentElement> comments;

    protected CommentMapper(List<CommentElement> comments) {
        this.comments = comments;
    }

    public void mapComments() {
        for (CommentElement comment : comments) {
            BasicElement closestElement = findClosestElement(comment);

            if (closestElement != null) {
                setCommentToElement(closestElement, comment);
            }
        }
    }

    private BasicElement findClosestElement(CommentElement comment) {
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

    protected abstract int calculateDistance(CommentElement comment, BasicElement element);

    protected abstract void setCommentToElement(BasicElement element, CommentElement comment);

}
