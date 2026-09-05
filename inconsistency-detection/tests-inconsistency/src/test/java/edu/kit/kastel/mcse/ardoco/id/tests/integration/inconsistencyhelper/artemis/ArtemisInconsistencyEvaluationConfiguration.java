package edu.kit.kastel.mcse.ardoco.id.tests.integration.inconsistencyhelper.artemis;

import java.util.List;

import edu.kit.kastel.mcse.ardoco.tlr.artemis.strategies.ArtemisNerStrategy;
import edu.kit.kastel.mcse.ardoco.tlr.artemis.strategies.ClassArtemisNerStrategy;
import edu.kit.kastel.mcse.ardoco.tlr.artemis.strategies.ComponentArtemisNerStrategy;
import edu.kit.kastel.mcse.ardoco.tlr.artemis.strategies.DatafileArtemisNerStrategy;

public record ArtemisInconsistencyEvaluationConfiguration(String name, List<ArtemisNerStrategy> strategies) {
    public ArtemisInconsistencyEvaluationConfiguration {
        strategies = List.copyOf(strategies);
    }

    public static ArtemisInconsistencyEvaluationConfiguration component() {
        return new ArtemisInconsistencyEvaluationConfiguration("Component", List.of(new ComponentArtemisNerStrategy()));
    }

    public static ArtemisInconsistencyEvaluationConfiguration clazz() {
        return new ArtemisInconsistencyEvaluationConfiguration("Class", List.of(new ClassArtemisNerStrategy()));
    }

    public static ArtemisInconsistencyEvaluationConfiguration datafile() {
        return new ArtemisInconsistencyEvaluationConfiguration("Datafile", List.of(new DatafileArtemisNerStrategy()));
    }
}
