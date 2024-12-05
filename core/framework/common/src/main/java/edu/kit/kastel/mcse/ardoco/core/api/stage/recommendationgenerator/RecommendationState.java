/* Licensed under MIT 2021-2023. */
package edu.kit.kastel.mcse.ardoco.core.api.stage.recommendationgenerator;

import org.eclipse.collections.api.list.ImmutableList;

import edu.kit.kastel.mcse.ardoco.core.api.stage.textextraction.NounMapping;
import edu.kit.kastel.mcse.ardoco.core.configuration.IConfigurable;
import edu.kit.kastel.mcse.ardoco.core.pipeline.agent.Claimant;

/**
 * The Interface IRecommendationState defines the state for recommendations.
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
     * @param name         name of that recommended instance
     * @param probability  probability of being in the model
     * @param nameMappings name mappings representing that recommended instance
     */
    void addRecommendedInstance(String name, Claimant claimant, double probability, ImmutableList<NounMapping> nameMappings);

    /**
     * Adds a recommended instance.
     *
     * @param name         name of that recommended instance
     * @param type         type of that recommended instance
     * @param probability  probability of being in the model
     * @param nameMappings name mappings representing the name of the recommended instance
     * @param typeMappings type mappings representing the type of the recommended instance
     * @return the added recommended instance
     */
    RecommendedInstance addRecommendedInstance(String name, String type, Claimant claimant, double probability, ImmutableList<NounMapping> nameMappings,
            ImmutableList<NounMapping> typeMappings);

    /**
     * Returns all recommended instances that contain a given mapping as type.
     *
     * @param mapping given mapping to search for in types
     * @return the list of recommended instances with the mapping as type.
     */
    ImmutableList<RecommendedInstance> getRecommendedInstancesByTypeMapping(NounMapping mapping);

    /**
     * Returns all recommended instances that contain a given mapping.
     *
     * @param mapping given mapping to search for
     * @return the list of recommended instances with the mapping.
     */
    ImmutableList<RecommendedInstance> getAnyRecommendedInstancesByMapping(NounMapping mapping);

    /**
     * Returns all recommended instances that contain a given name.
     *
     * @param name given name to search for in names
     * @return the list of recommended instances with that name.
     */
    ImmutableList<RecommendedInstance> getRecommendedInstancesByName(String name);

    /**
     * Returns all recommended instances that contain a similar name.
     *
     * @param name given name to search for in names
     * @return the list of recommended instances with a similar name.
     */
    ImmutableList<RecommendedInstance> getRecommendedInstancesBySimilarName(String name);

    /**
     * Returns all recommended instances that contain a given name and type.
     *
     * @param type given type to search for in types
     * @return the list of recommended instances with that name and type
     */
    ImmutableList<RecommendedInstance> getRecommendedInstancesByType(String type);

    /**
     * Returns all recommended instances that contain a similar type.
     *
     * @param type given type to search for in types
     * @return the list of recommended instances with a similar type.
     */
    ImmutableList<RecommendedInstance> getRecommendedInstancesBySimilarType(String type);

}
