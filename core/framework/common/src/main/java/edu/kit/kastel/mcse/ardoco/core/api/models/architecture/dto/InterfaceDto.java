/* Licensed under MIT 2026. */
package edu.kit.kastel.mcse.ardoco.core.api.models.architecture.dto;

import java.util.SortedSet;

import com.fasterxml.jackson.annotation.JsonProperty;

import edu.kit.kastel.mcse.ardoco.core.api.models.architecture.ArchitectureInterface;

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
