/* Licensed under MIT 2026. */
package edu.kit.kastel.mcse.ardoco.core.api.models.architecture.dto;

import java.util.List;
import java.util.SortedMap;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Container for the flat repository map and root-level content IDs.
 *
 * @param repository flat map of all architecture items by ID
 * @param content    IDs of the root-level content items
 */
public record ArchitectureItemRepository(@JsonProperty SortedMap<String, ArchitectureItemDto> repository, @JsonProperty List<String> content) {
}
