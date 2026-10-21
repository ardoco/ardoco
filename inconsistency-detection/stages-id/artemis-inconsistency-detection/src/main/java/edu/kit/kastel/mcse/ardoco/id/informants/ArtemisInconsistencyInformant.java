/* Licensed under MIT 2026. */
package edu.kit.kastel.mcse.ardoco.id.informants;

import edu.kit.kastel.mcse.ardoco.core.api.stage.connectiongenerator.artemis.ArtemisTarget;
import edu.kit.kastel.mcse.ardoco.core.data.DataRepository;
import edu.kit.kastel.mcse.ardoco.core.pipeline.agent.Informant;
import edu.kit.kastel.mcse.ardoco.tlr.artemis.strategies.ArtemisNerStrategy;

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
