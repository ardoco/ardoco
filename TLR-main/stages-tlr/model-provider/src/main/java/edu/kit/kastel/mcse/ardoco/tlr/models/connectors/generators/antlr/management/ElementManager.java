package edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.management;

import java.util.ArrayList;
import java.util.List;

import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.commentmatching.CommentMatcher;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.Comment;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.Element;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.Parent;

public abstract class ElementManager {

    protected ElementManager() {}

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

    private boolean isRootParent(Parent parent) {
        Element element = getElement(parent);
        return element != null && element.getParent() == null;
    }

    public abstract Element getElement(Parent parent);

    public abstract List<Element> getContentOfParent(Parent parent);

    protected abstract List<Element> getAllElements();

    protected abstract CommentMatcher buildCommentMatcher();

}
