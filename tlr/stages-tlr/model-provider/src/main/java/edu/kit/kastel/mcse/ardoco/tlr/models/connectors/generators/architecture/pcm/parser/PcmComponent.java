/* Licensed under MIT 2023-2026. */
package edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.architecture.pcm.parser;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.fuchss.xmlobjectmapper.annotation.XMLClass;
import org.fuchss.xmlobjectmapper.annotation.XMLList;
import org.fuchss.xmlobjectmapper.annotation.XMLValue;
import org.jspecify.annotations.Nullable;

@XMLClass
public final class PcmComponent {

    @XMLValue
    @SuppressWarnings("NullAway.Init") // set reflectively by the XML parser
    private String id;

    @XMLValue
    @SuppressWarnings("NullAway.Init") // set reflectively by the XML parser
    private String entityName;

    @XMLValue(name = "xsi:type")
    @SuppressWarnings("NullAway.Init") // set reflectively by the XML parser
    private String type;

    @XMLList(name = "requiredRoles_InterfaceRequiringEntity", elementType = InterfaceId.class)
    @SuppressWarnings("NullAway.Init") // set reflectively by the XML parser
    private List<InterfaceId> requiredInterfaceIds;

    @XMLList(name = "providedRoles_InterfaceProvidingEntity", elementType = InterfaceId.class)
    @SuppressWarnings("NullAway.Init") // set reflectively by the XML parser
    private List<InterfaceId> providedInterfaceIds;

    @XMLList(name = "assemblyContexts__ComposedStructure", elementType = ComponentId.class)
    private @Nullable List<ComponentId> innerComponents;

    private @Nullable List<PcmInterface> required;
    private @Nullable List<PcmInterface> provided;

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
        return new ArrayList<>(Objects.requireNonNull(required, "The component has not been initialized"));
    }

    public List<PcmInterface> getProvided() {
        return new ArrayList<>(Objects.requireNonNull(provided, "The component has not been initialized"));
    }

    public List<ComponentId> getInnerComponents() {
        return innerComponents == null ? List.of() : new ArrayList<>(innerComponents);
    }

    @XMLClass
    static final class InterfaceId {
        @XMLValue(name = "providedInterface__OperationProvidedRole", mandatory = false)
        private @Nullable String provided;
        @XMLValue(name = "requiredInterface__OperationRequiredRole", mandatory = false)
        private @Nullable String required;

        public String id() {
            String providedId = provided;
            if (providedId != null)
                return providedId;
            String requiredId = required;
            if (requiredId == null)
                throw new IllegalStateException("Required And Provided cannot be null at the same time");
            return requiredId;
        }
    }

    @XMLClass
    public static final class ComponentId {
        @XMLValue(name = "encapsulatedComponent__AssemblyContext")
        @SuppressWarnings("NullAway.Init") // set reflectively by the XML parser
        private String id;

        public String getId() {
            return id;
        }
    }
}
