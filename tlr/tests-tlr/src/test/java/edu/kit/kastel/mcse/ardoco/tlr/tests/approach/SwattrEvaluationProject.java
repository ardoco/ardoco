/* Licensed under MIT 2025. */
package edu.kit.kastel.mcse.ardoco.tlr.tests.approach;

import edu.kit.kastel.mcse.ardoco.core.tests.eval.ExpectedResults;
import edu.kit.kastel.mcse.ardoco.tlr.tests.task.DocumentationToArchitectureModelTlrTask;

public enum SwattrEvaluationProject {
    MEDIASTORE(DocumentationToArchitectureModelTlrTask.MEDIASTORE, new ExpectedResults(.999, .62, .765, .978, .778, .999)),//
    TEASTORE(DocumentationToArchitectureModelTlrTask.TEASTORE, new ExpectedResults(.999, .74, .85, .984, .853, .999)),//
    TEAMMATES(DocumentationToArchitectureModelTlrTask.TEAMMATES, new ExpectedResults(.555, .882, .681, .965, .688, .975)),//
    BIGBLUEBUTTON(DocumentationToArchitectureModelTlrTask.BIGBLUEBUTTON, new ExpectedResults(.875, .826, .85, .985, .835, .985)),//
    JABREF(DocumentationToArchitectureModelTlrTask.JABREF, new ExpectedResults(.899, .999, .946, .973, .932, .966));

    private final DocumentationToArchitectureModelTlrTask tlrTask;
    private final ExpectedResults expectedResults;

    SwattrEvaluationProject(DocumentationToArchitectureModelTlrTask tlrTask, ExpectedResults expectedResults) {
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
