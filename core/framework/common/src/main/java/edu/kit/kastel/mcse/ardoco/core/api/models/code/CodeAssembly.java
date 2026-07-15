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
    private List<String> pathElements;

    @JsonProperty
    private String extension;

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
     * Constructs a new CodeAssembly with language, file path, and import information.
     *
     * @param codeItemRepository  the code item repository
     * @param name                the name of the assembly
     * @param content             the content of the assembly
     * @param language            the programming language
     * @param pathElements        the directory path segments leading to this file
     * @param extension           the file extension (without leading dot)
     * @param importedModuleNames the list of imported module/package names for this assembly
     */
    public CodeAssembly(CodeItemRepository codeItemRepository, String name, SortedSet<? extends CodeItem> content, String language, List<String> pathElements,
            String extension, List<String> importedModuleNames) {
        super(codeItemRepository, name, content);
        this.language = language;
        this.pathElements = new ArrayList<>(pathElements);
        this.extension = extension;
        this.importedModuleNames = new ArrayList<>(importedModuleNames);
    }

    /**
     * Creates a CodeAssembly from a relative path string (forward-slash separated, including extension).
     *
     * @param codeItemRepository  the code item repository
     * @param content             the content of the assembly
     * @param language            the programming language
     * @param relativePath        relative path string, e.g. {@code "src/foo/Bar.cpp"}
     * @param importedModuleNames the list of imported module/package names for this assembly
     * @return a new CodeAssembly with name, pathElements, and extension derived from the path
     */
    public static CodeAssembly fromRelativePath(CodeItemRepository codeItemRepository, SortedSet<? extends CodeItem> content, String language,
            String relativePath, List<String> importedModuleNames) {
        int lastSlash = relativePath.lastIndexOf('/');
        int lastDot = relativePath.lastIndexOf('.');
        String name = relativePath.substring(lastSlash + 1, lastDot > lastSlash ? lastDot : relativePath.length());
        List<String> pathElements = lastSlash >= 0 ? List.of(relativePath.substring(0, lastSlash).split("/")) : List.of();
        String extension = lastDot > lastSlash ? relativePath.substring(lastDot + 1) : "";
        return new CodeAssembly(codeItemRepository, name, content, language, pathElements, extension, importedModuleNames);
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
     * Returns the directory path segments of this assembly.
     * Returns an empty list for structural assemblies (e.g. C++ namespaces) that have no file path.
     * Use {@link #hasFilePath()} to distinguish the two cases.
     *
     * @return copy of the path elements list
     */
    public List<String> getPathElements() {
        return this.pathElements != null ? new ArrayList<>(this.pathElements) : List.of();
    }

    /**
     * Returns the file extension of this assembly.
     * Returns an empty string for structural assemblies that have no file path.
     * Use {@link #hasFilePath()} to distinguish the two cases.
     *
     * @return the file extension without leading dot
     */
    public String getExtension() {
        return this.extension != null ? this.extension : "";
    }

    /**
     * Returns whether this assembly corresponds to a source file (has path information).
     *
     * @return true if path elements are present
     */
    public boolean hasFilePath() {
        return this.pathElements != null;
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
