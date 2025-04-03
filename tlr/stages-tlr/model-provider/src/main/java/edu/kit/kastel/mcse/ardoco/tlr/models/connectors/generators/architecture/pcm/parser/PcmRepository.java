/* Licensed under MIT 2023. */
package edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.architecture.pcm.parser;

import java.util.ArrayList;
import java.util.List;

import org.fuchss.xmlobjectmapper.annotation.XMLClass;
import org.fuchss.xmlobjectmapper.annotation.XMLList;
import org.fuchss.xmlobjectmapper.annotation.XMLValue;

@XMLClass(name = "repository:Repository")
public final class PcmRepository {

    @XMLValue
    private String id;

    @XMLValue(mandatory = false)
    private String entityName;

    @XMLList(name = "components__Repository", elementType = PcmComponent.class)
    private List<PcmComponent> components;

    @XMLList(name = "interfaces__Repository", elementType = PcmInterface.class)
    private List<PcmInterface> interfaces;

    @XMLList(name = "dataTypes__Repository", elementType = PcmDatatype.class)
    private List<PcmDatatype> datatypes;

    PcmRepository() {
        // NOP
    }

    void init() {
        interfaces.forEach(i -> i.init(datatypes));
        components.forEach(c -> c.init(interfaces));
    }

    public String getId() {
        return id;
    }

    public String getEntityName() {
        return entityName;
    }

    public List<PcmComponent> getComponents() {
        return new ArrayList<>(components);
    }

    public List<PcmInterface> getInterfaces() {
        return new ArrayList<>(interfaces);
    }
}
