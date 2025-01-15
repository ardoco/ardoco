/* Licensed under MIT 2023-2025. */
package edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.code;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.SortedSet;
import java.util.TreeSet;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;

/**
 * A compilation unit of a code model. A possible candidate for the code endpoint of a trace link that connects corresponding elements of an architecture model
 * and a code model.
 */
@JsonTypeName("CodeCompilationUnit")
public final class CodeCompilationUnit extends CodeModule {

    private static final long serialVersionUID = 6749513760670983294L;

    @JsonProperty
    private List<String> pathElements;
    @JsonProperty
    private String extension;
    @JsonProperty
    private String language;

    @SuppressWarnings("unused")
    private CodeCompilationUnit() {
        // Jackson
    }

    public CodeCompilationUnit(CodeItemRepository codeItemRepository, String name, SortedSet<? extends CodeItem> content, List<String> pathElements,
            String extension, String language) {
        super(codeItemRepository, name, content);
        this.pathElements = new ArrayList<>(pathElements);
        this.extension = extension;
        this.language = language;
    }

    public String getLanguage() {
        return this.language;
    }

    @Override
    public List<Datatype> getAllDataTypes() {
        List<Datatype> result = new ArrayList<>();
        this.getContent().forEach(c -> result.addAll(c.getAllDataTypes()));
        return result;
    }

    @Override
    public SortedSet<CodeCompilationUnit> getAllCompilationUnits() {
        SortedSet<CodeCompilationUnit> result = new TreeSet<>();
        result.add(this);
        return result;
    }

    public List<String> getParentPackageNames() {
        List<String> parents = new ArrayList<>();
        CodeModule parent = this;
        while (parent.hasParent()) {
            parent = parent.getParent();
            if (parent instanceof CodePackage) {
                parents.addFirst(parent.getName());
            }
        }
        return parents;
    }

    public List<String> getPathElements() {
        return new ArrayList<>(this.pathElements);
    }

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
        // Assumption mostly one class per unit
        var content = this.getContent().stream().filter(it -> this.getName().contains(it.getName())).findFirst().orElse(null);
        if (content instanceof ClassUnit) {
            return Optional.of("Class");
        }
        if (content instanceof InterfaceUnit) {
            return Optional.of("Interface");
        }
        if (this.getPath().endsWith("package-info.java")) {
            return Optional.of("PackageInfo");
        }
        if (this.getPath().endsWith(".java")) {
            // Default to Class
            return Optional.of("Class");
        }
        if (ProgrammingLanguages.SHELL.equals(this.getLanguage())) {
            return Optional.of("ShellScript");
        }
        return Optional.empty();
    }

    @Override
    public String toString() {
        return this.getPath();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof CodeCompilationUnit that) || !super.equals(o) || !Objects.equals(this.pathElements, that.pathElements) || !Objects.equals(
                this.extension, that.extension)) {
            return false;
        }
        return this.language == that.language;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (this.pathElements != null ? this.pathElements.hashCode() : 0);
        result = 31 * result + (this.extension != null ? this.extension.hashCode() : 0);
        return 31 * result + (this.language != null ? this.language.hashCode() : 0);
    }
}
