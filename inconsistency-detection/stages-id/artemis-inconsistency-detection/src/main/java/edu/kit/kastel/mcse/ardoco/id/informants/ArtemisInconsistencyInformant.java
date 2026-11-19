/* Licensed under MIT 2026. */
package edu.kit.kastel.mcse.ardoco.id.informants;

import edu.kit.kastel.mcse.ardoco.core.api.stage.connectiongenerator.artemis.ArtemisTarget;
import edu.kit.kastel.mcse.ardoco.core.data.DataRepository;
import edu.kit.kastel.mcse.ardoco.core.pipeline.agent.Informant;
import edu.kit.kastel.mcse.ardoco.tlr.artemis.strategies.ArtemisNerStrategy;

/**
 * Base class for Artemis inconsistency informants that operate on a specific NER strategy.
 * <p>
 * This class provides shared access to the configured {@link ArtemisNerStrategy} and its corresponding {@link ArtemisTarget}. Concrete subclasses use this
 * information to read Artemis traceability data and add detected inconsistencies to the appropriate target-specific inconsistency state.
 * </p>
 */
public abstract class ArtemisInconsistencyInformant extends Informant {

    private final ArtemisNerStrategy strategy;

    protected ArtemisInconsistencyInformant(String id, DataRepository dataRepository, ArtemisNerStrategy strategy) {
        super(id, dataRepository);
        this.strategy = strategy;
    }

    protected ArtemisNerStrategy getStrategy() {
        return this.strategy;
    }

    protected ArtemisTarget getTarget() {
        return this.strategy.getTarget();
    }

}
