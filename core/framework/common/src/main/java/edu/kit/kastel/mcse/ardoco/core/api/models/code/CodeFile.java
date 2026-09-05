/* Licensed under MIT 2025-2026. */
package edu.kit.kastel.mcse.ardoco.core.api.models.code;

import java.io.Serial;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.jspecify.annotations.Nullable;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import edu.kit.kastel.mcse.ardoco.core.api.entity.CodeEntity;

/**
 * Represents a file that is part of the code model.
 * <p>
 * A code file always stores its path information. If a language-specific extractor produced a {@link CodeCompilationUnit} for this file, this code file
 * additionally references that compilation unit.
 */
public final class CodeFile extends CodeEntity {

    @Serial
    private static final long serialVersionUID = -5361965865859960435L;

    @JsonProperty
    private List<String> pathElements;

    @JsonProperty
    private String extension;

    @JsonProperty
    @Nullable
    private String compilationUnitId;

    /**
     * Default constructor for Jackson.
     */
    @SuppressWarnings("unused")
    private CodeFile() {
        // Jackson
        super(null);
    }

    /**
     * Creates a new code file.
     *
     * @param name              the file name without extension
     * @param pathElements      the relative path elements excluding the file name
     * @param extension         the file extension without leading dot
     * @param compilationUnitId the id of the corresponding compilation unit, or {@code null}
     */
    public CodeFile(String name, List<String> pathElements, String extension, String compilationUnitId) {
        super(name);
        this.pathElements = List.copyOf(pathElements);
        this.extension = Objects.requireNonNullElse(extension, "");
        this.compilationUnitId = compilationUnitId;
    }

    /**
     * Creates a new code file that references a compilation unit.
     *
     * @param relativePath    the relative path using '/' as separator
     * @param compilationUnit the corresponding compilation unit
     * @return the code file
     */
    public static CodeFile fromRelativePath(String relativePath, CodeCompilationUnit compilationUnit) {
        Objects.requireNonNull(compilationUnit);
        return CodeFile.fromRelativePath(relativePath, compilationUnit.getId());
    }

    /**
     * Creates a new code file that optionally references a compilation unit id.
     *
     * @param relativePath      the relative path using '/' as separator
     * @param compilationUnitId the id of the corresponding compilation unit, or {@code null}
     * @return the code file
     */
    public static CodeFile fromRelativePath(String relativePath, String compilationUnitId) {
        Objects.requireNonNull(relativePath);

        int lastSlash = relativePath.lastIndexOf('/');
        int lastDot = relativePath.lastIndexOf('.');

        String name = relativePath.substring(lastSlash + 1, lastDot > lastSlash ? lastDot : relativePath.length());
        List<String> pathElements = lastSlash >= 0 ? List.of(relativePath.substring(0, lastSlash).split("/")) : List.of();
        String extension = lastDot > lastSlash ? relativePath.substring(lastDot + 1) : "";

        return new CodeFile(name, pathElements, extension, compilationUnitId);
    }

    /**
     * Creates a new code file without a corresponding compilation unit.
     *
     * @param relativePath the relative path using '/' as separator
     * @return the code file
     */
    public static CodeFile fromRelativePath(String relativePath) {
        return CodeFile.fromRelativePath(relativePath, (String) null);
    }

    /**
     * Returns the path elements of this file, excluding the file name.
     *
     * @return the path elements
     */
    public List<String> getPathElements() {
        return new ArrayList<>(this.pathElements);
    }

    /**
     * Returns the file extension without leading dot.
     *
     * @return the file extension
     */
    public String getExtension() {
        return this.extension;
    }

    /**
     * Returns the id of the corresponding compilation unit.
     *
     * @return the compilation unit id, or an empty optional
     */
    public Optional<String> getCompilationUnitId() {
        return Optional.ofNullable(this.compilationUnitId);
    }

    /**
     * Returns whether this file references a compilation unit.
     *
     * @return {@code true} if a compilation unit is referenced
     */
    @JsonIgnore
    public boolean hasCompilationUnit() {
        return this.compilationUnitId != null;
    }

    /**
     * Resolves the referenced compilation unit using the given code item repository.
     *
     * @param codeItemRepository the repository used for resolving the referenced item
     * @return the referenced compilation unit, or an empty optional
     */
    public Optional<CodeCompilationUnit> getCompilationUnit(CodeItemRepository codeItemRepository) {
        if (this.compilationUnitId == null) {
            return Optional.empty();
        }

        CodeItem codeItem = codeItemRepository.getCodeItem(this.compilationUnitId);
        if (codeItem instanceof CodeCompilationUnit compilationUnit) {
            return Optional.of(compilationUnit);
        }

        return Optional.empty();
    }

    /**
     * Sets the corresponding compilation unit.
     *
     * @param compilationUnit the compilation unit to reference
     */
    public void setCompilationUnit(CodeCompilationUnit compilationUnit) {
        this.compilationUnitId = Objects.requireNonNull(compilationUnit).getId();
    }

    /**
     * Removes the reference to a compilation unit.
     */
    public void clearCompilationUnit() {
        this.compilationUnitId = null;
    }

    /**
     * Returns the full relative path of this file.
     *
     * @return the relative path
     */
    public String getPath() {
        StringBuilder pathBuilder = new StringBuilder();
        for (String pathElement : this.pathElements) {
            pathBuilder.append(pathElement).append("/");
        }

        pathBuilder.append(this.getName());

        if (!this.extension.isEmpty()) {
            pathBuilder.append(".").append(this.extension);
        }

        return pathBuilder.toString();
    }

    @Override
    public String toString() {
        return this.getPath();
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof CodeFile that && Objects.equals(this.getPath(), that.getPath()) && Objects.equals(this.compilationUnitId, that.compilationUnitId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.getPath(), this.compilationUnitId);
    }
}
