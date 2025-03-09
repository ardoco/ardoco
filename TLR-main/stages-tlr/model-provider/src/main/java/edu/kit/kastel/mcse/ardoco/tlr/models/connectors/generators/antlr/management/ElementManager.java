package edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.management;

import java.util.ArrayList;
import java.util.List;

import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.commentmatching.CommentMatcher;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.Comment;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.Element;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.Parent;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.Type;

public abstract class ElementManager {
    protected final ElementStorageRegistry elementStorageRegistry;
    private final CommentMatcher commentMatcher;

    protected ElementManager(ElementStorageRegistry elementStorageRegistry, CommentMatcher commentMatcher) {
        this.elementStorageRegistry = elementStorageRegistry;
        this.commentMatcher = commentMatcher;
    }

    public void addComments(List<Comment> comments) {
        commentMatcher.matchComments(comments, getAllElements());
    }

    public List<Parent> getRootParents() {
        List<Parent> parents = new ArrayList<>();
        List<Element> elements = getAllElements();

        for (Element element : elements) {
            Parent parent = element.getParent();
            if (hasNotBeenAdded(parent, parents) && this.elementStorageRegistry.isRootParent(parent)) {
                parents.add(parent);
            }
        }
        return parents;
    }

    public Element getElement(Parent parent) {
        return elementStorageRegistry.getElement(parent);
    }

    protected <T extends Element> T getElement(Parent parent, Class<T> clazz) {
        return elementStorageRegistry.getElement(parent, clazz);
    }

    public List<Element> getContentOfParent(Parent parent) {
        return elementStorageRegistry.getContentOfParent(parent);
    }

    public List<Element> getAllElements() {
        return elementStorageRegistry.getAllElements();
    }

    protected <T extends Element> void addElement(Type type, T element) {
        elementStorageRegistry.addElement(type, element);
    }

    protected <T extends Element> void addElements(Type type, List<T> elements) {
        elementStorageRegistry.addElements(type, elements);
    }

    protected boolean isElement(Type type, Element element) {
        return elementStorageRegistry.containsElement(type, element);
    }

    private boolean hasNotBeenAdded(Parent parent, List<Parent> parents) {
        return parent != null && !parents.contains(parent);
    }
}
