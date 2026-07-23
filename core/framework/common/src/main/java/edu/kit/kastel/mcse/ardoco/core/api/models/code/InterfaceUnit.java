/* Licensed under MIT 2023-2026. */
package edu.kit.kastel.mcse.ardoco.core.api.models.code;

import java.io.Serial;
import java.util.SortedSet;

import com.fasterxml.jackson.annotation.JsonTypeName;

/**
 * Represents an interface unit in the code model. Contains code items representing the contents of an interface, such as method signatures.
 */
@JsonTypeName("InterfaceUnit")
public final class InterfaceUnit extends Datatype {

    @Serial
    private static final long serialVersionUID = 7746781256077022392L;

    @SuppressWarnings("unused")
    private InterfaceUnit() {
        // Jackson
    }

    /**
     * Creates a new interface unit with the specified name and content.
     *
     * @param codeItemRepository the code item repository
     * @param name               the name of the interface unit
     * @param content            the content of the interface unit
     */
    public InterfaceUnit(CodeItemRepository codeItemRepository, String name, SortedSet<? extends CodeItem> content) {
        super(codeItemRepository, name, content);
    }

    /**
     * Creates a new interface unit with the specified name, content, and source location.
     *
     * @param codeItemRepository the code item repository
     * @param name               the name of the interface unit
     * @param content            the content of the interface unit
     * @param startLine          the 1-indexed start line in the source file, or -1 if unknown
     * @param endLine            the 1-indexed end line in the source file, or -1 if unknown
     */
    public InterfaceUnit(CodeItemRepository codeItemRepository, String name, SortedSet<? extends CodeItem> content, int startLine, int endLine) {
        super(codeItemRepository, name, content, startLine, endLine);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        return o instanceof InterfaceUnit && super.equals(o);
    }
}
