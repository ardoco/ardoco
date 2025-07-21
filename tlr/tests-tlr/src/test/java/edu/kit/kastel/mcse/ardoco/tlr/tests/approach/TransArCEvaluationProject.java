/* Licensed under MIT 2025. */
package edu.kit.kastel.mcse.ardoco.tlr.tests.approach;

import edu.kit.kastel.mcse.ardoco.core.tests.eval.ExpectedResults;
import edu.kit.kastel.mcse.ardoco.tlr.tests.task.Documentation2Model2CodeTlrTask;

public enum TransArCEvaluationProject {
    MEDIASTORE(Documentation2Model2CodeTlrTask.MEDIASTORE, new ExpectedResults(.995, .515, .675, .99, .715, .999)),//
    TEASTORE(Documentation2Model2CodeTlrTask.TEASTORE, new ExpectedResults(.999, .708, .829, .976, .831, .999)),//
    TEAMMATES(Documentation2Model2CodeTlrTask.TEAMMATES, new ExpectedResults(.705, .909, .795, .975, .785, .975)),//
    BIGBLUEBUTTON(Documentation2Model2CodeTlrTask.BIGBLUEBUTTON, new ExpectedResults(.765, .905, .835, .985, .825, .985)),//
    JABREF(Documentation2Model2CodeTlrTask.JABREF, new ExpectedResults(.885, .999, .935, .96, .915, .935));

    private final Documentation2Model2CodeTlrTask tlrTask;
    private final ExpectedResults expectedResults;

    TransArCEvaluationProject(Documentation2Model2CodeTlrTask tlrTask, ExpectedResults expectedResults) {
        this.tlrTask = tlrTask;
        this.expectedResults = expectedResults;
    }

    public Documentation2Model2CodeTlrTask getTlrTask() {
        return tlrTask;
    }

    public ExpectedResults getExpectedResults() {
        return expectedResults;
    }
}
