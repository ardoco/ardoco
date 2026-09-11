package edu.kit.kastel.mcse.ardoco.id.tests.integration.inconsistencyhelper.artemis.evalruns;

public record ExtendedSadRun(int runIndex) implements ArtemisEvaluationRun {
    @Override
    public String id() {
        return "extended-sad-run-" + runIndex;
    }

    @Override
    public String displayName() {
        return "extended-sad-run-" + runIndex;
    }

    @Override
    public boolean isBaseRun() {
        return false;
    }
}
