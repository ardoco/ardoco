/* Licensed under MIT 2023-2026. */
package edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.architecture.pcm.parser;

import org.fuchss.xmlobjectmapper.annotation.XMLClass;
import org.fuchss.xmlobjectmapper.annotation.XMLValue;

@XMLClass
public final class PcmDatatype {

    @XMLValue
    @SuppressWarnings("NullAway.Init") // set reflectively by the XML parser
    private String id;

    @XMLValue
    @SuppressWarnings("NullAway.Init") // set reflectively by the XML parser
    private String entityName;

    PcmDatatype() {
        // NOP
    }

    public String getId() {
        return id;
    }

    public String getEntityName() {
        return entityName;
    }
}
