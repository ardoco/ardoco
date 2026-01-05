/* Licensed under MIT 2022-2025. */
package edu.kit.kastel.mcse.ardoco.core.api.models;

import java.io.Serial;
import java.util.HashSet;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import edu.kit.kastel.mcse.ardoco.core.common.persistence.PersistenceBridge;
import edu.kit.kastel.mcse.ardoco.core.data.PipelineStepData;

/**
 * Holds all models for a pipeline step.
 */
public final class ModelStates implements PipelineStepData {
    /**
     * The ID for this data object.
     */
    public static final String ID = "ModelStatesData";

    @Serial
    private static final long serialVersionUID = -603436842247064371L;
    private final SortedMap<Metamodel, Model> models = new TreeMap<>();

    // If a Metamodel is in this set, it must be re-loaded from the DB
    private final Set<Metamodel> dirtyMetamodels = new HashSet<>();
    /**
     * Return the set of IDs of all {@link Model Models} that are contained within this object.
     *
     * @return the IDs of all contained {@link Model Models}
     */
    public SortedSet<Metamodel> getMetamodels() {
        return new TreeSet<>(this.models.keySet());
    }

    /**
     * Adds a {@link Model} with the given id to the set of {@link Model Models}.
     *
     * @param id    the id
     * @param model the {@link Model}
     */
    public void addModel(Metamodel id, Model model) {
        // store the model in neo4j
        if ((id.isArchitectureModel() || id.isCodeModel()) && PersistenceBridge.isAvailable()) {
            PersistenceBridge.getHandler().saveModel(id, model);
            this.dirtyMetamodels.add(id);
        }
        this.models.put(id, model);
    }

    /**
     * Returns the {@link Model} with the given id.
     *
     * @param id the id
     * @return the corresponding {@link Model}
     */
    public Model getModel(Metamodel id) {
        boolean isPersistentType = id.isArchitectureModel() || id.isCodeModel();
        boolean persistenceAvailable = PersistenceBridge.isAvailable();

        // dirty check
        if (this.dirtyMetamodels.contains(id) && isPersistentType && persistenceAvailable) {
            Model loaded = PersistenceBridge.getHandler().loadModel(id);
            if (loaded != null) {
                this.models.put(id, loaded);    // Update Cache
                this.dirtyMetamodels.remove(id); // Mark Clean
                return loaded;
            }
        }

        // Cache hit
        if (this.models.containsKey(id)) {
            return this.models.get(id);
        }

        // Cache miss
        if (isPersistentType && persistenceAvailable) {
            Model loaded = PersistenceBridge.getHandler().loadModel(id);
            if (loaded != null) {
                this.models.put(id, loaded); // Fill Cache
                return loaded;
            }
        }

        throw new IllegalArgumentException("Model with id " + id.toString() + " not found");
    }

}
