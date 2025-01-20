/* Licensed under MIT 2021-2024. */
package edu.kit.kastel.mcse.ardoco.core.api.stage.recommendationgenerator;

import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.set.sorted.ImmutableSortedSet;

import edu.kit.kastel.mcse.ardoco.core.api.entity.TextEntity;
import edu.kit.kastel.mcse.ardoco.core.api.stage.textextraction.NounMapping;
import edu.kit.kastel.mcse.ardoco.core.pipeline.agent.Claimant;

/**
 * The Interface IRecommendedInstance defines the aggregation of noun mappings to one recommendation.
 */
public abstract class RecommendedInstance extends TextEntity {

    private static final long serialVersionUID = -5422301094494768943L;

    protected RecommendedInstance(String name, String id) {
        super(name, id);
    }

    /**
     * Returns the involved name mappings.
     *
     * @return the name mappings of this recommended instance
     */
    public abstract ImmutableList<NounMapping> getNameMappings();

    /**
     * Returns the involved type mappings.
     *
     * @return the type mappings of this recommended instance
     */
    public abstract ImmutableList<NounMapping> getTypeMappings();

    /**
     * Returns the probability being an instance of the model.
     *
     * @return the probability to be found in the model
     */
    public abstract double getProbability();

    /**
     * Adds a probability to the recommended instance
     *
     * @param claimant    the claimant of the confidence
     * @param probability the confidence
     */
    public abstract void addProbability(Claimant claimant, double probability);

    /**
     * Adds a name and type mapping to this recommended instance.
     *
     * @param nameMapping the name mapping to add
     * @param typeMapping the type mapping to add
     */
    public abstract void addMappings(NounMapping nameMapping, NounMapping typeMapping);

    /**
     * Adds name and type mappings to this recommended instance.
     *
     * @param nameMapping the name mappings to add
     * @param typeMapping the type mappings to add
     */
    public abstract void addMappings(ImmutableList<NounMapping> nameMapping, ImmutableList<NounMapping> typeMapping);

    /**
     * Adds a name mapping to this recommended instance.
     *
     * @param nameMapping the name mapping to add
     */
    public abstract void addName(NounMapping nameMapping);

    /**
     * Adds a type mapping to this recommended instance.
     *
     * @param typeMapping the type mapping to add
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

    public abstract ImmutableSortedSet<Integer> getSentenceNumbers();

    public abstract ImmutableList<Claimant> getClaimants();

    public abstract void onNounMappingDeletion(NounMapping nounMapping, NounMapping replacement);
}
