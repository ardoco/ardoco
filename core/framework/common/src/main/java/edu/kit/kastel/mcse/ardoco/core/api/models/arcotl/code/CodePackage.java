/* Licensed under MIT 2023-2024. */
package edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.code;

import java.util.Optional;
import java.util.SortedSet;
import java.util.TreeSet;

import com.fasterxml.jackson.annotation.JsonTypeName;

@JsonTypeName("CodePackage")
public final class CodePackage extends CodeModule {

    private static final long serialVersionUID = -5224168387357601602L;

    @SuppressWarnings("unused")
    private CodePackage() {
        // Jackson
    }

    public CodePackage(CodeItemRepository codeItemRepository, String name) {
        super(codeItemRepository, name, new TreeSet<>());
    }

    public CodePackage(CodeItemRepository codeItemRepository, String name, SortedSet<? extends CodeItem> content) {
        super(codeItemRepository, name, content);
    }

    public SortedSet<CodePackage> getSubpackages() {
        SortedSet<CodePackage> codePackages = new TreeSet<>();
        for (CodeItem packageElement : this.getContent()) {
            if (packageElement instanceof CodePackage codePackage) {
                codePackages.add(codePackage);
            }
        }
        return codePackages;
    }

    public SortedSet<CodeCompilationUnit> getCompilationUnits() {
        SortedSet<CodeCompilationUnit> compilationUnits = new TreeSet<>();
        for (CodeItem packageElement : this.getContent()) {
            if (packageElement instanceof CodeCompilationUnit compilationUnit) {
                compilationUnits.add(compilationUnit);
            }
        }
        return compilationUnits;
    }

    @Override
    public Optional<String> getType() {
        return Optional.of("Package");
    }

    @Override
    public SortedSet<CodePackage> getAllPackages() {
        SortedSet<CodePackage> result = new TreeSet<>();
        result.add(this);
        this.getContent().forEach(c -> result.addAll(c.getAllPackages()));
        return result;
    }
}
