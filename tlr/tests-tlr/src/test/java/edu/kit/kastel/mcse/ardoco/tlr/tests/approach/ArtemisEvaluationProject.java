package edu.kit.kastel.mcse.ardoco.tlr.tests.approach;

import edu.kit.kastel.mcse.ardoco.core.tests.eval.ExpectedResults;
import edu.kit.kastel.mcse.ardoco.tlr.tests.task.TlrTask;

public interface ArtemisEvaluationProject {
    TlrTask getTlrTask();

    ExpectedResults getExpectedResults();

    String getName();
}
