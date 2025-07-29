/* Licensed under MIT 2025. */
package edu.kit.kastel.mcse.ardoco.tlr.tests.approach;

import edu.kit.kastel.mcse.ardoco.core.tests.eval.ExpectedResults;
import edu.kit.kastel.mcse.ardoco.tlr.tests.task.DocumentationToCodeTlrTask;

public enum ArDoCodeEvaluationProject {
    MEDIASTORE(DocumentationToCodeTlrTask.MEDIASTORE, new ExpectedResults(.049, .66, .09, .857, .15, .86)),//
    TEASTORE(DocumentationToCodeTlrTask.TEASTORE, new ExpectedResults(.196, .74, .31, .787, .305, .79)),//
    TEAMMATES(DocumentationToCodeTlrTask.TEAMMATES, new ExpectedResults(.37, .92, .53, .928, .56, .93)),//
    BIGBLUEBUTTON(DocumentationToCodeTlrTask.BIGBLUEBUTTON, new ExpectedResults(.07, .57, .126, .80, .147, .81)),//
    JABREF(DocumentationToCodeTlrTask.JABREF, new ExpectedResults(.66, 1.00, .798, .855, .727, .80));

    private final DocumentationToCodeTlrTask tlrTask;
    private final ExpectedResults expectedResults;

    ArDoCodeEvaluationProject(DocumentationToCodeTlrTask tlrTask, ExpectedResults expectedResults) {
        this.tlrTask = tlrTask;
        this.expectedResults = expectedResults;
    }

    public DocumentationToCodeTlrTask getTlrTask() {
        return tlrTask;
    }

    public ExpectedResults getExpectedResults() {
        return expectedResults;
    }
}
