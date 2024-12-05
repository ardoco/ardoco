/* Licensed under MIT 2023. */
package edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.code;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeName;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({ @JsonSubTypes.Type(value = ControlElement.class, name = "ControlElement") })
@JsonTypeName("ComputationalObject")
public sealed class ComputationalObject extends CodeItem permits ControlElement {

    private static final long serialVersionUID = -6879811567216500291L;

    ComputationalObject() {
        // Jackson
    }

    public ComputationalObject(CodeItemRepository codeItemRepository, String name) {
        super(codeItemRepository, name);
    }

}
