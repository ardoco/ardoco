package edu.kit.kastel.mcse.ardoco.core.api.models.arcotl;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import edu.kit.kastel.mcse.ardoco.core.api.models.Metamodel;
import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.code.CodeItem;
import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.code.CodeItemRepository;

public final class CoarseGrainedCodeModel extends CodeModel {

    private final FineGrainedCodeModel codeModel;

    public CoarseGrainedCodeModel(CodeModelDTO codeModelDTO) {
        super(codeModelDTO.codeItemRepository(), codeModelDTO.content());
        this.codeModel = new FineGrainedCodeModel(codeModelDTO);
    }

    public CoarseGrainedCodeModel(CodeItemRepository codeItemRepository, SortedSet<? extends CodeItem> content) {
        super(codeItemRepository, content);
        this.codeModel = new FineGrainedCodeModel(codeItemRepository, content);
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
        return Metamodel.CODE_AS_ARCHITECTURE;
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
