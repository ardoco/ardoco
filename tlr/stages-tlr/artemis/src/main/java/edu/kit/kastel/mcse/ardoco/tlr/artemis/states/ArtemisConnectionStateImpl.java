package edu.kit.kastel.mcse.ardoco.tlr.artemis.states;

import java.io.Serial;
import java.util.Collection;
import java.util.SortedSet;
import java.util.TreeSet;

import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.factory.SortedSets;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.list.MutableList;

import edu.kit.kastel.mcse.ardoco.core.api.entity.ModelEntity;
import edu.kit.kastel.mcse.ardoco.core.api.stage.connectiongenerator.artemis.ArtemisConnectionState;
import edu.kit.kastel.mcse.ardoco.core.api.stage.connectiongenerator.artemis.NamedArchitectureEntity;
import edu.kit.kastel.mcse.ardoco.core.api.stage.connectiongenerator.artemis.NamedArchitectureEntityOccurrence;
import edu.kit.kastel.mcse.ardoco.core.api.tracelink.TraceLink;
import edu.kit.kastel.mcse.ardoco.core.architecture.Deterministic;
import edu.kit.kastel.mcse.ardoco.core.data.AbstractState;

/**
 * Connection state for one ArTEMiS target, e.g. components, classes, or functions.
 */
@Deterministic
public class ArtemisConnectionStateImpl extends AbstractState implements ArtemisConnectionState {

    @Serial
    private static final long serialVersionUID = 1L;

    private final SortedSet<NamedArchitectureEntity> namedEntities;
    private final MutableList<TraceLink<NamedArchitectureEntityOccurrence, ModelEntity>> traceLinks;
    private final SortedSet<NamedArchitectureEntity> unlinkedNamedEntities;

    public ArtemisConnectionStateImpl() {
        super();
        namedEntities = SortedSets.mutable.empty();
        traceLinks = Lists.mutable.empty();
        unlinkedNamedEntities = SortedSets.mutable.empty();
    }

    @Override
    public boolean addNamedEntities(Collection<NamedArchitectureEntity> namedEntities) {
        return this.namedEntities.addAll(namedEntities);
    }

    @Override
    public boolean addTraceLinks(Collection<TraceLink<NamedArchitectureEntityOccurrence, ModelEntity>> traceLinks) {
        return this.traceLinks.addAll(traceLinks);
    }

    @Override
    public boolean addUnlinkedNamedEntities(Collection<NamedArchitectureEntity> unlinkedNamedEntities) {
        return this.unlinkedNamedEntities.addAll(unlinkedNamedEntities);
    }

    @Override
    public SortedSet<NamedArchitectureEntity> getNamedEntities() {
        return new TreeSet<>(this.namedEntities);
    }

    @Override
    public ImmutableList<TraceLink<NamedArchitectureEntityOccurrence, ModelEntity>> getTraceLinks() {
        return Lists.immutable.withAll(this.traceLinks);
    }

    @Override
    public SortedSet<NamedArchitectureEntity> getUnlinkedNamedEntities() {
        return new TreeSet<>(this.unlinkedNamedEntities);
    }
}
