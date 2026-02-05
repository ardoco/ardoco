/* Licensed under MIT 2022-2026. */
package edu.kit.kastel.mcse.ardoco.id.types;

import org.junit.jupiter.api.BeforeEach;

import edu.kit.kastel.mcse.ardoco.core.api.entity.ArchitectureEntityImpl;
import edu.kit.kastel.mcse.ardoco.core.api.entity.ModelEntity;
import edu.kit.kastel.mcse.ardoco.core.api.stage.inconsistency.Inconsistency;

/**
 *
 */
public class ModelEntityAbsentFromTextInconsistencyTest extends AbstractInconsistencyTypeTest {

    private ModelEntityAbsentFromTextInconsistency modelEntityAbsentFromTextInconsistency;

    @BeforeEach
    void beforeEach() {
        ModelEntity instance = new ArchitectureEntityImpl("instance", "type", "uid1");
        modelEntityAbsentFromTextInconsistency = new ModelEntityAbsentFromTextInconsistency(instance);
    }

    @Override
    protected Inconsistency getInconsistency() {
        return modelEntityAbsentFromTextInconsistency;
    }

    @Override
    protected String getTypeString() {
        return "ModelEntityAbsentFromText";
    }

    @Override
    protected String getReasonString() {
        return "Model contains an Instance that should be documented (because it is not whitelisted and its type \"type\" is configured to need documentation) but could not be found in documentation: instance";
    }

    @Override
    protected Inconsistency getUnequalInconsistency() {
        ArchitectureEntityImpl entity = new ArchitectureEntityImpl("otherInstance", "otherType", "uid2");
        return new ModelEntityAbsentFromTextInconsistency(entity);
    }

    @Override
    protected Inconsistency getEqualInconsistency() {
        ArchitectureEntityImpl entity = new ArchitectureEntityImpl("instance", "type", "uid1");
        return new ModelEntityAbsentFromTextInconsistency(entity);
    }

    @Override
    protected String[] getFileOutputEntry() {
        return new String[] { getTypeString(), "instance", "type" };
    }

}
