/* Licensed under MIT 2025. */
package edu.kit.kastel.mcse.ardoco.tlr.tests.approach;

import edu.kit.kastel.mcse.ardoco.core.tests.eval.results.ExpectedResults;
import edu.kit.kastel.mcse.ardoco.tlr.tests.task.Documentation2CodeTlr;

public enum ArDoCodeEvaluationProject {
    MEDIASTORE(Documentation2CodeTlr.MEDIASTORE, new ExpectedResults(.049, .66, .09, .857, .15, .86)),//
    TEASTORE(Documentation2CodeTlr.TEASTORE, new ExpectedResults(.196, .74, .31, .787, .305, .79)),//
    TEAMMATES(Documentation2CodeTlr.TEAMMATES, new ExpectedResults(.37, .92, .53, .928, .56, .93)),//
    BIGBLUEBUTTON(Documentation2CodeTlr.BIGBLUEBUTTON, new ExpectedResults(.07, .57, .126, .80, .147, .81)),//
    JABREF(Documentation2CodeTlr.JABREF, new ExpectedResults(.66, 1.00, .798, .855, .727, .80));

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
