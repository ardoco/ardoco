/* Licensed under MIT 2025. */
package edu.kit.kastel.mcse.ardoco.tlr.tests.approach;

import edu.kit.kastel.mcse.ardoco.core.tests.eval.results.ExpectedResults;
import edu.kit.kastel.mcse.ardoco.tlr.tests.task.Documentation2ArchitectureModelTlr;

public enum SwattrEvaluationProject {
    MEDIASTORE(Documentation2ArchitectureModelTlr.MEDIASTORE, new ExpectedResults(.999, .620, .765, .978, .778, .999));

    private final Documentation2ArchitectureModelTlr tlrTask;
    private final ExpectedResults expectedResults;

    SwattrEvaluationProject(Documentation2ArchitectureModelTlr tlrTask, ExpectedResults expectedResults) {
        this.tlrTask = tlrTask;
        this.expectedResults = expectedResults;
    }

    public Documentation2ArchitectureModelTlr getTlrTask() {
        return tlrTask;
    }

    public ExpectedResults getExpectedResults() {
        return expectedResults;
    }
}
