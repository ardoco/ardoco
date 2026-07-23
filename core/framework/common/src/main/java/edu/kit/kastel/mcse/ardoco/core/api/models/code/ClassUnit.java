/* Licensed under MIT 2023-2026. */
package edu.kit.kastel.mcse.ardoco.core.api.models.code;

import java.io.Serial;
import java.util.SortedSet;

import com.fasterxml.jackson.annotation.JsonTypeName;

/**
 * Represents a class unit in the code model. Contains code items representing the contents of a class, such as methods and fields.
 */
@JsonTypeName("ClassUnit")
public final class ClassUnit extends Datatype {

    @Serial
    private static final long serialVersionUID = 354013115794534271L;

    /**
     * Default constructor for Jackson.
     */
    @SuppressWarnings("unused")
    private ClassUnit() {
        // Jackson
    }

    /**
     * Creates a new class unit with the specified name and content.
     *
     * @param codeItemRepository the code item repository
     * @param name               the name of the class unit
     * @param content            the content of the class unit
     */
    public ClassUnit(CodeItemRepository codeItemRepository, String name, SortedSet<? extends CodeItem> content) {
        super(codeItemRepository, name, content);
    }

    /**
     * Creates a new class unit with the specified name, content, and source location.
     *
     * @param codeItemRepository the code item repository
     * @param name               the name of the class unit
     * @param content            the content of the class unit
     * @param startLine          the 1-indexed start line in the source file, or -1 if unknown
     * @param endLine            the 1-indexed end line in the source file, or -1 if unknown
     */
    public ClassUnit(CodeItemRepository codeItemRepository, String name, SortedSet<? extends CodeItem> content, int startLine, int endLine) {
        super(codeItemRepository, name, content, startLine, endLine);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        return o instanceof ClassUnit && super.equals(o);
    }
}
