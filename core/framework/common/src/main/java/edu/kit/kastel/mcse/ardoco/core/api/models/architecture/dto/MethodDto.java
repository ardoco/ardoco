/* Licensed under MIT 2026. */
package edu.kit.kastel.mcse.ardoco.core.api.models.architecture.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import edu.kit.kastel.mcse.ardoco.core.api.models.architecture.ArchitectureMethod;

/**
 * DTO for an {@link ArchitectureMethod}.
 *
 * @param id   unique identifier
 * @param name method name
 */
public record MethodDto(@JsonProperty String id, @JsonProperty String name) implements ArchitectureItemDto {
}
