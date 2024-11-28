/* Licensed under MIT 2023. */
package edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.architecture.uml.parser.xmlelements;

import java.util.List;
import java.util.Objects;

import org.fuchss.xmlobjectmapper.annotation.XMLClass;
import org.fuchss.xmlobjectmapper.annotation.XMLList;
import org.fuchss.xmlobjectmapper.annotation.XMLValue;

@XMLClass
public final class PackagedElement {

    @XMLValue(name = "xmi:id")
    private String id;

    @XMLValue(name = "xmi:type")
    private String type;

    @XMLValue(mandatory = false)
    private String name;

    @XMLList(name = "ownedOperation", elementType = OwnedOperation.class)
    private List<OwnedOperation> ownedOperations;

    @XMLList(name = "interfaceRealization", elementType = Reference.class)
    private List<Reference> interfaceRealizations;

    @XMLList(name = "packagedElement", elementType = Reference.class)
    private List<Reference> usages;

    public String getName() {
        return name;
    }

    public String getId() {
        return id;
    }

    public boolean isComponent() {
        return Objects.equals(type, "uml:Component");
    }

    public boolean isInterface() {
        return Objects.equals(type, "uml:Interface");
    }

    public List<OwnedOperation> getOwnedOperations() {
        return ownedOperations;
    }

    public List<Reference> getInterfaceRealizations() {
        return interfaceRealizations;
    }

    public List<Reference> getUsages() {
        return usages;
    }

    public String getType() {
        return type;
    }
}
