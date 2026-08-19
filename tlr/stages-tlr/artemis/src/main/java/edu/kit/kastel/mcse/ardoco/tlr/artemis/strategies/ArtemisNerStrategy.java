package edu.kit.kastel.mcse.ardoco.tlr.artemis.strategies;

import edu.kit.kastel.mcse.ardoco.core.api.models.Metamodel;
import edu.kit.kastel.mcse.ardoco.core.api.stage.connectiongenerator.artemis.ArtemisTarget;
import edu.kit.kastel.mcse.ardoco.core.data.DataRepository;
import edu.kit.kastel.mcse.ardoco.naer.model.NamedEntityType;
import edu.kit.kastel.mcse.ardoco.naer.recognizer.Prompt;

public interface ArtemisNerStrategy {

    Prompt createPrompt(DataRepository dataRepository);

    Metamodel getMetamodel();

    NamedEntityType getNamedEntityType();

    default ArtemisTarget getTarget() {
        return new ArtemisTarget(getMetamodel(), getNamedEntityType());
    }

    default String getId() {
        return getNamedEntityType().name() + "Artemis";
    }
}
