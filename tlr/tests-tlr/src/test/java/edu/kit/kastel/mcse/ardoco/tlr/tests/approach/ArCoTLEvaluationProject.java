/* Licensed under MIT 2025. */
package edu.kit.kastel.mcse.ardoco.tlr.tests.approach;

import edu.kit.kastel.mcse.ardoco.core.tests.eval.ExpectedResults;
import edu.kit.kastel.mcse.ardoco.tlr.tests.task.Model2CodeTlr;

public enum ArCoTLEvaluationProject {
    MEDIASTORE(Model2CodeTlr.MEDIASTORE, new ExpectedResults(.975, .995, .985, .995, .985, .995)),//
    TEASTORE(Model2CodeTlr.TEASTORE, new ExpectedResults(.975, .975, .975, .997, .965, .999)),//
    TEAMMATES(Model2CodeTlr.TEAMMATES, new ExpectedResults(.999, .999, .999, .999, .999, .999)),//
    BIGBLUEBUTTON(Model2CodeTlr.BIGBLUEBUTTON, new ExpectedResults(.874, .953, .912, .989, .908, .985)),//
    JABREF(Model2CodeTlr.JABREF, new ExpectedResults(.999, .999, .999, .999, .999, .999));

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
