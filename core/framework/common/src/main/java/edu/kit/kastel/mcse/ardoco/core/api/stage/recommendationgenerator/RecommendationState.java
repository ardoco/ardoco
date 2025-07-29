/* Licensed under MIT 2021-2025. */
package edu.kit.kastel.mcse.ardoco.core.api.stage.recommendationgenerator;

import org.eclipse.collections.api.list.ImmutableList;

import edu.kit.kastel.mcse.ardoco.core.api.stage.textextraction.NounMapping;
import edu.kit.kastel.mcse.ardoco.core.configuration.IConfigurable;
import edu.kit.kastel.mcse.ardoco.core.pipeline.agent.Claimant;

/**
 * State interface for recommendations. Provides access to recommended instances and related operations.
 */
public interface RecommendationState extends IConfigurable {

    /**
     * Returns all recommended instances.
     *
     * @return all recommended instances as list
     */
    ImmutableList<RecommendedInstance> getRecommendedInstances();

    /**
     * Adds a recommended instance without a type.
     *
     * @param name         name of the recommended instance
     * @param claimant     the claimant
     * @param probability  probability of being in the model
     * @param nameMappings name mappings representing the recommended instance
     */
    void addRecommendedInstance(String name, Claimant claimant, double probability, ImmutableList<NounMapping> nameMappings);

    /**
     * Adds a recommended instance.
     *
     * @param name         name of the recommended instance
     * @param type         type of the recommended instance
     * @param claimant     the claimant
     * @param probability  probability of being in the model
     * @param nameMappings name mappings representing the name
     * @param typeMappings type mappings representing the type
     * @return the added recommended instance
     */
    RecommendedInstance addRecommendedInstance(String name, String type, Claimant claimant, double probability, ImmutableList<NounMapping> nameMappings,
            ImmutableList<NounMapping> typeMappings);

    /**
     * Sync noun mappings. This method will be invoked if a noun mapping gets removed from the text extraction state.
     *
     * @param nounMapping the noun mapping to delete
     * @param replacement the replacement noun mapping
     */
    void onNounMappingDeletion(NounMapping nounMapping, NounMapping replacement);

}
