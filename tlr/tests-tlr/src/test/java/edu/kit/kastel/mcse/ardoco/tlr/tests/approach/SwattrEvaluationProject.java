/* Licensed under MIT 2025. */
package edu.kit.kastel.mcse.ardoco.tlr.tests.approach;

import edu.kit.kastel.mcse.ardoco.core.tests.eval.results.ExpectedResults;
import edu.kit.kastel.mcse.ardoco.tlr.tests.task.Documentation2ArchitectureModelTlr;

public enum SwattrEvaluationProject {
    MEDIASTORE(Documentation2ArchitectureModelTlr.MEDIASTORE, new ExpectedResults(.999, .62, .765, .978, .778, .999)),//
    TEASTORE(Documentation2ArchitectureModelTlr.TEASTORE, new ExpectedResults(.999, .74, .85, .984, .853, .999)),//
    TEAMMATES(Documentation2ArchitectureModelTlr.TEAMMATES, new ExpectedResults(.555, .882, .681, .965, .688, .975)),//
    BIGBLUEBUTTON(Documentation2ArchitectureModelTlr.BIGBLUEBUTTON, new ExpectedResults(.875, .826, .85, .985, .835, .985)),//
    JABREF(Documentation2ArchitectureModelTlr.JABREF, new ExpectedResults(.899, .999, .946, .973, .932, .966));

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
