package edu.kit.kastel.mcse.ardoco.id.tests.integration.inconsistencyhelper.artemis.evalruns;

import edu.kit.kastel.mcse.ardoco.core.api.models.architecture.ArchitectureItem;

public record HeldBackArchitectureItemRun(ArchitectureItem item) implements ArtemisEvaluationRun {
    @Override
    public String id() {
        return item.getId();
    }

    @Override
    public String displayName() {
        return item.getName();
    }

    @Override
    public boolean isBaseRun() {
        return false;
    }
}
