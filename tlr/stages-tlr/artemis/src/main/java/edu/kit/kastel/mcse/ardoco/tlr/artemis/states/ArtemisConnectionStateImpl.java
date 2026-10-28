/* Licensed under MIT 2026. */
package edu.kit.kastel.mcse.ardoco.tlr.artemis.states;

import java.io.Serial;

import org.eclipse.collections.api.factory.Lists;
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

    private final MutableList<NamedArchitectureEntity> namedEntities;
    private final MutableList<TraceLink<NamedArchitectureEntityOccurrence, ModelEntity>> traceLinks;
    private final MutableList<NamedArchitectureEntity> unlinkedNamedEntities;

    public ArtemisConnectionStateImpl() {
        super();
        namedEntities = Lists.mutable.empty();
        traceLinks = Lists.mutable.empty();
        unlinkedNamedEntities = Lists.mutable.empty();
    }

    @Override
    public boolean addNamedEntities(MutableList<NamedArchitectureEntity> namedEntities) {
        return this.namedEntities.addAll(namedEntities);
    }

    @Override
    public boolean addTraceLinks(MutableList<TraceLink<NamedArchitectureEntityOccurrence, ModelEntity>> traceLinks) {
        return this.traceLinks.addAll(traceLinks);
    }

    @Override
    public boolean addUnlinkedNamedEntities(MutableList<NamedArchitectureEntity> namedEntities) {
        return this.unlinkedNamedEntities.addAll(namedEntities);
    }

    @Override
    public ImmutableList<NamedArchitectureEntity> getNamedEntities() {
        return Lists.immutable.withAll(this.namedEntities);
    }

    @Override
    public ImmutableList<TraceLink<NamedArchitectureEntityOccurrence, ModelEntity>> getTraceLinks() {
        return Lists.immutable.withAll(this.traceLinks);
    }

    @Override
    public ImmutableList<NamedArchitectureEntity> getUnlinkedNamedEntities() {
        return Lists.immutable.withAll(this.unlinkedNamedEntities);
    }
}
