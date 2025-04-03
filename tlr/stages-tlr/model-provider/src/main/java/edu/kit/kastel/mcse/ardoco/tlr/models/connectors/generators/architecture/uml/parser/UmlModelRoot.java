/* Licensed under MIT 2023. */
package edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.architecture.uml.parser;

import java.util.List;

import org.fuchss.xmlobjectmapper.annotation.XMLClass;
import org.fuchss.xmlobjectmapper.annotation.XMLList;
import org.fuchss.xmlobjectmapper.annotation.XMLValue;

import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.architecture.uml.parser.xmlelements.PackagedElement;

@XMLClass(name = "uml:Model")
public final class UmlModelRoot {

    @XMLValue(name = "xmi:id")
    private String id;

    @XMLList(name = "packagedElement", elementType = PackagedElement.class)
    private List<PackagedElement> interfacesAndComponents;

    private List<UmlComponent> components;
    private List<UmlInterface> interfaces;

    public void init() {
        this.interfaces = loadInterfaces();
        this.components = loadComponents();
        this.components.forEach(umlComponent -> umlComponent.init(this));
    }

    private List<UmlComponent> loadComponents() {
        return interfacesAndComponents.stream().filter(PackagedElement::isComponent).map(UmlComponent::new).toList();
    }

    private List<UmlInterface> loadInterfaces() {
        return interfacesAndComponents.stream().filter(PackagedElement::isInterface).map(UmlInterface::new).toList();
    }

    public List<UmlComponent> getComponents() {
        return components;
    }

    public List<UmlInterface> getInterfaces() {
        return interfaces;
    }

    public String getId() {
        return id;
    }
}
