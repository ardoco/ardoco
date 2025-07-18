/* Licensed under MIT 2025. */
package edu.kit.kastel.mcse.ardoco.tlr.tests.approach;

import edu.kit.kastel.mcse.ardoco.core.tests.eval.results.ExpectedResults;
import edu.kit.kastel.mcse.ardoco.tlr.tests.task.Model2CodeTlr;

public enum ArCoTLEvaluationProject {
    MEDIASTORE(Model2CodeTlr.MEDIASTORE, new ExpectedResults(.975, .995, .985, .995, .985, .995));

    private final Model2CodeTlr tlrTask;
    private final ExpectedResults expectedResults;

    ArCoTLEvaluationProject(Model2CodeTlr tlrTask, ExpectedResults expectedResults) {
        this.tlrTask = tlrTask;
        this.expectedResults = expectedResults;
    }

    public Model2CodeTlr getTlrTask() {
        return tlrTask;
    }

    public ExpectedResults getExpectedResults() {
        return expectedResults;
    }
}
