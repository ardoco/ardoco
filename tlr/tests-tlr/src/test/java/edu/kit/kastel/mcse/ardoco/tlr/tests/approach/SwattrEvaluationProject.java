/* Licensed under MIT 2025. */
package edu.kit.kastel.mcse.ardoco.tlr.tests.approach;

import edu.kit.kastel.mcse.ardoco.core.tests.eval.ExpectedResults;
import edu.kit.kastel.mcse.ardoco.tlr.tests.task.Documentation2ArchitectureModelTlrTask;

public enum SwattrEvaluationProject {
    MEDIASTORE(Documentation2ArchitectureModelTlrTask.MEDIASTORE, new ExpectedResults(.999, .62, .765, .978, .778, .999)),//
    TEASTORE(Documentation2ArchitectureModelTlrTask.TEASTORE, new ExpectedResults(.999, .74, .85, .984, .853, .999)),//
    TEAMMATES(Documentation2ArchitectureModelTlrTask.TEAMMATES, new ExpectedResults(.555, .882, .681, .965, .688, .975)),//
    BIGBLUEBUTTON(Documentation2ArchitectureModelTlrTask.BIGBLUEBUTTON, new ExpectedResults(.875, .826, .85, .985, .835, .985)),//
    JABREF(Documentation2ArchitectureModelTlrTask.JABREF, new ExpectedResults(.899, .999, .946, .973, .932, .966));

    private final Documentation2ArchitectureModelTlrTask tlrTask;
    private final ExpectedResults expectedResults;

    SwattrEvaluationProject(Documentation2ArchitectureModelTlrTask tlrTask, ExpectedResults expectedResults) {
        this.tlrTask = tlrTask;
        this.expectedResults = expectedResults;
    }

    public Documentation2ArchitectureModelTlrTask getTlrTask() {
        return tlrTask;
    }

    public ExpectedResults getExpectedResults() {
        return expectedResults;
    }
}
