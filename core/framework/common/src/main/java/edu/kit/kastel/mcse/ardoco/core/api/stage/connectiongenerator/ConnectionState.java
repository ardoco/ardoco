/* Licensed under MIT 2021-2025. */
package edu.kit.kastel.mcse.ardoco.core.api.stage.connectiongenerator;

import org.eclipse.collections.api.factory.Sets;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.set.ImmutableSet;
import org.eclipse.collections.api.set.MutableSet;

import edu.kit.kastel.mcse.ardoco.core.api.entity.ModelEntity;
import edu.kit.kastel.mcse.ardoco.core.api.stage.recommendationgenerator.RecommendedInstance;
import edu.kit.kastel.mcse.ardoco.core.api.text.SentenceEntity;
import edu.kit.kastel.mcse.ardoco.core.api.tracelink.TraceLink;
import edu.kit.kastel.mcse.ardoco.core.architecture.Deterministic;
import edu.kit.kastel.mcse.ardoco.core.configuration.IConfigurable;
import edu.kit.kastel.mcse.ardoco.core.pipeline.agent.Claimant;

/**
 * The Interface IConnectionState.
 */
@Deterministic
public interface ConnectionState extends IConfigurable {

    /**
     * Returns all instance links.
     *
     * @return all instance links
     */
    ImmutableList<TraceLink<RecommendedInstance, ModelEntity>> getInstanceLinks();

    /**
     * Returns all instance links with a model instance containing the given name.
     *
     * @param name the name of a model instance
     * @return all instance links with a model instance containing the given name as list
     */
    ImmutableList<TraceLink<RecommendedInstance, ModelEntity>> getInstanceLinksByName(String name);

    /**
     * Returns all instance links with a model instance containing the given type.
     *
     * @param type the type of a model instance
     * @return all instance links with a model instance containing the given type as list
     */
    ImmutableList<TraceLink<RecommendedInstance, ModelEntity>> getInstanceLinksByType(String type);

    /**
     * Returns all instance links with a model instance containing the given recommended instance.
     *
     * @param recommendedInstance the recommended instance to consider
     * @return all instance links found
     */
    ImmutableList<TraceLink<RecommendedInstance, ModelEntity>> getInstanceLinksByRecommendedInstance(RecommendedInstance recommendedInstance);

    /**
     * Returns all instance links with a model instance containing the given name and type.
     *
     * @param name the name of a model instance
     * @param type the type of a model instance
     * @return all instance links with a model instance containing the given name and type as list
     */
    ImmutableList<TraceLink<RecommendedInstance, ModelEntity>> getInstanceLinks(String name, String type);

    /**
     * Returns a list of tracelinks that are contained within this connection state.
     *
     * @return list of tracelinks within this connection state
     */
    default ImmutableSet<TraceLink<SentenceEntity, ModelEntity>> getTraceLinks() {
        MutableSet<TraceLink<SentenceEntity, ModelEntity>> traceLinks = Sets.mutable.empty();
        for (var instanceLink : this.getInstanceLinks()) {
            var textualInstance = instanceLink.getFirstEndpoint();
            for (var nm : textualInstance.getNameMappings()) {
                for (var word : nm.getWords()) {
                    var traceLink = new SadModelTraceLink(word.getSentence(), instanceLink.getSecondEndpoint());
                    traceLinks.add(traceLink);
                }
            }
        }
        return traceLinks.toImmutable();
    }

    /**
     * Adds the connection of a recommended instance and a model instance to the state. If the model instance is already contained by the state it is extended.
     * Elsewhere a new instance link is created
     *
     * @param recommendedModelInstance the recommended instance
     * @param ModelEntity              the model instance
     * @param claimant                 the claimant
     * @param probability              the probability of the link
     */
    void addToLinks(RecommendedInstance recommendedModelInstance, ModelEntity ModelEntity, Claimant claimant, double probability);

    /**
     * Checks if an instance link is already contained by the state.
     *
     * @param instanceLink the given instance link
     * @return true if it is already contained
     */
    boolean isContainedByInstanceLinks(TraceLink<RecommendedInstance, ModelEntity> instanceLink);

    /**
     * Removes an instance link from the state.
     *
     * @param instanceMapping the instance link to remove
     */
    void removeFromMappings(TraceLink<RecommendedInstance, ModelEntity> instanceMapping);

    /**
     * Removes all instance links containing the given instance.
     *
     * @param instance the given instance
     */
    void removeAllInstanceLinksWith(ModelEntity instance);

    /**
     * Removes all instance links containing the given recommended instance.
     *
     * @param instance the given recommended instance
     */
    void removeAllInstanceLinksWith(RecommendedInstance instance);

}
