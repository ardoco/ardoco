/* Licensed under MIT 2026. */
package edu.kit.kastel.mcse.ardoco.core.api.models.dto;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 * Sealed base type for architecture item DTOs, discriminated by a "type" property.
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({ @JsonSubTypes.Type(value = ComponentDto.class, name = "Component"), @JsonSubTypes.Type(value = InterfaceDto.class, name = "Interface"),
        @JsonSubTypes.Type(value = MethodDto.class, name = "Method") })
public sealed interface ArchitectureItemDto permits ComponentDto, InterfaceDto, MethodDto {
    String id();
}
