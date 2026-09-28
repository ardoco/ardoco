package edu.kit.kastel.mcse.ardoco.tlr.tests.task;

import org.eclipse.collections.api.list.ImmutableList;

import edu.kit.kastel.mcse.ardoco.core.common.tuple.Pair;
import edu.kit.kastel.mcse.ardoco.core.tests.eval.EvaluationProject;

public interface TlrTask {
    ImmutableList<Pair<Integer, String>> getExpectedTraceLinks();

    EvaluationProject getEvaluationProject();
}
