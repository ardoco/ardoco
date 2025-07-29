package edu.kit.kastel.mcse.ardoco.tlr.connectiongenerator.ner.informants;

import edu.kit.kastel.mcse.ardoco.core.data.DataRepository;
import edu.kit.kastel.mcse.ardoco.core.pipeline.agent.Informant;

public class NerInformant extends Informant {

    public NerInformant(DataRepository dataRepository) {
        super(NerInformant.class.getSimpleName(), dataRepository);
    }

    @Override
    public void process() {
        // TODO
        getLogger().info("Hello from NerInformant");
    }
}
