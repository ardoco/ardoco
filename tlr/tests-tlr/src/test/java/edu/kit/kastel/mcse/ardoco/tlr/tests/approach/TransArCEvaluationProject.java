/* Licensed under MIT 2025. */
package edu.kit.kastel.mcse.ardoco.tlr.tests.approach;

import edu.kit.kastel.mcse.ardoco.core.tests.eval.ExpectedResults;
import edu.kit.kastel.mcse.ardoco.tlr.tests.task.DocumentationToModelToCodeTlrTask;

public enum TransArCEvaluationProject {
    MEDIASTORE(DocumentationToModelToCodeTlrTask.MEDIASTORE, new ExpectedResults(.96, .42, .588, .99, .63, .999)),//
    TEASTORE(DocumentationToModelToCodeTlrTask.TEASTORE, new ExpectedResults(.999, .708, .829, .976, .831, .999)),//
    TEAMMATES(DocumentationToModelToCodeTlrTask.TEAMMATES, new ExpectedResults(.75, .90, .82, .98, .81, .98)),//
    BIGBLUEBUTTON(DocumentationToModelToCodeTlrTask.BIGBLUEBUTTON, new ExpectedResults(.82, .84, .83, .989, .825, .985)),//
    JABREF(DocumentationToModelToCodeTlrTask.JABREF, new ExpectedResults(.885, .999, .935, .96, .915, .935));

    private final DocumentationToModelToCodeTlrTask tlrTask;
    private final ExpectedResults expectedResults;

    TransArCEvaluationProject(DocumentationToModelToCodeTlrTask tlrTask, ExpectedResults expectedResults) {
        this.tlrTask = tlrTask;
        this.expectedResults = expectedResults;
    }

    public DocumentationToModelToCodeTlrTask getTlrTask() {
        return tlrTask;
    }

    public ExpectedResults getExpectedResults() {
        return expectedResults;
    }
}
