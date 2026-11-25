/* Licensed under MIT 2023-2026. */
package edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.architecture.uml.parser;

import java.util.Objects;

import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.architecture.uml.parser.xmlelements.PackagedElement;

abstract class UmlElement {

    protected final PackagedElement element;

    protected final String id;
    protected final String name;
    private final String type;

    UmlElement(PackagedElement element) {
        this.element = element;
        this.id = element.getId();
        this.name = Objects.requireNonNull(element.getName(), "A UML component or interface must have a name");
        this.type = element.getType();
    }

    public final String getId() {
        return id;
    }

    public final String getName() {
        return name;
    }

    public final String getType() {
        return type;
    }
}
