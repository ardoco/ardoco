/* Licensed under MIT 2026. */
package edu.kit.kastel.mcse.ardoco.tlr.artemis.states;

import java.io.Serial;
import java.util.Objects;

import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.list.MutableList;

import edu.kit.kastel.mcse.ardoco.core.api.entity.ModelEntity;
import edu.kit.kastel.mcse.ardoco.core.api.stage.connectiongenerator.artemis.ArtemisConnectionState;
import edu.kit.kastel.mcse.ardoco.core.api.stage.connectiongenerator.artemis.ArtemisTarget;
import edu.kit.kastel.mcse.ardoco.core.api.stage.connectiongenerator.artemis.NamedArchitectureEntity;
import edu.kit.kastel.mcse.ardoco.core.api.stage.connectiongenerator.artemis.NamedArchitectureEntityOccurrence;
import edu.kit.kastel.mcse.ardoco.core.api.tracelink.TraceLink;
import edu.kit.kastel.mcse.ardoco.core.architecture.Deterministic;
import edu.kit.kastel.mcse.ardoco.core.common.persistence.PersistenceBridge;
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
    private final ArtemisTarget target;

    /**
     * Creates the connection state of one ArTEMiS target. The target is used as the key under which the named entities of this state are stored in
     * Neo4j storage mode.
     *
     * @param target the ArTEMiS target (metamodel and entity type) of this state
     */
    public ArtemisConnectionStateImpl(ArtemisTarget target) {
        super();
        this.target = Objects.requireNonNull(target);
        namedEntities = Lists.mutable.empty();
        traceLinks = Lists.mutable.empty();
        unlinkedNamedEntities = Lists.mutable.empty();
    }

    /**
     * Adds named entities and, in Neo4j storage mode, saves each of them for this state's target as linked.
     */
    @Override
    public boolean addNamedEntities(MutableList<NamedArchitectureEntity> namedEntities) {
        boolean added = this.namedEntities.addAll(namedEntities);
        for (NamedArchitectureEntity namedEntity : namedEntities) {
            PersistenceBridge.saveNamedArchitectureEntity(namedEntity, this.target, false);
        }
        return added;
    }

    /**
     * Adds trace links and, in Neo4j storage mode, saves them.
     */
    @Override
    public boolean addTraceLinks(MutableList<TraceLink<NamedArchitectureEntityOccurrence, ModelEntity>> traceLinks) {
        boolean added = this.traceLinks.addAll(traceLinks);
        PersistenceBridge.saveTraceLinks(traceLinks);
        return added;
    }

    /**
     * Adds unlinked named entities and, in Neo4j storage mode, saves each of them for this state's target as unlinked.
     */
    @Override
    public boolean addUnlinkedNamedEntities(MutableList<NamedArchitectureEntity> namedEntities) {
        boolean added = this.unlinkedNamedEntities.addAll(namedEntities);
        for (NamedArchitectureEntity namedEntity : namedEntities) {
            PersistenceBridge.saveNamedArchitectureEntity(namedEntity, this.target, true);
        }
        return added;
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
