/* Licensed under MIT 2021-2024. */
package edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.architecture.legacy;

import java.io.Serializable;

import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.set.sorted.ImmutableSortedSet;

import edu.kit.kastel.mcse.ardoco.core.api.models.Metamodel;
import edu.kit.kastel.mcse.ardoco.core.api.models.ModelStates;
import edu.kit.kastel.mcse.ardoco.core.configuration.IConfigurable;

/**
 * The Interface IModelState defines the information directly extracted from the models.
 *
 * @deprecated use {@link ModelStates#getModel(Metamodel)}
 */
@Deprecated(since = "0.32.0")
public interface Model extends IConfigurable, Serializable {
    /**
     * Returns the unique id of the model
     *
     * @return modelId
     */
    String getModelId();

    /**
     * Returns the metalevel or type of the current model
     *
     * @return type of model
     */
    Metamodel getMetamodel();

    /**
     * Returns the instances of a specific type.
     *
     * @param type the type to search for
     * @return all instances that are from that type
     */
    ImmutableList<ModelInstance> getInstancesOfType(String type);

    /**
     * Returns all types that are contained by instances of this state.
     *
     * @return all instance types of this state
     */
    ImmutableSortedSet<String> getInstanceTypes();

    /**
     * Returns all names that are contained by this state.
     *
     * @return all names of this state
     */
    ImmutableSortedSet<String> getNames();

    /**
     * Returns all instances that are contained by this state.
     *
     * @return all instances of this state
     */
    ImmutableList<ModelInstance> getInstances();

}
