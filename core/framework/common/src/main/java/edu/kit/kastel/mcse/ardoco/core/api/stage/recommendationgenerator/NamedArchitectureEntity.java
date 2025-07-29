package edu.kit.kastel.mcse.ardoco.core.api.stage.recommendationgenerator;

import java.io.Serial;

import edu.kit.kastel.mcse.ardoco.core.api.entity.TextEntity;

public abstract class NamedArchitectureEntity extends TextEntity {

    @Serial
    private static final long serialVersionUID = 1988388774030529447L;

    /**
     * Creates a new text entity with the specified name and id.
     *
     * @param name the name of the text entity
     * @param id   the unique identifier
     */
    protected NamedArchitectureEntity(String name, String id) {
        super(name, id);
    }

    /**
     * Returns the name as string from this recommended instance.
     *
     * @return the name as string
     */
    @Override
    public abstract String getName();

    // TODO
}
