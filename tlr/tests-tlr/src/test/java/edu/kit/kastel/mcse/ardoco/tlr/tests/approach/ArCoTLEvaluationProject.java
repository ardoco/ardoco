/* Licensed under MIT 2025. */
package edu.kit.kastel.mcse.ardoco.tlr.tests.approach;

import edu.kit.kastel.mcse.ardoco.core.tests.eval.ExpectedResults;
import edu.kit.kastel.mcse.ardoco.tlr.tests.task.ModelToCodeTlrTask;

public enum ArCoTLEvaluationProject {
    MEDIASTORE(ModelToCodeTlrTask.MEDIASTORE, new ExpectedResults(.975, .995, .985, .995, .985, .995)),//
    TEASTORE(ModelToCodeTlrTask.TEASTORE, new ExpectedResults(.975, .975, .975, .997, .965, .999)),//
    TEAMMATES(ModelToCodeTlrTask.TEAMMATES, new ExpectedResults(.999, .999, .999, .999, .999, .999)),//
    BIGBLUEBUTTON(ModelToCodeTlrTask.BIGBLUEBUTTON, new ExpectedResults(.874, .953, .912, .989, .908, .985)),//
    JABREF(ModelToCodeTlrTask.JABREF, new ExpectedResults(.999, .999, .999, .999, .999, .999));

    private final ModelToCodeTlrTask tlrTask;
    private final ExpectedResults expectedResults;

    ArCoTLEvaluationProject(ModelToCodeTlrTask tlrTask, ExpectedResults expectedResults) {
        this.tlrTask = tlrTask;
        this.expectedResults = expectedResults;
    }

    public ModelToCodeTlrTask getTlrTask() {
        return tlrTask;
    }

    public ExpectedResults getExpectedResults() {
        return expectedResults;
    }
}
