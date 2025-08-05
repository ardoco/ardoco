/* Licensed under MIT 2025. */
package edu.kit.kastel.mcse.ardoco.tlr.tests.approach;

import edu.kit.kastel.mcse.ardoco.core.tests.eval.ExpectedResults;
import edu.kit.kastel.mcse.ardoco.tlr.tests.task.DocumentationToArchitectureModelTlrTask;

public enum ArtemisEvaluationProject {
    MEDIASTORE(DocumentationToArchitectureModelTlrTask.MEDIASTORE, new ExpectedResults(.967, .967, .967, .996, .965, .997)),//
    TEASTORE(DocumentationToArchitectureModelTlrTask.TEASTORE, new ExpectedResults(.540, .999, .701, .951, .715, .948)),//
    TEAMMATES(DocumentationToArchitectureModelTlrTask.TEAMMATES, new ExpectedResults(.479, .807, .601, .961, .604, .967)),//
    BIGBLUEBUTTON(DocumentationToArchitectureModelTlrTask.BIGBLUEBUTTON, new ExpectedResults(.676, .709, .692, .962, .673, .978)),//
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
