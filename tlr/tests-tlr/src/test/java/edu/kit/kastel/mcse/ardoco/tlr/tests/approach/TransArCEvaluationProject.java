/* Licensed under MIT 2025. */
package edu.kit.kastel.mcse.ardoco.tlr.tests.approach;

import edu.kit.kastel.mcse.ardoco.core.tests.eval.results.ExpectedResults;
import edu.kit.kastel.mcse.ardoco.tlr.tests.task.Documentation2Model2CodeTlr;

public enum TransArCEvaluationProject {
    MEDIASTORE(Documentation2Model2CodeTlr.MEDIASTORE, new ExpectedResults(.995, .515, .675, .990, .715, .999));

    private final Documentation2Model2CodeTlr tlrTask;
    private final ExpectedResults expectedResults;

    TransArCEvaluationProject(Documentation2Model2CodeTlr tlrTask, ExpectedResults expectedResults) {
        this.tlrTask = tlrTask;
        this.expectedResults = expectedResults;
    }

    public Documentation2Model2CodeTlr getTlrTask() {
        return tlrTask;
    }

    public ExpectedResults getExpectedResults() {
        return expectedResults;
    }
}
