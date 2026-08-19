package edu.kit.kastel.mcse.ardoco.core.api.stage.connectiongenerator.artemis;

import java.io.Serial;
import java.io.Serializable;

import edu.kit.kastel.mcse.ardoco.core.api.models.Metamodel;
import edu.kit.kastel.mcse.ardoco.naer.model.NamedEntityType;

public record ArtemisTarget(Metamodel metamodel, NamedEntityType entityType) implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    public String getId() {
        return metamodel.name() + ":" + entityType.name();
    }
}
