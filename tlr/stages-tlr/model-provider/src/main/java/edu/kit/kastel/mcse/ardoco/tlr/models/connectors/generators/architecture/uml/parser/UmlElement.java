/* Licensed under MIT 2023. */
package edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.architecture.uml.parser;

import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.architecture.uml.parser.xmlelements.PackagedElement;

abstract class UmlElement {

    protected final PackagedElement element;

    protected final String id;
    protected final String name;
    private final String type;

    UmlElement(PackagedElement element) {
        this.element = element;
        this.id = element.getId();
        this.name = element.getName();
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
