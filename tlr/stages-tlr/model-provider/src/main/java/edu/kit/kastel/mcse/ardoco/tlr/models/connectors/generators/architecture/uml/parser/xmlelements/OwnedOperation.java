/* Licensed under MIT 2023. */
package edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.architecture.uml.parser.xmlelements;

import org.fuchss.xmlobjectmapper.annotation.XMLClass;
import org.fuchss.xmlobjectmapper.annotation.XMLValue;

// Added getter
@XMLClass
public final class OwnedOperation {

    @XMLValue(name = "xmi:id")
    private String id;

    @XMLValue
    private String name;

    public String getName() {
        return name;
    }
}
