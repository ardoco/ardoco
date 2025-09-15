/* Licensed under MIT 2025. */
package edu.kit.kastel.mcse.ardoco.tlr.tests.approach;

import edu.kit.kastel.mcse.ardoco.core.tests.eval.ExpectedResults;
import edu.kit.kastel.mcse.ardoco.tlr.tests.task.DocumentationToCodeTlrTask;

public enum ArDoCodeEvaluationProject {
    MEDIASTORE(DocumentationToCodeTlrTask.MEDIASTORE, new ExpectedResults(.049, .593, .096, .856, .145, .86)),//
    TEASTORE(DocumentationToCodeTlrTask.TEASTORE, new ExpectedResults(.196, .74, .31, .787, .305, .79)),//
    TEAMMATES(DocumentationToCodeTlrTask.TEAMMATES, new ExpectedResults(.39, .916, .55, .928, .57, .93)),//
    BIGBLUEBUTTON(DocumentationToCodeTlrTask.BIGBLUEBUTTON, new ExpectedResults(.08, .56, .14, .805, .156, .81)),//
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
