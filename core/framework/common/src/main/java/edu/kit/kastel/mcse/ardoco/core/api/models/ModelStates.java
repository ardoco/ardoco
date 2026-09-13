/* Licensed under MIT 2022-2026. */
package edu.kit.kastel.mcse.ardoco.core.api.models;

import java.io.Serial;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.kit.kastel.mcse.ardoco.core.common.persistence.PersistenceBridge;
import edu.kit.kastel.mcse.ardoco.core.data.PipelineStepData;

/**
 * Holds all models for a pipeline step.
 */
public final class ModelStates implements PipelineStepData {

    // TODO(neo4j-diagnostic): temporary logging to confirm whether the Neo4j model reload is lossy (20-vs-16). Remove once root-caused.
    private static final Logger LOGGER = LoggerFactory.getLogger(ModelStates.class);

    /**
     * The ID for this data object.
     */
    public static final String ID = "ModelStatesData";

    @Serial
    private static final long serialVersionUID = -603436842247064371L;
    private final SortedMap<Metamodel, Model> models = new TreeMap<>();

    // TODO: think about whether the currently implemented caching of neo4j persisted models is fine or whether we should do it differently
    // If a Metamodel is in this set, it must be re-loaded from the DB
    private final SortedSet<Metamodel> dirtyMetamodels = new TreeSet<>();

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
        this.models.put(id, model);
        if ((id.isArchitectureModel() || id.isCodeModel()) && PersistenceBridge.isAvailable()) {
            PersistenceBridge.runQuietly("saveModel", () -> PersistenceBridge.getHandler().saveModel(id, model));
            this.dirtyMetamodels.add(id);
        }
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
        boolean needsLoading = !this.models.containsKey(id) || this.dirtyMetamodels.contains(id);

        if (needsLoading && isPersistentType && persistenceAvailable) {
            // TODO(neo4j-diagnostic): temporary logging to confirm whether the Neo4j model reload is lossy (20-vs-16). Remove once root-caused.
            Model inMemory = this.models.get(id);
            Model loaded = PersistenceBridge.callQuietly("loadModel", () -> PersistenceBridge.getHandler().loadModel(id), null);
            if (loaded != null) {
                if (inMemory != null) {
                    LOGGER.info("[neo4j-diagnostic] Reloading model {} from Neo4j: in-memory content={}, endpoints={} -> reloaded content={}, endpoints={}", id,
                            inMemory.getContent().size(), inMemory.getEndpoints().size(), loaded.getContent().size(), loaded.getEndpoints().size());
                } else {
                    LOGGER.info("[neo4j-diagnostic] Loading model {} from Neo4j (not in memory): content={}, endpoints={}", id, loaded.getContent().size(),
                            loaded.getEndpoints().size());
                }
                this.models.put(id, loaded);
                this.dirtyMetamodels.remove(id);
                return loaded;
            }
            LOGGER.info("[neo4j-diagnostic] Neo4j reload of model {} returned null; keeping in-memory model (content={})", id,
                    inMemory != null ? inMemory.getContent().size() : -1);
        }

        if (this.models.containsKey(id)) {
            return this.models.get(id);
        }

        throw new IllegalArgumentException("Model with id " + id.toString() + " not found");
    }

}
