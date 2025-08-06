/* Licensed under MIT 2025. */
package edu.kit.kastel.mcse.ardoco.tlr.tests.approach;

import edu.kit.kastel.mcse.ardoco.core.tests.eval.ExpectedResults;
import edu.kit.kastel.mcse.ardoco.tlr.tests.task.DocumentationToArchitectureModelTlrTask;

public enum ArtemisEvaluationProject {
    MEDIASTORE(DocumentationToArchitectureModelTlrTask.MEDIASTORE, new ExpectedResults(.995, .865, .925, .985, .925, .995)),//
    TEASTORE(DocumentationToArchitectureModelTlrTask.TEASTORE, new ExpectedResults(.535, .995, .695, .945, .715, .945)),//
    TEAMMATES(DocumentationToArchitectureModelTlrTask.TEAMMATES, new ExpectedResults(.666, .666, .666, .900, .500, .900)),//
    BIGBLUEBUTTON(DocumentationToArchitectureModelTlrTask.BIGBLUEBUTTON, new ExpectedResults(.655, .655, .655, .955, .635, .975)),//
    JABREF(DocumentationToArchitectureModelTlrTask.JABREF, new ExpectedResults(.900, .999, .947, .974, .932, .966));

    private final DocumentationToArchitectureModelTlrTask tlrTask;
    private final ExpectedResults expectedResults;

    ArtemisEvaluationProject(DocumentationToArchitectureModelTlrTask tlrTask, ExpectedResults expectedResults) {
        this.tlrTask = tlrTask;
        this.expectedResults = expectedResults;
    }

    public DocumentationToArchitectureModelTlrTask getTlrTask() {
        return tlrTask;
    }

    public ExpectedResults getExpectedResults() {
        return expectedResults;
    }
}
