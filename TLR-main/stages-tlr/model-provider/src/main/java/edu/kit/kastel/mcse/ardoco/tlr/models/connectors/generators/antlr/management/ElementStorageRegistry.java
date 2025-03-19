package edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.management;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.commentmatching.CommentMatcher;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.Comment;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.Element;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.ElementIdentifier;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.Type;

public abstract class ElementStorageRegistry {
    private final CommentMatcher commentMatcher;
    private final Map<Type, ElementStorage<?>> storages = new HashMap<>();
    private final Map<Type, Class<?>> typeOfClass = new EnumMap<>(Type.class);
    

    public ElementStorageRegistry(CommentMatcher commentMatcher) {
        registerStorage();
        createTypeMap();
        this.commentMatcher = commentMatcher;
    }

    protected abstract void registerStorage();

    protected <T extends Element> void registerStorage(Type type, ElementStorage<T> storage) {
        if (hasStorage(type)) {
            return;
        }
        storages.put(type, storage);
    }

    public boolean hasStorage(Type type) {
        return typeOfClass.containsKey(type) && storages.containsKey(type);
    }

    protected <T extends Element> void addElement(Type type, T element) {
        if (hasStorage(type, element)) {
            ElementStorage<T> storage = getTypedStorage(type);
            if (storage != null) {
                storage.addElement(element);
            }
        }
    }

    protected <T extends Element> void addElements(Type type, Iterable<T> elements) {
        for (T element : elements) {
            addElement(type, element);
        }
    }

    protected <T extends Element> T getElement(ElementIdentifier parent, Class<T> clazz) {
        if (parent.type() == null || !verifyAllowed(parent.type(), clazz)) {
            return null;
        }
        ElementStorage<T> storage = getTypedStorage(parent.type());
        return storage.getElement(parent);
    }

    protected <T extends Element> List<T> getElements(Type type, Class<T> clazz) {
        if (hasStorage(type, clazz)) {
            ElementStorage<T> storage = getStorage(type, clazz);
            return storage.getElements();
        }
        return new ArrayList<>();
    }

    protected <T extends Element> boolean containsElement(Type type, T element) {
        if (hasStorage(type, element)) {
            ElementStorage<T> storage = getTypedStorage(type);
            return storage.contains(element);
        }
        return false;
    }

    public List<Element> getContentOfParent(ElementIdentifier parent) {
        List<Element> elements = new ArrayList<>();
        for (ElementStorage<?> storage : storages.values()) {
            elements.addAll(storage.getContentOfParent(parent));
        }
        return elements;
    }

    protected <T extends Element> List<T> getContentOfParent(Type type, ElementIdentifier parent) {
        if (hasStorage(type)) {
            ElementStorage<T> storage = getTypedStorage(type);
            return storage.getContentOfParent(parent);
        }
        return new ArrayList<>();
    }

    public List<Element> getAllElements() {
        List<Element> elements = new ArrayList<>();
        for (ElementStorage<?> storage : storages.values()) {
            elements.addAll(storage.getElements());
        }
        return elements;
    }

    protected List<Element> getElementsWithoutParent() {
        List<Element> roots = new ArrayList<>();
        for (ElementStorage<?> storage : storages.values()) {
            roots.addAll(storage.getElementsWithoutParent());
        }
        return roots;
    }

    public boolean isRootParent(ElementIdentifier parent) {
        ElementStorage<?> storage = getTypedStorage(parent.type());
        if (storage != null) {

            return storage.getElement(parent) != null && storage.getElement(parent).getParentIdentifier() == null;
        }
        return false;
    }

    public Element getElement(ElementIdentifier parent) {
        for (ElementStorage<?> storage : storages.values()) {
            Element element = storage.getElement(parent);
            if (element != null) {
                return element;
            }
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private <T extends Element> ElementStorage<T> getTypedStorage(Type type) {
        Class<T> clazz = (Class<T>) typeOfClass.get(type);
        return (ElementStorage<T>) getStorage(type, clazz);
    }

    @SuppressWarnings("unchecked")
    public <T extends Element> ElementStorage<T> getStorage(Type type, Class<T> clazz) {
        if (hasStorage(type, clazz)) {
            return (ElementStorage<T>) storages.get(type);
        }
        return null;
    }

    private <T extends Element> boolean verifyAllowed(Type type, Class<T> clazz) {
        return typeOfClass.get(type) != null && typeOfClass.get(type).equals(clazz);
    }

    private <T extends Element> boolean hasStorage(Type type, T element) {
        return element != null && storages.containsKey(type) && storages.get(type).getType().equals(element.getClass());
    }

    private <T extends Element> boolean hasStorage(Type type, Class<T> clazz) {
        return storages.containsKey(type) && storages.get(type).getType().equals(clazz);
    }

    private void createTypeMap() {
        for (Type type : storages.keySet()) {
            typeOfClass.put(type, storages.get(type).getType());
        }
    }

    public void addComments(List<Comment> comments) {
        commentMatcher.matchComments(comments, getAllElements());
    }

    public List<ElementIdentifier> getRootParents() {
        List<ElementIdentifier> parents = new ArrayList<>();
        List<Element> elements = getAllElements();

        for (Element element : elements) {
            ElementIdentifier parent = element.getParentIdentifier();
            if (hasNotBeenAdded(parent, parents) && isRootParent(parent)) {
                parents.add(parent);
            }
        }
        return parents;
    }

    private boolean hasNotBeenAdded(ElementIdentifier parent, List<ElementIdentifier> parents) {
        return parent != null && !parents.contains(parent);
    }
}
