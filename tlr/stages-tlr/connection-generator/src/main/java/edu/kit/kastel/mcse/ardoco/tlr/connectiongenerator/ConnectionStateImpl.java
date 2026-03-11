/* Licensed under MIT 2021-2025. */
package edu.kit.kastel.mcse.ardoco.tlr.connectiongenerator;

import java.io.Serial;
import java.util.Collection;

import edu.kit.kastel.mcse.ardoco.core.api.entity.ArchitectureEntity;
import edu.kit.kastel.mcse.ardoco.core.api.stage.connectiongenerator.SentenceModelTraceLink;
import edu.kit.kastel.mcse.ardoco.core.api.stage.textextraction.NounMapping;
import edu.kit.kastel.mcse.ardoco.core.api.text.SentenceEntity;

import edu.kit.kastel.mcse.ardoco.core.common.persistence.PersistenceBridge;

import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.factory.Sets;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.list.MutableList;

import edu.kit.kastel.mcse.ardoco.core.api.entity.ModelEntity;
import edu.kit.kastel.mcse.ardoco.core.api.stage.connectiongenerator.ConnectionState;
import edu.kit.kastel.mcse.ardoco.core.api.stage.connectiongenerator.RecommendationModelTraceLink;
import edu.kit.kastel.mcse.ardoco.core.api.stage.recommendationgenerator.RecommendedInstance;
import edu.kit.kastel.mcse.ardoco.core.api.tracelink.TraceLink;
import edu.kit.kastel.mcse.ardoco.core.data.AbstractState;
import edu.kit.kastel.mcse.ardoco.core.pipeline.agent.Claimant;

import org.eclipse.collections.api.set.MutableSet;

/**
 * The connection state encapsulates all connections between the model extraction state and the recommendation state. These connections are stored in instance
 * and relation links.
 */
public class ConnectionStateImpl extends AbstractState implements ConnectionState {

    @Serial
    private static final long serialVersionUID = 3340998661239696150L;
    private final MutableList<TraceLink<RecommendedInstance, ModelEntity>> instanceLinks;

    /**
     * Creates a new connection state.
     */
    public ConnectionStateImpl() {
        super();
        this.instanceLinks = Lists.mutable.empty();
    }

    /**
     * Returns all instance links.
     *
     * @return all instance links
     */
    @Override
    public ImmutableList<TraceLink<RecommendedInstance, ModelEntity>> getInstanceLinks() {
        return Lists.immutable.withAll(this.instanceLinks);
    }

    /**
     * Adds the connection of a recommended instance and a model instance to the state. If the model instance is already contained by the state it is extended.
     * Elsewhere, a new instance link is created
     *
     * @param recommendedModelInstance the recommended instance
     * @param modelEntity              the model instance
     * @param probability              the probability of the link
     */
    @Override
    public void addToLinks(RecommendedInstance recommendedModelInstance, ModelEntity modelEntity, Claimant claimant, double probability) {

        boolean shouldPersist = PersistenceBridge.isAvailable() && modelEntity instanceof ArchitectureEntity;
        MutableSet<TraceLink<SentenceEntity, ModelEntity>> linksToPersist = Sets.mutable.empty();

        var newInstanceLink = new RecommendationModelTraceLink(recommendedModelInstance, modelEntity, claimant, probability);
        if (!this.isContainedByInstanceLinks(newInstanceLink)) {
            this.instanceLinks.add(newInstanceLink);

            if (shouldPersist) {
                linksToPersist.addAll(generateLinksFromInstance(recommendedModelInstance, modelEntity));
            }

        } else {
            var optionalInstanceLink = this.instanceLinks.stream().filter(il -> il.equals(newInstanceLink)).findFirst();
            if (optionalInstanceLink.isPresent()) {
                var existingInstanceLink = optionalInstanceLink.get();
                var newNameMappings = newInstanceLink.getFirstEndpoint().getNameMappings();
                var newTypeMappings = newInstanceLink.getFirstEndpoint().getTypeMappings();
                existingInstanceLink.getFirstEndpoint().addMappings(newNameMappings, newTypeMappings);

                if (shouldPersist) {
                    for (var nm : newNameMappings) {
                        linksToPersist.addAll(generateLinksFromNameMapping(nm, modelEntity));
                    }
                }
            }
        }

        if (!linksToPersist.isEmpty()) {
            PersistenceBridge.getHandler().saveSentenceModelTraceLinks(linksToPersist);
        }
    }

    private Collection<SentenceModelTraceLink> generateLinksFromInstance(RecommendedInstance recommendedInstance, ModelEntity modelEntity) {
        return recommendedInstance.getNameMappings().stream()
                .flatMap(nm -> generateLinksFromNameMapping(nm, modelEntity).stream())
                .toList();
    }

    private Collection<SentenceModelTraceLink> generateLinksFromNameMapping(NounMapping nm, ModelEntity modelEntity) {
        return nm.getWords().stream()
                .map(word -> new SentenceModelTraceLink(word.getSentence(), modelEntity))
                .toList();
    }


    /**
     * Checks if an instance link is already contained by the state.
     *
     * @param instanceLink the given instance link
     * @return true if it is already contained
     */
    @Override
    public boolean isContainedByInstanceLinks(TraceLink<RecommendedInstance, ModelEntity> instanceLink) {
        return this.instanceLinks.contains(instanceLink);
    }

}
