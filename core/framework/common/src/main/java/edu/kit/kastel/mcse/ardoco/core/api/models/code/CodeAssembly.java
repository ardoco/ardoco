/* Licensed under MIT 2023-2026. */
package edu.kit.kastel.mcse.ardoco.core.api.models.code;

import java.io.Serial;
import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;

/**
 * Represents a code assembly in the code model.
 * Specialized type of {@link CodeModule}.
 */
@JsonTypeName("CodeAssembly")
public final class CodeAssembly extends CodeModule {

    @Serial
    private static final long serialVersionUID = 3082912967900986071L;

    @JsonProperty
    private String language;

    @JsonProperty
    private List<String> importedModuleNames;

    /**
     * Default constructor for Jackson.
     */
    @SuppressWarnings("unused")
    private CodeAssembly() {
        // Jackson
    }

    /**
     * Constructs a new CodeAssembly with the given repository, name, and content.
     *
     * @param codeItemRepository the code item repository
     * @param name               the name of the assembly
     * @param content            the content of the assembly
     */
    public CodeAssembly(CodeItemRepository codeItemRepository, String name, SortedSet<? extends CodeItem> content) {
        super(codeItemRepository, name, content);
    }

    /**
     * Constructs a new CodeAssembly with language information.
     *
     * @param codeItemRepository the code item repository
     * @param name               the name of the assembly
     * @param content            the content of the assembly
     * @param language           the programming language
     */
    public CodeAssembly(CodeItemRepository codeItemRepository, String name, SortedSet<? extends CodeItem> content, String language) {
        super(codeItemRepository, name, content);
        this.language = language;
    }

    /**
     * Returns the programming language of this assembly.
     *
     * @return the language string
     */
    public String getLanguage() {
        return language;
    }

    /**
     * Returns the list of imported module/package names for this assembly (file-level imports).
     *
     * @return list of imported module names
     */
    public List<String> getImportedModuleNames() {
        return this.importedModuleNames != null ? new ArrayList<>(this.importedModuleNames) : List.of();
    }
}
