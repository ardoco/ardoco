/* Licensed under MIT 2025. */
package edu.kit.kastel.mcse.ardoco.tlr.tests.approach;

import edu.kit.kastel.mcse.ardoco.core.tests.eval.results.ExpectedResults;
import edu.kit.kastel.mcse.ardoco.tlr.tests.task.Documentation2CodeTlr;

public enum ArDoCodeEvaluationProject {
    // TODO Add expected results
    MEDIASTORE(Documentation2CodeTlr.MEDIASTORE, new ExpectedResults(0, 0, 0, 0, 0, 0)),//
    TEASTORE(Documentation2CodeTlr.TEASTORE, new ExpectedResults(0, 0, 0, 0, 0, 0)),//
    TEAMMATES(Documentation2CodeTlr.TEAMMATES, new ExpectedResults(0, 0, 0, 0, 0, 0)),//
    BIGBLUEBUTTON(Documentation2CodeTlr.BIGBLUEBUTTON, new ExpectedResults(0, 0, 0, 0, 0, 0)),//
    JABREF(Documentation2CodeTlr.JABREF, new ExpectedResults(0, 0, 0, 0, 0, 0));

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
