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
        List<String> contentIds = getContent().stream().map(ArchitectureItem::getId).toList();
        for (ArchitectureItem item : getContent()) {
            collectItem(item, repository);
        }
        return new ArchitectureModelDto(getId(), new ArchitectureItemRepository(repository, contentIds));
    }

    private static void collectItem(ArchitectureItem item, SortedMap<String, ArchitectureItemDto> repository) {
        if (repository.containsKey(item.getId())) {
            return;
        }
        if (item instanceof ArchitectureComponent component) {
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
        } else if (item instanceof ArchitectureInterface iface) {
            SortedSet<String> methodIds = iface.getMethodSignatures().stream().map(ArchitectureMethod::getId).collect(Collectors.toCollection(TreeSet::new));
            repository.put(iface.getId(), new InterfaceDto(iface.getId(), iface.getName(), methodIds));
            for (ArchitectureMethod method : iface.getMethodSignatures()) {
                collectItem(method, repository);
            }
        } else if (item instanceof ArchitectureMethod method) {
            repository.put(method.getId(), new MethodDto(method.getId(), method.getName()));
        }
    }

    /**
     * DTO for an architecture model.
     *
     * @param id                         unique identifier of the model
     * @param architectureItemRepository container holding all architecture items and root content IDs
     */
    public record ArchitectureModelDto(@JsonProperty String id, @JsonProperty ArchitectureItemRepository architectureItemRepository) {
    }

    /**
     * Container for the flat repository map and root-level content IDs.
     *
     * @param repository flat map of all architecture items by ID
     * @param content    IDs of the root-level content items
     */
    public record ArchitectureItemRepository(@JsonProperty SortedMap<String, ArchitectureItemDto> repository, @JsonProperty List<String> content) {
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
     *
     * @param id                    unique identifier
     * @param name                  component name
     * @param componentType         component type string, may be null
     * @param subcomponentsIds      IDs of direct subcomponents
     * @param providedInterfacesIds IDs of provided interfaces
     * @param requiredInterfacesIds IDs of required interfaces
     */
    public record ComponentDto(@JsonProperty String id, @JsonProperty String name, @JsonProperty String componentType,
                               @JsonProperty SortedSet<String> subcomponentsIds, @JsonProperty SortedSet<String> providedInterfacesIds,
                               @JsonProperty SortedSet<String> requiredInterfacesIds) implements ArchitectureItemDto {
    }

    /**
     * DTO for an {@link ArchitectureInterface}.
     *
     * @param id                  unique identifier
     * @param name                interface name
     * @param methodSignaturesIds IDs of the interface's method signatures
     */
    public record InterfaceDto(@JsonProperty String id, @JsonProperty String name, @JsonProperty SortedSet<String> methodSignaturesIds) implements
            ArchitectureItemDto {
    }

    /**
     * DTO for an {@link ArchitectureMethod}.
     *
     * @param id   unique identifier
     * @param name method name
     */
    public record MethodDto(@JsonProperty String id, @JsonProperty String name) implements ArchitectureItemDto {
    }
}
