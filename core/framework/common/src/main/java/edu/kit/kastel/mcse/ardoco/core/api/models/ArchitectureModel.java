/* Licensed under MIT 2023-2026. */
package edu.kit.kastel.mcse.ardoco.core.api.models;

import java.util.List;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import edu.kit.kastel.mcse.ardoco.core.api.models.architecture.ArchitectureComponent;
import edu.kit.kastel.mcse.ardoco.core.api.models.architecture.ArchitectureInterface;
import edu.kit.kastel.mcse.ardoco.core.api.models.architecture.ArchitectureItem;
import edu.kit.kastel.mcse.ardoco.core.api.models.architecture.ArchitectureMethod;
import edu.kit.kastel.mcse.ardoco.core.architecture.NoHashCodeEquals;

/**
 * Represents an architecture model.
 */
@NoHashCodeEquals
public abstract sealed class ArchitectureModel extends Model permits ArchitectureComponentModel, ArchitectureModelWithComponentsAndInterfaces {

    /**
     * Returns the content of the architecture model.
     *
     * @return list of architecture items
     */
    @Override
    public abstract List<? extends ArchitectureItem> getContent();

    /**
     * Returns the endpoints of this model.
     *
     * @return list of architecture items
     */
    @Override
    public abstract List<? extends ArchitectureItem> getEndpoints();

    /**
     * Creates a DTO for this architecture model suitable for JSON serialization.
     *
     * @return architecture model DTO
     */
    public ArchitectureModelDto createArchitectureModelDto() {
        SortedMap<String, ArchitectureItemDto> repository = new TreeMap<>();
        List<? extends ArchitectureItem> content = getContent();
        for (ArchitectureItem item : content) {
            collectItem(item, repository);
        }
        List<String> contentIds = content.stream().map(ArchitectureItem::getId).toList();
        return new ArchitectureModelDto(getId(), new ArchitectureItemRepository(repository, contentIds));
    }

    private static void collectItem(ArchitectureItem item, SortedMap<String, ArchitectureItemDto> repository) {
        if (repository.containsKey(item.getId())) {
            return;
        }
        switch (item) {
            case ArchitectureComponent component -> {
                SortedSet<String> subcomponentIds = component.getSubcomponents()
                        .stream()
                        .map(ArchitectureComponent::getId)
                        .collect(Collectors.toCollection(TreeSet::new));
                SortedSet<String> providedIds = component.getProvidedInterfaces()
                        .stream()
                        .map(ArchitectureInterface::getId)
                        .collect(Collectors.toCollection(TreeSet::new));
                SortedSet<String> requiredIds = component.getRequiredInterfaces()
                        .stream()
                        .map(ArchitectureInterface::getId)
                        .collect(Collectors.toCollection(TreeSet::new));
                repository.put(component.getId(), new ComponentDto(component.getId(), component.getName(), component.getType().orElse(null), subcomponentIds,
                        providedIds, requiredIds));
                for (ArchitectureComponent sub : component.getSubcomponents()) {
                    collectItem(sub, repository);
                }
                for (ArchitectureInterface iface : component.getProvidedInterfaces()) {
                    collectItem(iface, repository);
                }
                for (ArchitectureInterface iface : component.getRequiredInterfaces()) {
                    collectItem(iface, repository);
                }
            }
            case ArchitectureInterface iface -> {
                SortedSet<String> methodIds = iface.getMethodSignatures()
                        .stream()
                        .map(ArchitectureMethod::getId)
                        .collect(Collectors.toCollection(TreeSet::new));
                repository.put(iface.getId(), new InterfaceDto(iface.getId(), iface.getName(), methodIds));
                for (ArchitectureMethod method : iface.getMethodSignatures()) {
                    collectItem(method, repository);
                }
            }
            case ArchitectureMethod method -> {
                repository.put(method.getId(), new MethodDto(method.getId(), method.getName()));
            }
        }
    }

    /**
     * DTO for an architecture model.
     */
    public static final class ArchitectureModelDto {
        @JsonProperty
        private final String id;
        @JsonProperty
        private final ArchitectureItemRepository architectureItemRepository;

        /**
         * @param id                         unique identifier of the model
         * @param architectureItemRepository container holding all architecture items and root content IDs
         */
        public ArchitectureModelDto(@JsonProperty("id") String id,
                @JsonProperty("architectureItemRepository") ArchitectureItemRepository architectureItemRepository) {
            this.id = id;
            this.architectureItemRepository = architectureItemRepository;
        }

        public String id() {
            return id;
        }

        public ArchitectureItemRepository architectureItemRepository() {
            return architectureItemRepository;
        }
    }

    /**
     * Container for the flat repository map and root-level content IDs.
     */
    public static final class ArchitectureItemRepository {
        @JsonProperty
        private final SortedMap<String, ArchitectureItemDto> repository;
        @JsonProperty
        private final List<String> content;

        /**
         * @param repository flat map of all architecture items by ID
         * @param content    IDs of the root-level content items
         */
        public ArchitectureItemRepository(@JsonProperty("repository") SortedMap<String, ArchitectureItemDto> repository,
                @JsonProperty("content") List<String> content) {
            this.repository = repository;
            this.content = content;
        }

        public SortedMap<String, ArchitectureItemDto> repository() {
            return repository;
        }

        public List<String> content() {
            return content;
        }
    }

    /**
     * Sealed base type for architecture item DTOs, discriminated by a "type" property.
     */
    @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
    @JsonSubTypes({ @JsonSubTypes.Type(value = ComponentDto.class, name = "Component"), @JsonSubTypes.Type(value = InterfaceDto.class, name = "Interface"),
            @JsonSubTypes.Type(value = MethodDto.class, name = "Method") })
    public sealed interface ArchitectureItemDto permits ComponentDto, InterfaceDto, MethodDto {
        String id();
    }

    /**
     * DTO for an {@link ArchitectureComponent}.
     */
    public static final class ComponentDto implements ArchitectureItemDto {
        @JsonProperty
        private final String id;
        @JsonProperty
        private final String name;
        @JsonProperty
        private final String componentType;
        @JsonProperty
        private final SortedSet<String> subcomponentsIds;
        @JsonProperty
        private final SortedSet<String> providedInterfacesIds;
        @JsonProperty
        private final SortedSet<String> requiredInterfacesIds;

        /**
         * @param id                    unique identifier
         * @param name                  component name
         * @param componentType         component type string, may be null
         * @param subcomponentsIds      IDs of direct subcomponents
         * @param providedInterfacesIds IDs of provided interfaces
         * @param requiredInterfacesIds IDs of required interfaces
         */
        public ComponentDto(@JsonProperty("id") String id, @JsonProperty("name") String name, @JsonProperty("componentType") String componentType,
                @JsonProperty("subcomponentsIds") SortedSet<String> subcomponentsIds,
                @JsonProperty("providedInterfacesIds") SortedSet<String> providedInterfacesIds,
                @JsonProperty("requiredInterfacesIds") SortedSet<String> requiredInterfacesIds) {
            this.id = id;
            this.name = name;
            this.componentType = componentType;
            this.subcomponentsIds = subcomponentsIds;
            this.providedInterfacesIds = providedInterfacesIds;
            this.requiredInterfacesIds = requiredInterfacesIds;
        }

        @Override
        public String id() {
            return id;
        }

        public String name() {
            return name;
        }

        public String componentType() {
            return componentType;
        }

        public SortedSet<String> subcomponentsIds() {
            return subcomponentsIds;
        }

        public SortedSet<String> providedInterfacesIds() {
            return providedInterfacesIds;
        }

        public SortedSet<String> requiredInterfacesIds() {
            return requiredInterfacesIds;
        }
    }

    /**
     * DTO for an {@link ArchitectureInterface}.
     */
    public static final class InterfaceDto implements ArchitectureItemDto {
        @JsonProperty
        private final String id;
        @JsonProperty
        private final String name;
        @JsonProperty
        private final SortedSet<String> methodSignaturesIds;

        /**
         * @param id                  unique identifier
         * @param name                interface name
         * @param methodSignaturesIds IDs of the interface's method signatures
         */
        public InterfaceDto(@JsonProperty("id") String id, @JsonProperty("name") String name,
                @JsonProperty("methodSignaturesIds") SortedSet<String> methodSignaturesIds) {
            this.id = id;
            this.name = name;
            this.methodSignaturesIds = methodSignaturesIds;
        }

        @Override
        public String id() {
            return id;
        }

        public String name() {
            return name;
        }

        public SortedSet<String> methodSignaturesIds() {
            return methodSignaturesIds;
        }
    }

    /**
     * DTO for an {@link ArchitectureMethod}.
     */
    public static final class MethodDto implements ArchitectureItemDto {
        @JsonProperty
        private final String id;
        @JsonProperty
        private final String name;

        /**
         * @param id   unique identifier
         * @param name method name
         */
        public MethodDto(@JsonProperty("id") String id, @JsonProperty("name") String name) {
            this.id = id;
            this.name = name;
        }

        @Override
        public String id() {
            return id;
        }

        public String name() {
            return name;
        }
    }
}
