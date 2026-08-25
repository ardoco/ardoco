package edu.kit.kastel.mcse.ardoco.id.tests.integration.inconsistencyhelper.artemis.evalruns;

import java.util.List;

public record HeldBackClassesRun(List<String> classNames, int runIndex) implements ArtemisEvaluationRun {
    @Override
    public String id() {
        return "class-holdback-run-" + runIndex;
    }

    @Override
    public String displayName() {
        return String.join(" + ", classNames);
    }

    @Override
    public boolean isBaseRun() {
        return false;
    }
}
