package edu.kit.kastel.mcse.ardoco.tlr.connectiongenerator.ner.informants;

import edu.kit.kastel.mcse.ardoco.core.data.DataRepository;
import edu.kit.kastel.mcse.ardoco.core.pipeline.agent.Informant;

public class NerConnectionInformant extends Informant {

    public NerConnectionInformant(DataRepository dataRepository) {
        super(NerConnectionInformant.class.getSimpleName(), dataRepository);
    }

    @Override
    protected void process() {
        // TODO
        getLogger().info("Hello from NerConnectionInformant");
    }
}
