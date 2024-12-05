/* Licensed under MIT 2023-2024. */
package edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.architecture;

import edu.kit.kastel.mcse.ardoco.core.api.entity.ArchitectureEntity;

/**
 * An architecture item of an architecture model. A possible candidate for the
 * architecture endpoint of a trace link that connects corresponding elements of
 * an architecture model and a code model.
 */
public abstract sealed class ArchitectureItem extends ArchitectureEntity permits ArchitectureComponent, ArchitectureInterface, ArchitectureMethod {

    private static final long serialVersionUID = -216185356134452192L;

    /**
     * Creates a new architecture item with the specified name.
     *
     * @param name the name of the architecture item to be created
     */
    protected ArchitectureItem(String name) {
        super(name);
    }

    /**
     * Creates a new architecture item with the specified name and identifier.
     *
     * @param name the name of the architecture item to be created
     * @param id   the identifier of the architecture item to be created
     */
    protected ArchitectureItem(String name, String id) {
        super(name, id);
    }
}
