package edu.kit.kastel.mcse.ardoco.id.tests.tasks;

import java.util.List;

import edu.kit.kastel.mcse.ardoco.core.common.tuple.Pair;
import edu.kit.kastel.mcse.ardoco.core.tests.eval.EvaluationProject;

public interface TlrTask {
    List<Pair<Integer, String>> getExpectedTraceLinks();

    EvaluationProject getEvaluationProject();
}
