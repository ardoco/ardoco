/* Licensed under MIT 2025. */
package edu.kit.kastel.mcse.ardoco.core.api.models.arcotl;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import edu.kit.kastel.mcse.ardoco.core.api.models.Metamodel;
import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.code.CodeItem;
import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.code.CodeItemRepository;

public final class CodeModelWithCompilationUnitsAndPackages extends CodeModel {

    private final CodeModelWithOnlyCompilationUnits codeModel;

    public CodeModelWithCompilationUnitsAndPackages(CodeModelDTO codeModelDTO) {
        super(codeModelDTO.codeItemRepository(), codeModelDTO.content());
        this.codeModel = new CodeModelWithOnlyCompilationUnits(codeModelDTO);
    }

    public CodeModelWithCompilationUnitsAndPackages(CodeItemRepository codeItemRepository, SortedSet<? extends CodeItem> content) {
        super(codeItemRepository, content);
        this.codeModel = new CodeModelWithOnlyCompilationUnits(codeItemRepository, content);
    }

    @Override
    public List<? extends CodeItem> getContent() {
        return this.getEndpoints();
    }

    @Override
    public List<CodeItem> getEndpoints() {
        List<CodeItem> entities = new ArrayList<>();
        codeModel.getContent().forEach(c -> entities.addAll(c.getAllCompilationUnits()));

        entities.addAll(codeModel.getAllPackages());
        return entities;
    }

    @Override
    public Metamodel getMetamodel() {
        return Metamodel.CODE_WITH_COMPILATION_UNITS_AND_PACKAGES;
    }

    @Override
    public SortedSet<String> getTypeIdentifiers() {
        SortedSet<String> identifiers = new TreeSet<>();

        for (var codeItem : this.getContent()) {
            var type = codeItem.getType();
            if (type.isPresent()) {
                identifiers.add(type.get());
            }
        }
        return identifiers;
    }
}
