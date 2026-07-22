/* Licensed under MIT 2025. */
package edu.kit.kastel.mcse.ardoco.tlr.tests.approach;

import edu.kit.kastel.mcse.ardoco.core.tests.eval.ExpectedResults;
import edu.kit.kastel.mcse.ardoco.tlr.tests.task.ClassSadCodeTlrTask;
import edu.kit.kastel.mcse.ardoco.tlr.tests.task.TlrTask;

public enum ClassArtemisEvaluationProject implements ArtemisEvaluationProject {
    TEAMMATES(ClassSadCodeTlrTask.TEAMMATES, new ExpectedResults(.420, .420, .420, .420, .420, .420)),//
    SCONS(ClassSadCodeTlrTask.SCONS, new ExpectedResults(.420, .420, .420, .420, .420, .420)),//
    BEETS(ClassSadCodeTlrTask.BEETS, new ExpectedResults(.420, .420, .420, .420, .420, .420)),//
    ZENGARDEN(ClassSadCodeTlrTask.ZENGARDEN, new ExpectedResults(.420, .420, .420, .420, .420, .420));

    private final ClassSadCodeTlrTask tlrTask;
    private final ExpectedResults expectedResults;

    ClassArtemisEvaluationProject(ClassSadCodeTlrTask tlrTask, ExpectedResults expectedResults) {
        this.tlrTask = tlrTask;
        this.expectedResults = expectedResults;
    }

    @Override
    public TlrTask getTlrTask() {
        return tlrTask;
    }

    @Override
    public ExpectedResults getExpectedResults() {
        return expectedResults;
    }

    @Override
    public String getName() {
        return this.name();
    }

}
