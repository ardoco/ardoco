/* Licensed under MIT 2022-2024. */
package edu.kit.kastel.mcse.ardoco.id.types;

import org.junit.jupiter.api.BeforeEach;

import edu.kit.kastel.mcse.ardoco.core.api.stage.inconsistency.Inconsistency;
import edu.kit.kastel.mcse.ardoco.core.pipeline.agent.Claimant;

/**
 * This class tests the record MissingModelInconsistency.
 */
public class MissingModelInstanceInconsistencyTest extends AbstractInconsistencyTypeTest implements Claimant {

    private MissingModelInstanceInconsistency missingModelInstanceInconsistency;

    @BeforeEach
    void beforeEach() {
        missingModelInstanceInconsistency = new MissingModelInstanceInconsistency("inconsistency", 1, 1.0, null);
    }

    @Override
    protected Inconsistency getInconsistency() {
        return missingModelInstanceInconsistency;
    }

    @Override
    protected String getTypeString() {
        return "MissingModelInstance";
    }

    @Override
    protected String getReasonString() {
        return "Text indicates (confidence: 1.0) that \"inconsistency\" (sentence 1) should be contained in the model(s) but could not be found.";

    }

    @Override
    protected Inconsistency getUnequalInconsistency() {
        return new MissingModelInstanceInconsistency("otherInconsistency", 1, 1.0, null);
    }

    @Override
    protected Inconsistency getEqualInconsistency() {
        return new MissingModelInstanceInconsistency("inconsistency", 1, 1.0, null);
    }

    @Override
    protected String[] getFileOutputEntry() {
        return new String[] { getTypeString(), "1", "name", Integer.toString(1), Double.toString(0.0) };
    }

}
