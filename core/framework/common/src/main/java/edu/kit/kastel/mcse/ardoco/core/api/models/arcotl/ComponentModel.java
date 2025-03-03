package edu.kit.kastel.mcse.ardoco.core.api.models.arcotl;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.SortedSet;
import java.util.TreeSet;

import edu.kit.kastel.mcse.ardoco.core.api.entity.ModelEntity;
import edu.kit.kastel.mcse.ardoco.core.api.models.Metamodel;
import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.architecture.ArchitectureComponent;
import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.architecture.ArchitectureItem;

public final class ComponentModel extends Model {

    private final ArchitectureModel architectureModel;

    public ComponentModel(ArchitectureModel architectureModel) {
        this.architectureModel = Objects.requireNonNull(architectureModel);
    }

    @Override
    public List<ArchitectureComponent> getContent() {
        List<ArchitectureComponent> entities = new ArrayList<>();
        for (ArchitectureItem entity : architectureModel.getContent()) {
            switch (entity) {
            case ArchitectureComponent component -> entities.add(component);
            default -> {
            }
            }
        }
        return entities;
    }

    @Override
    public List<? extends ModelEntity> getEndpoints() {
        return this.getContent();
    }

    @Override
    public Metamodel getMetamodel() {
        return Metamodel.COMPONENT;
    }

    @Override
    public SortedSet<String> getTypeIdentifiers() {

        SortedSet<String> identifiers = new TreeSet<>();

        for (var entity : getContent()) {
            if (entity.getType().isPresent()) {
                identifiers.add(entity.getType().orElseThrow());
                identifiers.addAll(entity.getTypeParts().orElseThrow().toList());
            }
        }
        return identifiers;
    }
}
