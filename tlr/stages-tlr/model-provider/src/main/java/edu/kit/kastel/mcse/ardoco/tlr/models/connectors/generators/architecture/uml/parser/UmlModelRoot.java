/* Licensed under MIT 2023-2026. */
package edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.architecture.uml.parser;

import java.util.List;
import java.util.Objects;

import org.fuchss.xmlobjectmapper.annotation.XMLClass;
import org.fuchss.xmlobjectmapper.annotation.XMLList;
import org.fuchss.xmlobjectmapper.annotation.XMLValue;
import org.jspecify.annotations.Nullable;

import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.architecture.uml.parser.xmlelements.PackagedElement;

@XMLClass(name = "uml:Model")
public final class UmlModelRoot {

    @XMLValue(name = "xmi:id")
    @SuppressWarnings("NullAway.Init") // set reflectively by the XML parser
    private String id;

    @XMLList(name = "packagedElement", elementType = PackagedElement.class)
    @SuppressWarnings("NullAway.Init") // set reflectively by the XML parser
    private List<PackagedElement> interfacesAndComponents;

    private @Nullable List<UmlComponent> components;
    private @Nullable List<UmlInterface> interfaces;

    public void init() {
        List<UmlInterface> loadedInterfaces = loadInterfaces();
        this.interfaces = loadedInterfaces;
        this.components = loadComponents(loadedInterfaces);
    }

    private List<UmlComponent> loadComponents(List<UmlInterface> umlInterfaces) {
        return interfacesAndComponents.stream().filter(PackagedElement::isComponent).map(it -> new UmlComponent(it, umlInterfaces)).toList();
    }

    private List<UmlInterface> loadInterfaces() {
        return interfacesAndComponents.stream().filter(PackagedElement::isInterface).map(UmlInterface::new).toList();
    }

    public List<UmlComponent> getComponents() {
        return Objects.requireNonNull(components, "The model has not been initialized");
    }

    public List<UmlInterface> getInterfaces() {
        return Objects.requireNonNull(interfaces, "The model has not been initialized");
    }

    public String getId() {
        return id;
    }
}
