/* Licensed under MIT 2023-2026. */
package edu.kit.kastel.mcse.ardoco.core.api.models.code;

import java.io.Serial;

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
     */
    public ControlElement(CodeItemRepository codeItemRepository, String name, int startLine, int endLine) {
        super(codeItemRepository, name);
        this.startLine = startLine;
        this.endLine = endLine;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ControlElement that) || !super.equals(o)) {
            return false;
        }
        return this.startLine == that.startLine && this.endLine == that.endLine;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + this.startLine;
        return 31 * result + this.endLine;
    }
}
