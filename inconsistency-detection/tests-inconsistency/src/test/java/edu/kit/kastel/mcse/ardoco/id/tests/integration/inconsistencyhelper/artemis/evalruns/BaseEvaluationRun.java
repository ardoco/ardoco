package edu.kit.kastel.mcse.ardoco.id.tests.integration.inconsistencyhelper.artemis.evalruns;

public record BaseEvaluationRun() implements ArtemisEvaluationRun {
    @Override
    public String id() {
        return "base";
    }

    @Override
    public String displayName() {
        return "Base run";
    }

    @Override
    public boolean isBaseRun() {
        return true;
    }
}
