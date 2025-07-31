/* Licensed under MIT 2025. */
package edu.kit.kastel.mcse.ardoco.tlr.connectiongenerator.ner;

import java.util.Collection;

import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.factory.Sets;
import org.eclipse.collections.api.factory.SortedSets;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.list.MutableList;
import org.eclipse.collections.api.set.MutableSet;
import org.eclipse.collections.api.set.sorted.ImmutableSortedSet;

import edu.kit.kastel.mcse.ardoco.core.api.entity.ModelEntity;
import edu.kit.kastel.mcse.ardoco.core.api.stage.connectiongenerator.ner.NamedArchitectureEntity;
import edu.kit.kastel.mcse.ardoco.core.api.stage.connectiongenerator.ner.NamedArchitectureEntityOccurrence;
import edu.kit.kastel.mcse.ardoco.core.api.stage.connectiongenerator.ner.NamedArchitectureEntityToModelTraceLink;
import edu.kit.kastel.mcse.ardoco.core.api.stage.connectiongenerator.ner.NerConnectionState;
import edu.kit.kastel.mcse.ardoco.core.api.tracelink.TraceLink;
import edu.kit.kastel.mcse.ardoco.core.data.AbstractState;
import edu.kit.kastel.mcse.ardoco.core.pipeline.agent.Claimant;

public class NerConnectionStateImpl extends AbstractState implements NerConnectionState {

    private final MutableList<TraceLink<NamedArchitectureEntityOccurrence, ModelEntity>> instanceLinks;
    private final MutableSet<NamedArchitectureEntity> namedEntities;

    /**
     * Creates a new connection state.
     */
    public NerConnectionStateImpl() {
        this.instanceLinks = Lists.mutable.empty();
        this.namedEntities = Sets.mutable.empty();
    }

    @Override
    public ImmutableList<TraceLink<NamedArchitectureEntityOccurrence, ModelEntity>> getTraceLinks() {
        return Lists.immutable.withAll(this.instanceLinks);
    }

    @Override
    public void addToLinks(NamedArchitectureEntityOccurrence namedArchitectureEntityOccurrence, ModelEntity modelEntity, Claimant claimant,
            double probability) {
        TraceLink<NamedArchitectureEntityOccurrence, ModelEntity> traceLink = new NamedArchitectureEntityToModelTraceLink(namedArchitectureEntityOccurrence,
                modelEntity, claimant, probability);
        if (!this.instanceLinks.contains(traceLink)) {
            this.instanceLinks.add(traceLink);
        }
    }

    @Override
    public ImmutableSortedSet<NamedArchitectureEntity> getNamedArchitectureEntities() {
        return SortedSets.immutable.withAll(this.namedEntities);
    }

    @Override
    public void addNamedEntities(Collection<NamedArchitectureEntity> namedArchitectureEntities) {
        this.namedEntities.addAll(namedArchitectureEntities);
    }

}
