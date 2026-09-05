/* Licensed under MIT 2025. */
package edu.kit.kastel.mcse.ardoco.tlr.tests.approach;

import edu.kit.kastel.mcse.ardoco.core.tests.eval.ExpectedResults;
import edu.kit.kastel.mcse.ardoco.tlr.tests.task.DatafileSadCodeTlrTask;
import edu.kit.kastel.mcse.ardoco.tlr.tests.task.TlrTask;

public enum DatafileArtemisEvaluationProject implements ArtemisEvaluationProject {
    CORONAWARNAPP(DatafileSadCodeTlrTask.CORONAWARNAPP, new ExpectedResults(.420, .420, .420, .420, .420, .420)), //
    BIGBLUEBUTTON(DatafileSadCodeTlrTask.BIGBLUEBUTTON, new ExpectedResults(.420, .420, .420, .420, .420, .420)), //
    ROD(DatafileSadCodeTlrTask.ROD, new ExpectedResults(.420, .420, .420, .420, .420, .420)), //
    TEAMMATES(DatafileSadCodeTlrTask.TEAMMATES, new ExpectedResults(.420, .420, .420, .420, .420, .420)); //

    private final DatafileSadCodeTlrTask tlrTask;
    private final ExpectedResults expectedResults;

    DatafileArtemisEvaluationProject(DatafileSadCodeTlrTask tlrTask, ExpectedResults expectedResults) {
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
