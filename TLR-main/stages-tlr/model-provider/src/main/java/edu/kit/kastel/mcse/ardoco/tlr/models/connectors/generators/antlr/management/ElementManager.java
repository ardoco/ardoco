package edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.management;

import java.util.ArrayList;
import java.util.List;

import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.commentmatching.CommentMatcher;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.Comment;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.Element;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.Parent;

public abstract class ElementManager {

    protected ElementManager() {
    }

    public void addComments(List<Comment> comments) {
        CommentMatcher commentMatcher = buildCommentMatcher();
        commentMatcher.matchComments(comments, getAllElements());
    }

    public List<Parent> getRootParents() {
        List<Parent> parents = new ArrayList<>();
        List<Element> elements = getAllElements();

        for (Element element : elements) {

            if (element.getParent() != null && !parents.contains(element.getParent()) &&
                    isRootParent(element.getParent())) {
                parents.add(element.getParent());
            }
        }
        return parents;
    }

    protected List<Element> getBasicElementsWithParent(List<Element> basicElements, Parent parent) {
        List<Element> elementsWithMatchingParent = new ArrayList<>();

        for (Element element : basicElements) {
            if (elementParentMatchesParent(element, parent)) {
                elementsWithMatchingParent.add(element);
            }
        }
        return elementsWithMatchingParent;
    }

    protected boolean elementParentMatchesParent(Element element, Parent parent) {
        return (parent == null && element.getParent() == null)
                || (element.getParent() != null && element.getParent().equals(parent));
    }

    protected boolean elementIsParent(Element element, Parent parent) {
        return parent != null && element.getName().equals(parent.getName())
                && element.getPath().equals(parent.getPath());
    }

    private boolean isRootParent(Parent parent) {
        Element element = getElement(parent);
        return element != null && element.getParent() == null;
    }

    public abstract Element getElement(Parent parent);

    public abstract List<Element> getContentOfParent(Parent parent);

    protected abstract List<Element> getAllElements();

    protected abstract CommentMatcher buildCommentMatcher();

}
