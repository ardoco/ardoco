/* Licensed under MIT 2023-2026. */
package edu.kit.kastel.mcse.ardoco.core.api.models.code;

import java.io.Serial;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;

/**
 * Represents a control element in the code model.
 * Extends {@link ComputationalObject}.
 */
@JsonTypeName("ControlElement")
public final class ControlElement extends ComputationalObject {

    @Serial
    private static final long serialVersionUID = -2733651783905632198L;

    @JsonProperty
    private List<String> calleeNames;

    /**
     * Default constructor for Jackson.
     */
    @SuppressWarnings("unused")
    private ControlElement() {
        // Jackson
    }

    /**
     * Creates a new control element with the specified name.
     *
     * @param codeItemRepository the code item repository
     * @param name               the name of the control element
     */
    public ControlElement(CodeItemRepository codeItemRepository, String name) {
        super(codeItemRepository, name);
    }

    /**
     * Creates a new control element with the specified name and callee names.
     *
     * @param codeItemRepository the code item repository
     * @param name               the name of the control element
     * @param calleeNames        the names of the callees of this control element
     */
    public ControlElement(CodeItemRepository codeItemRepository, String name, List<String> calleeNames) {
        super(codeItemRepository, name);
        this.calleeNames = new ArrayList<>(calleeNames);
    }

    /**
     * Returns the names of the callees of this control element.
     *
     * @return unmodifiable list of callee names
     */
    public List<String> getCalleeNames() {
        return this.calleeNames != null ? List.copyOf(calleeNames) : List.of();
    }
}
