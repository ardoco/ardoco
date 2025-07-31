/* Licensed under MIT 2025. */
package edu.kit.kastel.mcse.ardoco.core.api.stage.connectiongenerator.ner;

import java.util.Collection;

import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.set.ImmutableSet;

import edu.kit.kastel.mcse.ardoco.core.api.entity.ModelEntity;
import edu.kit.kastel.mcse.ardoco.core.api.tracelink.TraceLink;
import edu.kit.kastel.mcse.ardoco.core.configuration.IConfigurable;
import edu.kit.kastel.mcse.ardoco.core.pipeline.agent.Claimant;

public interface NerConnectionState extends IConfigurable {
    /**
     * Returns all instance links.
     *
     * @return all instance links
     */
    ImmutableList<TraceLink<NamedArchitectureEntityOccurrence, ModelEntity>> getTraceLinks();

    /**
     * Adds the connection of a recommended instance and a model instance to the state. If the model instance is already contained by the state it is extended,
     * otherwise a new instance link is created.
     *
     * @param namedArchitectureEntityOccurrence the recommended instance
     * @param modelEntity                       the model instance
     * @param claimant                          the claimant
     * @param probability                       the probability of the link
     */
    void addToLinks(NamedArchitectureEntityOccurrence namedArchitectureEntityOccurrence, ModelEntity modelEntity, Claimant claimant, double probability);

    ImmutableSet<NamedArchitectureEntity> getNamedArchitectureEntities();

    void addNamedEntities(Collection<NamedArchitectureEntity> namedArchitectureEntities);
}
