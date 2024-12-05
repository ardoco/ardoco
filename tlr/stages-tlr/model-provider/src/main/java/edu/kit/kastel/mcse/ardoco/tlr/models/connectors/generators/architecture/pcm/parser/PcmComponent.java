/* Licensed under MIT 2023. */
package edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.architecture.pcm.parser;

import java.util.ArrayList;
import java.util.List;

import org.fuchss.xmlobjectmapper.annotation.XMLClass;
import org.fuchss.xmlobjectmapper.annotation.XMLList;
import org.fuchss.xmlobjectmapper.annotation.XMLValue;

@XMLClass
public final class PcmComponent {

    @XMLValue
    private String id;

    @XMLValue
    private String entityName;

    @XMLValue(name = "xsi:type")
    private String type;

    @XMLList(name = "requiredRoles_InterfaceRequiringEntity", elementType = InterfaceId.class)
    private List<InterfaceId> requiredInterfaceIds;

    @XMLList(name = "providedRoles_InterfaceProvidingEntity", elementType = InterfaceId.class)
    private List<InterfaceId> providedInterfaceIds;

    @XMLList(name = "assemblyContexts__ComposedStructure", elementType = ComponentId.class)
    private List<ComponentId> innerComponents;

    private List<PcmInterface> required;
    private List<PcmInterface> provided;

    PcmComponent() {
        // NOP
    }

    public String getId() {
        return id;
    }

    public String getEntityName() {
        return entityName;
    }

    public String getType() {
        // e.g., xsi:type="repository:BasicComponent"
        return type.split(":")[1];
    }

    void init(List<PcmInterface> interfaces) {
        provided = providedInterfaceIds.stream()
                .map(currId -> interfaces.stream().filter(it -> it.getId().equals(currId.id())).findFirst().orElseThrow())
                .toList();
        required = requiredInterfaceIds.stream()
                .map(currId -> interfaces.stream().filter(it -> it.getId().equals(currId.id())).findFirst().orElseThrow())
                .toList();
    }

    public List<PcmInterface> getRequired() {
        return new ArrayList<>(required);
    }

    public List<PcmInterface> getProvided() {
        return new ArrayList<>(provided);
    }

    public List<ComponentId> getInnerComponents() {
        return innerComponents == null ? List.of() : new ArrayList<>(innerComponents);
    }

    @XMLClass
    static final class InterfaceId {
        @XMLValue(name = "providedInterface__OperationProvidedRole", mandatory = false)
        private String provided;
        @XMLValue(name = "requiredInterface__OperationRequiredRole", mandatory = false)
        private String required;

        public String id() {
            if (provided == null && required == null)
                throw new IllegalStateException("Required And Provided cannot be null at the same time");
            return provided == null ? required : provided;
        }
    }

    @XMLClass
    public static final class ComponentId {
        @XMLValue(name = "encapsulatedComponent__AssemblyContext")
        private String id;

        public String getId() {
            return id;
        }
    }
}
