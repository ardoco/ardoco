package edu.kit.kastel.mcse.ardoco.core.api.models.code;

import java.io.Serial;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;

/**
 * Represents a file that is part of the code-project but not a source file.
 */
@JsonTypeName("NonSourceFile")
public final class NonSourceFile extends CodeItem {

    @Serial
    private static final long serialVersionUID = -2769321650037749217L;

    @JsonProperty
    private List<String> pathElements;

    @JsonProperty
    private String extension;

    @JsonProperty
    private String predictedFileType;

    /**
     * Default constructor for Jackson.
     */
    @SuppressWarnings("unused")
    private NonSourceFile() {
        // Jackson
    }

    /**
     * Creates a new non-source file.
     *
     * @param codeItemRepository the code item repository
     * @param name               the file name
     * @param pathElements       the relative path elements including the file name
     * @param extension          the file extension without leading dot
     * @param predictedFileType  the predicted file type
     */
    public NonSourceFile(CodeItemRepository codeItemRepository, String name, List<String> pathElements, String extension, String predictedFileType) {
        super(codeItemRepository, name);
        this.pathElements = List.copyOf(pathElements);
        this.extension = extension;
        this.predictedFileType = predictedFileType;
    }

    /**
     * Creates a non-source file from a relative path string (forward-slash separated, including extension).
     *
     * @param codeItemRepository the code item repository
     * @param relativePath       the relative path using '/' as separator
     * @param predictedFileType  the predicted file type
     * @return the non-source file
     */
    public static NonSourceFile fromRelativePath(CodeItemRepository codeItemRepository, String relativePath, String predictedFileType) {
        int lastSlash = relativePath.lastIndexOf('/');
        int lastDot = relativePath.lastIndexOf('.');
        String name = relativePath.substring(lastSlash + 1, lastDot > lastSlash ? lastDot : relativePath.length());
        List<String> pathElements = lastSlash >= 0 ? List.of(relativePath.substring(0, lastSlash).split("/")) : List.of();
        String extension = lastDot > lastSlash ? relativePath.substring(lastDot + 1) : "";
        return new NonSourceFile(codeItemRepository, name, pathElements, extension, predictedFileType);
    }

    /**
     * Returns the path elements of this non-source file.
     *
     * @return the path elements
     */
    public List<String> getPathElements() {
        return new ArrayList<>(this.pathElements);
    }

    /**
     * Returns the file extension without leading dot.
     *
     * @return the extension
     */
    public String getExtension() {
        return this.extension;
    }

    /**
     * Returns the predicted file type.
     *
     * @return the predicted file type
     */
    public String getPredictedFileType() {
        return this.predictedFileType;
    }

    /**
     * Returns the full path of this non-source file.
     *
     * @return the relative path
     */
    public String getPath() {
        StringBuilder pathBuilder = new StringBuilder();
        for (String pathElement : this.pathElements) {
            pathBuilder.append(pathElement).append("/");
        }
        String ending = "";
        if (!this.extension.isEmpty()) {
            ending = "." + this.extension;
        }
        pathBuilder.append(this.getName()).append(ending);
        return pathBuilder.toString();
    }

    @Override
    public Optional<String> getType() {
        return Optional.of(this.predictedFileType);
    }

    @Override
    public String toString() {
        return this.getPath();
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof NonSourceFile that && Objects.equals(this.getPath(), that.getPath());
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.getPath());
    }
}
