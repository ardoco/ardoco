package edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.management;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.Element;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.ElementIdentifier;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.Type;

public abstract class ElementStorageRegistryDeprectated {
    private final Map<Type, ElementStorage<?>> storages = new HashMap<>();
    private final Map<Type, Class<?>> typeOfClass = new EnumMap<>(Type.class);


    protected ElementStorageRegistryDeprectated(EnumMap<Type, Class<?>> typeOfClass) {
        this.typeOfClass.putAll(typeOfClass);
        registerStorage();
    }

    protected <T extends Element> void registerStorage(Type type, ElementStorage<T> storage) {
        if (!verifyAllowedType(type) || hasStorage(type)) {
            return;
        }
        if (verifyAllowed(type, storage)) {
            storages.put(type, storage);
        }
    }    

    public boolean hasStorage(Type type) {
        return typeOfClass.containsKey(type) && storages.containsKey(type);
    }

    public <T extends Element> void addElement(Type type, T element) {
        if (hasStorage(type, element)) {
            ElementStorage<T> storage = getTypedStorage(type);
            if (storage != null) {
                storage.addElement(element);
            }
        }
    }

    public <T extends Element> void addElements(Type type, Iterable<T> elements) {
        for (T element : elements) {
            addElement(type, element);
        }
    }

    public <T extends Element> T getElement(ElementIdentifier parent, Class<T> clazz) {
        if (parent.type() == null || !verifyAllowed(parent.type(), clazz)) {
            return null;
        } 
        ElementStorage<T> storage = getTypedStorage(parent.type());
        return storage.getElement(parent);
    }

    public <T extends Element> List<T> getElements(Type type, Class<T> clazz) {
        if (hasStorage(type, clazz)) {
            ElementStorage<T> storage = getStorage(type, clazz);
            return storage.getElements();
        }
        return null;
    }

    public <T extends Element> boolean containsElement(Type type, T element) {
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

    public <T extends Element> List<T> getContentOfParent(Type type, ElementIdentifier parent) {
        if (hasStorage(type)) {
            ElementStorage<T> storage = getTypedStorage(type);
            return storage.getContentOfParent(parent);
        }
        return null;
    }

    public List<Element> getAllElements() {
        List<Element> elements = new ArrayList<>();
        for (ElementStorage<?> storage : storages.values()) {
            elements.addAll(storage.getElements());
        }
        return elements;
    }

    public List<Element> getElementsWithoutParent() {
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

    private boolean verifyAllowedType(Type type) {
        return typeOfClass.containsKey(type);
    }

    private boolean verifyAllowed(Type type, ElementStorage<?> storage) {
        return verifyAllowed(type, storage.getType());
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

    protected abstract void registerStorage();
    
}
