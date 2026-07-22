/* Licensed under MIT 2025. */
package edu.kit.kastel.mcse.ardoco.tlr.tests.approach;

import edu.kit.kastel.mcse.ardoco.core.tests.eval.ExpectedResults;
import edu.kit.kastel.mcse.ardoco.tlr.tests.task.ComponentSadSamTlrTask;
import edu.kit.kastel.mcse.ardoco.tlr.tests.task.TlrTask;

public enum ComponentArtemisEvaluationProject implements ArtemisEvaluationProject {
    MEDIASTORE(ComponentSadSamTlrTask.MEDIASTORE, new ExpectedResults(.420, .420, .420, .420, .420, .420)),//
    TEASTORE(ComponentSadSamTlrTask.TEASTORE, new ExpectedResults(.420, .420, .420, .420, .420, .420)),//
    TEAMMATES(ComponentSadSamTlrTask.TEAMMATES, new ExpectedResults(.420, .420, .420, .420, .420, .420)),//
    BIGBLUEBUTTON(ComponentSadSamTlrTask.BIGBLUEBUTTON, new ExpectedResults(.420, .420, .420, .420, .420, .420)),//
    JABREF(ComponentSadSamTlrTask.JABREF, new ExpectedResults(.420, .420, .420, .420, .420, .420));

    private final ComponentSadSamTlrTask tlrTask;
    private final ExpectedResults expectedResults;

    ComponentArtemisEvaluationProject(ComponentSadSamTlrTask tlrTask, ExpectedResults expectedResults) {
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
