package edu.kit.kastel.mcse.ardoco.core.api.models.arcotl;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.SortedSet;
import java.util.TreeSet;

import edu.kit.kastel.mcse.ardoco.core.api.entity.ModelEntity;
import edu.kit.kastel.mcse.ardoco.core.api.models.Metamodel;

public final class CoarseGrainedCodeModel extends Model {

    private final CodeModel codeModel;

    public CoarseGrainedCodeModel(CodeModel codeModel) {
        this.codeModel = Objects.requireNonNull(codeModel);
    }

    @Override
    public List<? extends ModelEntity> getContent() {
        return this.getEndpoints();
    }

    @Override
    public List<? extends ModelEntity> getEndpoints() {
        List<ModelEntity> entities = new ArrayList<>();
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
