package edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements;

import java.util.ArrayList;
import java.util.List;

import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.mapping.CommentMatcher;

public abstract class ElementManager {

    protected ElementManager() {}

    public void addComments(List<CommentElement> comments) {
        CommentMatcher commentMatcher = buildCommentMatcher();
        commentMatcher.matchComments(comments, getAllElements());
    }

    public List<Parent> getRootParents() {
        List<Parent> parents = new ArrayList<>();
        List<BasicElement> elements = getAllElements();

        for (BasicElement element : elements) {

            if (element.getParent() != null && !parents.contains(element.getParent()) && 
            isRootParent(element.getParent())) {
                parents.add(element.getParent());
            }
        }
        return parents;
    }

    protected List<BasicElement> getBasicElementsWithParent(List<BasicElement> basicElements, Parent parent) {
        List<BasicElement> elementsWithMatchingParent = new ArrayList<>();
        for (BasicElement element : basicElements) {
            if (element.getParent() != null && element.getParent().equals(parent)) {
                elementsWithMatchingParent.add(element);
            }
        }
        return elementsWithMatchingParent;
    }

    protected abstract boolean isRootParent(Parent parent);
    public abstract List<BasicElement> getElementsWithParent(Parent parent);
    protected abstract List<BasicElement> getAllElements();
    protected abstract CommentMatcher buildCommentMatcher();

}
