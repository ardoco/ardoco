/* Licensed under MIT 2021-2024. */
package edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.architecture.legacy;

import org.eclipse.collections.api.list.ImmutableList;

import edu.kit.kastel.mcse.ardoco.core.api.entity.ArchitectureEntity;

/**
 * The Interface IModelInstance defines instances from models.
 */
@Deprecated
public abstract sealed class ModelInstance extends ArchitectureEntity permits ModelInstanceImpl {

    private static final long serialVersionUID = 2351521910499184817L;

    protected ModelInstance(String name, String id) {
        super(name, id);
    }

    /**
     * Returns the full name of the instance.
     *
     * @return the original name of the instance
     */
    public abstract String getFullName();

    /**
     * Returns the full type of the instance.
     *
     * @return the original type of the instance
     */
    public abstract String getFullType();

    /**
     * Returns all name parts of the instance.
     *
     * @return all name parts of the instance as list
     */
    public abstract ImmutableList<String> getNameParts();

    /**
     * Returns all type parts of the instance.
     *
     * @return all type parts of the instance as list
     */
    public abstract ImmutableList<String> getTypeParts();

    /**
     * Returns the unique identifier of the instance.
     *
     * @return the unique identifier of the instance
     */
    public abstract String getUid();

}
