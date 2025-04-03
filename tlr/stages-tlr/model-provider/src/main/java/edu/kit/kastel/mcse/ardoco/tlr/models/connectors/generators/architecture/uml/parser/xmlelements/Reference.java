/* Licensed under MIT 2023. */
package edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.architecture.uml.parser.xmlelements;

import org.fuchss.xmlobjectmapper.annotation.XMLClass;
import org.fuchss.xmlobjectmapper.annotation.XMLValue;

@XMLClass
public final class Reference {

    @XMLValue(name = "xmi:id")
    private String id;

    @XMLValue
    private String client;

    @XMLValue
    private String supplier;

    public String getId() {
        return id;
    }

    public String getClient() {
        return client;
    }

    public String getSupplier() {
        return supplier;
    }
}
