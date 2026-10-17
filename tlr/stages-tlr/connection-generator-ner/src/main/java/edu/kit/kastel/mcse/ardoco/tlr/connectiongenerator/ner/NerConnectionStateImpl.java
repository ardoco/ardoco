/* Licensed under MIT 2025. */
package edu.kit.kastel.mcse.ardoco.tlr.connectiongenerator.ner;

import java.util.Collection;
import java.util.List;

import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.factory.SortedSets;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.list.MutableList;
import org.eclipse.collections.api.set.sorted.ImmutableSortedSet;
import org.eclipse.collections.api.set.sorted.MutableSortedSet;

import edu.kit.kastel.mcse.ardoco.core.api.entity.ModelEntity;
import edu.kit.kastel.mcse.ardoco.core.api.models.Metamodel;
import edu.kit.kastel.mcse.ardoco.core.api.stage.connectiongenerator.ner.NamedArchitectureEntity;
import edu.kit.kastel.mcse.ardoco.core.api.stage.connectiongenerator.ner.NamedArchitectureEntityOccurrence;
import edu.kit.kastel.mcse.ardoco.core.api.stage.connectiongenerator.ner.NamedArchitectureEntityToModelTraceLink;
import edu.kit.kastel.mcse.ardoco.core.api.stage.connectiongenerator.ner.NerConnectionState;
import edu.kit.kastel.mcse.ardoco.core.api.tracelink.TraceLink;
import edu.kit.kastel.mcse.ardoco.core.common.persistence.PersistenceBridge;
import edu.kit.kastel.mcse.ardoco.core.data.AbstractState;
import edu.kit.kastel.mcse.ardoco.core.pipeline.agent.Claimant;

public class NerConnectionStateImpl extends AbstractState implements NerConnectionState {

    private final MutableList<TraceLink<NamedArchitectureEntityOccurrence, ModelEntity>> instanceLinks;
    private final MutableSortedSet<NamedArchitectureEntity> namedEntities;
    private final MutableSortedSet<NamedArchitectureEntity> unlinkedNamedArchitectureEntities;
    private Metamodel metamodel;

    /**
     * Creates a new connection state.
     */
    public NerConnectionStateImpl() {
        this.instanceLinks = Lists.mutable.empty();
        this.namedEntities = SortedSets.mutable.empty();
        this.unlinkedNamedArchitectureEntities = SortedSets.mutable.empty();
    }

    public void setMetamodel(Metamodel metamodel) {
        this.metamodel = metamodel;
    }

    public Metamodel getMetamodel() {
        return metamodel;
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
            dualWriteTraceLink(traceLink);
        }
    }

    @Override
    public ImmutableSortedSet<NamedArchitectureEntity> getNamedArchitectureEntities() {
        return SortedSets.immutable.withAll(this.namedEntities);
    }

    @Override
    public void addNamedEntities(Collection<NamedArchitectureEntity> namedArchitectureEntities) {
        this.namedEntities.addAll(namedArchitectureEntities);
        dualWriteEntities(namedArchitectureEntities, false);
    }

    @Override
    public ImmutableSortedSet<NamedArchitectureEntity> getUnlinkedNamedArchitectureEntities() {
        return SortedSets.immutable.withAll(unlinkedNamedArchitectureEntities);
    }

    @Override
    public void addUnlinkedNamedArchitectureEntities(Collection<NamedArchitectureEntity> namedArchitectureEntities) {
        this.unlinkedNamedArchitectureEntities.addAll(namedArchitectureEntities);
        dualWriteEntities(namedArchitectureEntities, true);
    }

    private void dualWriteEntities(Collection<NamedArchitectureEntity> entities, boolean unlinked) {
        if (!PersistenceBridge.shouldPersistNerConnection() || this.metamodel == null || entities == null || entities.isEmpty()) {
            return;
        }
        for (NamedArchitectureEntity entity : entities) {
            PersistenceBridge.saveNamedArchitectureEntity(entity, this.metamodel, unlinked);
        }
    }

    private void dualWriteTraceLink(TraceLink<NamedArchitectureEntityOccurrence, ModelEntity> link) {
        if (!PersistenceBridge.shouldPersistNerConnection()) {
            return;
        }
        PersistenceBridge.saveTraceLinks(List.of(link));
    }

}
