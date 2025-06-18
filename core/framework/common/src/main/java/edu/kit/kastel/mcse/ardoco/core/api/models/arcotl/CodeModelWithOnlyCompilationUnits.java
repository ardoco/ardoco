/* Licensed under MIT 2025. */
package edu.kit.kastel.mcse.ardoco.core.api.models.arcotl;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import edu.kit.kastel.mcse.ardoco.core.api.models.Metamodel;
import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.code.CodeCompilationUnit;
import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.code.CodeItem;
import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.code.CodeItemRepository;

public final class CodeModelWithOnlyCompilationUnits extends CodeModel {

    public CodeModelWithOnlyCompilationUnits(CodeModelDTO codeModelDTO) {
        super(codeModelDTO.codeItemRepository(), codeModelDTO.content());
    }

    public CodeModelWithOnlyCompilationUnits(CodeItemRepository codeItemRepository, SortedSet<? extends CodeItem> content) {
        super(codeItemRepository, content);
    }

    @Override
    public List<CodeCompilationUnit> getEndpoints() {

        List<CodeCompilationUnit> entities = new ArrayList<>();
        this.getContent().forEach(c -> entities.addAll(c.getAllCompilationUnits()));

        return entities;
    }

    @Override
    public List<? extends CodeItem> getContent() {
        this.initialize();
        return this.codeItemRepository.getCodeItemsFromIds(this.content);
    }

    @Override
    public Metamodel getMetamodel() {
        return Metamodel.CODE_ONLY_COMPILATION_UNITS;
    }

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
