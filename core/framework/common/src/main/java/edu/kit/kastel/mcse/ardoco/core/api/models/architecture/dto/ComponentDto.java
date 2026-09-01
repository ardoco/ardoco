/* Licensed under MIT 2026. */
package edu.kit.kastel.mcse.ardoco.core.api.models.architecture.dto;

import java.util.SortedSet;

import org.jspecify.annotations.Nullable;

import com.fasterxml.jackson.annotation.JsonProperty;

import edu.kit.kastel.mcse.ardoco.core.api.models.architecture.ArchitectureComponent;

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
public record ComponentDto(@JsonProperty String id, @JsonProperty String name, @JsonProperty @Nullable String componentType,
                           @JsonProperty SortedSet<String> subcomponentsIds, @JsonProperty SortedSet<String> providedInterfacesIds,
                           @JsonProperty SortedSet<String> requiredInterfacesIds) implements ArchitectureItemDto {
}
