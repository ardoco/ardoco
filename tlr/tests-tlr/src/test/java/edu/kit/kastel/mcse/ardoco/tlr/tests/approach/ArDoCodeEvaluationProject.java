/* Licensed under MIT 2025. */
package edu.kit.kastel.mcse.ardoco.tlr.tests.approach;

import edu.kit.kastel.mcse.ardoco.core.tests.eval.results.ExpectedResults;
import edu.kit.kastel.mcse.ardoco.tlr.tests.task.Documentation2CodeTlr;

public enum ArDoCodeEvaluationProject {
    MEDIASTORE(Documentation2CodeTlr.MEDIASTORE, new ExpectedResults(0, 0, 0, 0, 0, 0));

    private final Documentation2CodeTlr tlrTask;
    private final ExpectedResults expectedResults;

    ArDoCodeEvaluationProject(Documentation2CodeTlr tlrTask, ExpectedResults expectedResults) {
        this.tlrTask = tlrTask;
        this.expectedResults = expectedResults;
    }

    public Documentation2CodeTlr getTlrTask() {
        return tlrTask;
    }

    public ExpectedResults getExpectedResults() {
        return expectedResults;
    }
}
