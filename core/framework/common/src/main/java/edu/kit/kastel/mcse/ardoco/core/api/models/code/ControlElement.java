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
    private int startLine = -1;
    
    @JsonProperty
    private int endLine = -1;

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
     * Creates a new control element with the specified name and source location.
     *
     * @param codeItemRepository the code item repository
     * @param name               the name of the control element
     * @param startLine          the 1-indexed start line in the source file, or -1 if unknown
     * @param endLine            the 1-indexed end line in the source file, or -1 if unknown
     * @param calleeNames        the names of the callees of this control element
     */
    public ControlElement(CodeItemRepository codeItemRepository, String name, int startLine, int endLine, List<String> calleeNames) {
        super(codeItemRepository, name);
        this.startLine = startLine;
        this.endLine = endLine;
        this.calleeNames = new ArrayList<>(calleeNames);
    }

    /**
     * Returns the 1-indexed start line of this element in its source file, or -1 if unknown.
     *
     * @return the start line
     */
    public int getStartLine() {
        return startLine;
    }

    /**
     * Returns the 1-indexed end line of this element in its source file, or -1 if unknown.
     *
     * @return the end line
     */
    public int getEndLine() {
        return endLine;
    }

    /**
     * Returns the names of the callees of this control element.
     *
     * @return unmodifiable list of callee names
     */
    public List<String> getCalleeNames() {
        return this.calleeNames != null ? List.copyOf(calleeNames) : List.of();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ControlElement that) || !super.equals(o)) {
            return false;
        }
        return this.startLine == that.startLine && this.endLine == that.endLine && (this.calleeNames != null ? this.calleeNames.equals(that.calleeNames) : that.calleeNames == null);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + this.startLine;
        result = 31 * result + this.endLine;
        result = 31 * result + (this.calleeNames != null ? this.calleeNames.hashCode() : 0);
        return result;
    }
}
