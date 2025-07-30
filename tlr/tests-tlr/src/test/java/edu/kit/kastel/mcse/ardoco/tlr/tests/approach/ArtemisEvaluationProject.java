/* Licensed under MIT 2025. */
package edu.kit.kastel.mcse.ardoco.tlr.tests.approach;

import edu.kit.kastel.mcse.ardoco.core.tests.eval.ExpectedResults;
import edu.kit.kastel.mcse.ardoco.tlr.tests.task.DocumentationToArchitectureModelTlrTask;

public enum ArtemisEvaluationProject {
    MEDIASTORE(DocumentationToArchitectureModelTlrTask.MEDIASTORE, new ExpectedResults(.0, .0, .0, .0, -1, .0)),//
    TEASTORE(DocumentationToArchitectureModelTlrTask.TEASTORE, new ExpectedResults(.0, .0, .0, .0, -1, .0)),//
    TEAMMATES(DocumentationToArchitectureModelTlrTask.TEAMMATES, new ExpectedResults(.0, .0, .0, .0, -1, .0)),//
    BIGBLUEBUTTON(DocumentationToArchitectureModelTlrTask.BIGBLUEBUTTON, new ExpectedResults(.0, .0, .0, .0, -1, .0)),//
    JABREF(DocumentationToArchitectureModelTlrTask.JABREF, new ExpectedResults(.0, .0, .0, .0, -1, .0));

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
