/* Licensed under MIT 2026. */
package edu.kit.kastel.mcse.ardoco.core.api.models.architecture.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DTO for an architecture model.
 *
 * @param id                         unique identifier of the model
 * @param architectureItemRepository container holding all architecture items and root content IDs
 */
public record ArchitectureModelDto(@JsonProperty String id, @JsonProperty ArchitectureItemRepository architectureItemRepository) {
}
