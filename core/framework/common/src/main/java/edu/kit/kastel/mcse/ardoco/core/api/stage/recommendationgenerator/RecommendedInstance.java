/* Licensed under MIT 2021-2025. */
package edu.kit.kastel.mcse.ardoco.core.api.stage.recommendationgenerator;

import java.io.Serial;

import org.eclipse.collections.api.list.ImmutableList;

import edu.kit.kastel.mcse.ardoco.core.api.entity.TextEntity;
import edu.kit.kastel.mcse.ardoco.core.api.stage.textextraction.NounMapping;
import edu.kit.kastel.mcse.ardoco.core.pipeline.agent.Claimant;

/**
 * Represents an aggregation of noun mappings to one recommendation.
 */
public abstract class RecommendedInstance extends TextEntity {

    @Serial
    private static final long serialVersionUID = -5422301094494768943L;

    /**
     * Creates a new recommended instance.
     *
     * @param name the name
     * @param id   the identifier
     */
    protected RecommendedInstance(String name, String id) {
        super(name, id);
    }

    /**
     * Returns the involved name mappings.
     *
     * @return the name mappings
     */
    public abstract ImmutableList<NounMapping> getNameMappings();

    /**
     * Returns the involved type mappings.
     *
     * @return the type mappings
     */
    public abstract ImmutableList<NounMapping> getTypeMappings();

    /**
     * Returns the probability of being an instance of the model.
     *
     * @return the probability
     */
    public abstract double getProbability();

    /**
     * Adds a probability to the recommended instance.
     *
     * @param claimant    the claimant
     * @param probability the confidence
     */
    public abstract void addProbability(Claimant claimant, double probability);

    /**
     * Adds name and type mappings to this recommended instance.
     *
     * @param nameMapping the name mappings
     * @param typeMapping the type mappings
     */
    public abstract void addMappings(ImmutableList<NounMapping> nameMapping, ImmutableList<NounMapping> typeMapping);

    /**
     * Adds a name mapping to this recommended instance.
     *
     * @param nameMapping the name mapping
     */
    public abstract void addName(NounMapping nameMapping);

    /**
     * Adds a type mapping to this recommended instance.
     *
     * @param typeMapping the type mapping
     */
    public abstract void addType(NounMapping typeMapping);

    /**
     * Returns the type as string from this recommended instance.
     *
     * @return the type as string
     */
    public abstract String getType();

    /**
     * Returns the name as string from this recommended instance.
     *
     * @return the name as string
     */
    @Override
    public abstract String getName();

    /**
     * Handles the deletion of a noun mapping by replacing it with another.
     *
     * @param nounMapping the noun mapping to delete
     * @param replacement the replacement noun mapping
     */
    public abstract void onNounMappingDeletion(NounMapping nounMapping, NounMapping replacement);
}
