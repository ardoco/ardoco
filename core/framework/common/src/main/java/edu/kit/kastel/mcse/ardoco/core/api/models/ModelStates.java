/* Licensed under MIT 2022-2024. */
package edu.kit.kastel.mcse.ardoco.core.api.models;

import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.Model;
import edu.kit.kastel.mcse.ardoco.core.data.PipelineStepData;

public class ModelStates implements PipelineStepData {
    public static final String ID = "ModelStatesData";
    private static final long serialVersionUID = -603436842247064371L;
    private final SortedMap<Metamodel, Model> models = new TreeMap<>();

    /**
     * Constructor to create a {@link ModelStates} object that holds all {@link Model}s
     */
    public ModelStates() {
    }

    /*
    /**
     * Returns the {@link LegacyModelExtractionState} with the given id
     *
     * @param id the id
     * @return the corresponding {@link LegacyModelExtractionState}
     * @deprecated use {@link #getModel(Metamodel)} instead
     /
    @Deprecated
    public LegacyModelExtractionState getModelExtractionState(Metamodel id) {
        if (this.legacyModels.containsKey(id)) {
            return this.legacyModels.get(id);
        }
    
        var model = this.models.get(id);
        if (model == null) {
            return null;
        }
    
        var legacyModel = switch (model) {
            case ArchitectureModel am -> new LegacyModelExtractionStateByArCoTL(am);
            case CodeModel cm -> new LegacyModelExtractionStateByArCoTL(cm);
        };
    
        this.legacyModels.put(id, legacyModel);
        return legacyModel;
    }
    */

    /**
     * Return the set of IDs of all {@link Model Models} that are contained within this object.
     *
     * @return the IDs of all contained {@link Model Models}
     */
    public SortedSet<Metamodel> modelIds() {
        return new TreeSet<>(this.models.keySet());
    }

    /**
     * Adds a {@link Model} with the given id to the set of {@link Model Models}
     *
     * @param id    the id
     * @param model the {@link Model}
     */
    public void addModel(Metamodel id, Model model) {
        this.models.put(id, model);
    }

    /**
     * Returns the {@link Model} with the given id
     *
     * @param id the id
     * @return the corresponding {@link Model}
     */
    public Model getModel(Metamodel id) {
        return this.models.get(id);
    }

}
