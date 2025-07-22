/* Licensed under MIT 2025. */
package edu.kit.kastel.mcse.ardoco.core.api.models;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import edu.kit.kastel.mcse.ardoco.core.api.models.code.CodeCompilationUnit;
import edu.kit.kastel.mcse.ardoco.core.api.models.code.CodeItem;
import edu.kit.kastel.mcse.ardoco.core.api.models.code.CodeItemRepository;
import edu.kit.kastel.mcse.ardoco.core.architecture.Deterministic;

/**
 * Code model containing only compilation units. Provides endpoints and type identifiers for compilation units.
 */
@Deterministic
public final class CodeModelWithCompilationUnits extends CodeModel {

    /**
     * Creates a new code model from a Dto.
     *
     * @param codeModelDto the code model Dto
     */
    public CodeModelWithCompilationUnits(CodeModelDto codeModelDto) {
        super(codeModelDto.codeItemRepository(), codeModelDto.content());
    }

    /**
     * Creates a new code model from a repository and content.
     *
     * @param codeItemRepository the code item repository
     * @param content            the code items
     */
    public CodeModelWithCompilationUnits(CodeItemRepository codeItemRepository, SortedSet<? extends CodeItem> content) {
        super(codeItemRepository, content);
    }

    /**
     * Returns the endpoints of this code model.
     *
     * @return list of compilation units
     */
    @Override
    public List<CodeCompilationUnit> getEndpoints() {
        List<CodeCompilationUnit> entities = new ArrayList<>();
        for (CodeItem codeItem : this.getContent()) {
            entities.addAll(codeItem.getAllCompilationUnits());
        }
        return entities;
    }

    /**
     * Returns the content of this code model.
     *
     * @return list of code items
     */
    @Override
    public List<? extends CodeItem> getContent() {
        this.initialize();
        return this.codeItemRepository.getCodeItemsByIds(this.content);
    }

    /**
     * Returns the metamodel of this code model.
     *
     * @return the metamodel
     */
    @Override
    public Metamodel getMetamodel() {
        return Metamodel.CODE_WITH_COMPILATION_UNITS;
    }

    /**
     * Returns the type identifiers of the code items in this model.
     *
     * @return sorted set of type identifiers
     */
    @Override
    public SortedSet<String> getTypeIdentifiers() {
        SortedSet<String> identifiers = new TreeSet<>();
        for (var codeItem : this.getContent()) {
            var type = codeItem.getType();
            type.ifPresent(identifiers::add);
        }
        return identifiers;
    }

}
