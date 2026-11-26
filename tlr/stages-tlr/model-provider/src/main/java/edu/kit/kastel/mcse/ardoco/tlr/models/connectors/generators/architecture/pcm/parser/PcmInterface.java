/* Licensed under MIT 2023-2026. */
package edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.architecture.pcm.parser;

import java.util.ArrayList;
import java.util.List;

import org.fuchss.xmlobjectmapper.annotation.XMLClass;
import org.fuchss.xmlobjectmapper.annotation.XMLList;
import org.fuchss.xmlobjectmapper.annotation.XMLValue;

@XMLClass
public final class PcmInterface {

    @XMLValue
    @SuppressWarnings("NullAway.Init") // set reflectively by the XML parser
    private String id;

    @XMLValue
    @SuppressWarnings("NullAway.Init") // set reflectively by the XML parser
    private String entityName;

    @XMLList(name = "signatures__OperationInterface", elementType = PcmSignature.class)
    @SuppressWarnings("NullAway.Init") // set reflectively by the XML parser
    private List<PcmSignature> methods;

    PcmInterface() {
        // NOP
    }

    public String getId() {
        return id;
    }

    public String getEntityName() {
        return entityName;
    }

    public List<PcmSignature> getMethods() {
        return new ArrayList<>(methods);
    }

    void init(List<PcmDatatype> datatypes) {
        for (PcmSignature method : methods) {
            method.init(datatypes);
        }
    }
}
