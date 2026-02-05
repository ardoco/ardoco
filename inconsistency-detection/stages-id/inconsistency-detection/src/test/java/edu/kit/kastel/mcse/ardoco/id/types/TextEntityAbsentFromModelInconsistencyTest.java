/* Licensed under MIT 2022-2026. */
package edu.kit.kastel.mcse.ardoco.id.types;

import org.junit.jupiter.api.BeforeEach;

import edu.kit.kastel.mcse.ardoco.core.api.stage.inconsistency.Inconsistency;
import edu.kit.kastel.mcse.ardoco.core.pipeline.agent.Claimant;

/**
 * This class tests the record TextEntityAbsentFromModelInconsistency.
 */
public class TextEntityAbsentFromModelInconsistencyTest extends AbstractInconsistencyTypeTest implements Claimant {

    private TextEntityAbsentFromModelInconsistency textEntityAbsentFromModelInconsistency;

    @BeforeEach
    @SuppressWarnings("null")
    void beforeEach() {
        textEntityAbsentFromModelInconsistency = new TextEntityAbsentFromModelInconsistency("inconsistency", 1, 1.0, null);
    }

    @Override
    protected Inconsistency getInconsistency() {
        return textEntityAbsentFromModelInconsistency;
    }

    @Override
    protected String getTypeString() {
        return "TextEntityAbsentFromModel";
    }

    @Override
    protected String getReasonString() {
        return "Text indicates (confidence: 1.0) that \"inconsistency\" (sentence 1) should be contained in the model(s) but could not be found.";

    }

    @Override
    @SuppressWarnings("null")
    protected Inconsistency getUnequalInconsistency() {
        return new TextEntityAbsentFromModelInconsistency("otherInconsistency", 1, 1.0, null);
    }

    @Override
    @SuppressWarnings("null")
    protected Inconsistency getEqualInconsistency() {
        return new TextEntityAbsentFromModelInconsistency("inconsistency", 1, 1.0, null);
    }

    @Override
    protected String[] getFileOutputEntry() {
        return new String[] { getTypeString(), "1", "name", Integer.toString(1), Double.toString(0.0) };
    }

}
