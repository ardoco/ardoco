package edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.management;

import java.util.ArrayList;
import java.util.List;

import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.Element;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.ElementIdentifier;

public class ElementStorage<T extends Element> {
    private final Class<T> type;	
    private List<T> elements;

    public ElementStorage(List<T> elements, Class<T> type) {
        this.type = type;
        this.elements.addAll(elements);
    }

    public ElementStorage(Class<T> type) {
        this.type = type;
        elements = new ArrayList<>();
    }

    public Class<T> getType() {
        return type;
    }

    public void addElement(T element) {
        if (element == null || elements.contains(element)) {
            return;
        }
        this.elements.add(element);
    }

    public void addElements(List<T> elements) {
        for (T element : elements) {
            addElement(element);
        }
    }

    public T getElement(ElementIdentifier parent) {
        for (T element : elements) {
            if (elementIsParent(element, parent)) {
                return element;
            }
        }
        return null;
    }

    public List<T> getElements() {
        return elements;
    }

    public boolean contains(Element element) {
        return elements.contains(element);
    }

    public List<T> getContentOfParent(ElementIdentifier parent) {
        List<T> elementsWithMatchingParent = new ArrayList<>();

        for (T element : elements) {
            if (elementParentMatchesParent(element, parent)) {
                elementsWithMatchingParent.add(element);
            }
        }
        return elementsWithMatchingParent;
    }

    public List<T> getElementsWithoutParent() {
        List<T> elementsWithoutParent = new ArrayList<>();

        for (T element : elements) {
            if (element.getParentIdentifier() == null) {
                elementsWithoutParent.add(element);
            }
        }
        return elementsWithoutParent;
    }

    private boolean elementIsParent(T element, ElementIdentifier parent) {
        return parent != null && element.getName().equals(parent.name()) && element.getPath().equals(parent.path());
    }

    private boolean elementParentMatchesParent(T element, ElementIdentifier parent) {
        return (parent == null && element.getParentIdentifier() == null) || (element.getParentIdentifier() != null && element.getParentIdentifier().equals(parent));
    }



    
    
}
