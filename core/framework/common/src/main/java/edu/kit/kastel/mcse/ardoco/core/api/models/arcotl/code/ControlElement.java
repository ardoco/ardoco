/* Licensed under MIT 2023-2024. */
package edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.code;

import com.fasterxml.jackson.annotation.JsonTypeName;

@JsonTypeName("ControlElement")
public final class ControlElement extends ComputationalObject {

    private static final long serialVersionUID = -2733651783905632198L;

    @SuppressWarnings("unused")
    private ControlElement() {
        // Jackson
    }

    public ControlElement(CodeItemRepository codeItemRepository, String name) {
        super(codeItemRepository, name);
    }
}
