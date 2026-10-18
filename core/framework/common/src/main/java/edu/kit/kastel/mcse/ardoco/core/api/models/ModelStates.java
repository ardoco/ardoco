/* Licensed under MIT 2022-2026. */
package edu.kit.kastel.mcse.ardoco.core.api.models;

import java.io.Serial;
import java.util.Collections;
import java.util.IdentityHashMap;
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

    // ModelProviderInformant registers the same extracted model object under two metamodel keys (extractedModel.getMetamodel()
    // and extractor.getMetamodel(), which resolve to the same value for e.g. PcmExtractor); persisting on every
    // addModel call would then save the identical model twice per run. Track which model instances have been persisted
    // already (by object identity, not Model.getId() - IdentifierProvider.reset() before every extraction means two
    // distinct models in one run can share an id) so a given model object is written to Neo4j only once, regardless
    // of how many keys it is registered under.
    private final Set<Model> persistedModels = Collections.newSetFromMap(new IdentityHashMap<>());

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
        if ((id.isArchitectureModel() || id.isCodeModel()) && PersistenceBridge.isAvailable() && persistedModels.add(model)) {
            PersistenceBridge.runQuietly("saveModel", () -> PersistenceBridge.getHandler().saveModel(id, model));
        }
    }

    /**
     * Returns the {@link Model} with the given id.
     *
     * @param id the id
     * @return the corresponding {@link Model}
     */
    public Model getModel(Metamodel id) {
        if (this.models.containsKey(id)) {
            return this.models.get(id);
        }

        throw new IllegalArgumentException("Model with id " + id.toString() + " not found");
    }

}
