/* Licensed under MIT 2023-2026. */
package edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.architecture.uml.parser.xmlelements;

import org.fuchss.xmlobjectmapper.annotation.XMLClass;
import org.fuchss.xmlobjectmapper.annotation.XMLValue;

// Added getter
@XMLClass
public final class OwnedOperation {

    @XMLValue(name = "xmi:id")
    @SuppressWarnings("NullAway.Init") // set reflectively by the XML parser
    private String id;

    @XMLValue
    @SuppressWarnings("NullAway.Init") // set reflectively by the XML parser
    private String name;

    public String getName() {
        return name;
    }
}
