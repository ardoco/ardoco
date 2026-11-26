/* Licensed under MIT 2023-2026. */
package edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.architecture.uml.parser.xmlelements;

import java.util.List;
import java.util.Objects;

import org.fuchss.xmlobjectmapper.annotation.XMLClass;
import org.fuchss.xmlobjectmapper.annotation.XMLList;
import org.fuchss.xmlobjectmapper.annotation.XMLValue;
import org.jspecify.annotations.Nullable;

@XMLClass
public final class PackagedElement {

    @XMLValue(name = "xmi:id")
    @SuppressWarnings("NullAway.Init") // set reflectively by the XML parser
    private String id;

    @XMLValue(name = "xmi:type")
    @SuppressWarnings("NullAway.Init") // set reflectively by the XML parser
    private String type;

    @XMLValue(mandatory = false)
    private @Nullable String name;

    @XMLList(name = "ownedOperation", elementType = OwnedOperation.class)
    @SuppressWarnings("NullAway.Init") // set reflectively by the XML parser
    private List<OwnedOperation> ownedOperations;

    @XMLList(name = "interfaceRealization", elementType = Reference.class)
    private @Nullable List<Reference> interfaceRealizations;

    @XMLList(name = "packagedElement", elementType = Reference.class)
    private @Nullable List<Reference> usages;

    public @Nullable String getName() {
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

    public @Nullable List<Reference> getInterfaceRealizations() {
        return interfaceRealizations;
    }

    public @Nullable List<Reference> getUsages() {
        return usages;
    }

    public String getType() {
        return type;
    }
}
