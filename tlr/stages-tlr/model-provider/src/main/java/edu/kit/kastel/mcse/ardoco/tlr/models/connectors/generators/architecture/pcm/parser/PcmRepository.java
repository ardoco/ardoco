/* Licensed under MIT 2023-2026. */
package edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.architecture.pcm.parser;

import java.util.ArrayList;
import java.util.List;

import org.fuchss.xmlobjectmapper.annotation.XMLClass;
import org.fuchss.xmlobjectmapper.annotation.XMLList;
import org.fuchss.xmlobjectmapper.annotation.XMLValue;
import org.jspecify.annotations.Nullable;

@XMLClass(name = "repository:Repository")
public final class PcmRepository {

    @XMLValue
    @SuppressWarnings("NullAway.Init") // set reflectively by the XML parser
    private String id;

    @XMLValue(mandatory = false)
    private @Nullable String entityName;

    @XMLList(name = "components__Repository", elementType = PcmComponent.class)
    @SuppressWarnings("NullAway.Init") // set reflectively by the XML parser
    private List<PcmComponent> components;

    @XMLList(name = "interfaces__Repository", elementType = PcmInterface.class)
    @SuppressWarnings("NullAway.Init") // set reflectively by the XML parser
    private List<PcmInterface> interfaces;

    @XMLList(name = "dataTypes__Repository", elementType = PcmDatatype.class)
    @SuppressWarnings("NullAway.Init") // set reflectively by the XML parser
    private List<PcmDatatype> datatypes;

    PcmRepository() {
        // NOP
    }

    void init() {
        for (PcmInterface pcmInterface : interfaces) {
            pcmInterface.init(datatypes);
        }
        for (PcmComponent pcmComponent : components) {
            pcmComponent.init(interfaces);
        }
    }

    public String getId() {
        return id;
    }

    public @Nullable String getEntityName() {
        return entityName;
    }

    public List<PcmComponent> getComponents() {
        return new ArrayList<>(components);
    }

    public List<PcmInterface> getInterfaces() {
        return new ArrayList<>(interfaces);
    }
}
