/* Licensed under MIT 2023-2026. */
package edu.kit.kastel.mcse.ardoco.tlr.codetraceability;

import java.io.Serial;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

import edu.kit.kastel.mcse.ardoco.core.api.stage.connectiongenerator.SentenceModelTraceLink;

import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.MutableList;
import org.eclipse.collections.api.set.ImmutableSet;
import org.eclipse.collections.impl.factory.Sets;

import edu.kit.kastel.mcse.ardoco.core.api.entity.ArchitectureEntity;
import edu.kit.kastel.mcse.ardoco.core.api.entity.ModelEntity;
import edu.kit.kastel.mcse.ardoco.core.api.stage.codetraceability.ArchitectureCodeTraceLink;
import edu.kit.kastel.mcse.ardoco.core.api.stage.codetraceability.CodeTraceabilityState;
import edu.kit.kastel.mcse.ardoco.core.api.text.SentenceEntity;
import edu.kit.kastel.mcse.ardoco.core.api.tracelink.TraceLink;
import edu.kit.kastel.mcse.ardoco.core.architecture.Deterministic;
import edu.kit.kastel.mcse.ardoco.core.common.persistence.PersistenceBridge;
import edu.kit.kastel.mcse.ardoco.core.data.AbstractState;

@Deterministic
public class CodeTraceabilityStateImpl extends AbstractState implements CodeTraceabilityState {

    @Serial
    private static final long serialVersionUID = 8036723521626754832L;
    private MutableList<TraceLink<? extends ArchitectureEntity, ? extends ModelEntity>> samCodeTraceLinks = Lists.mutable.empty();
    private MutableList<TraceLink<SentenceEntity, ? extends ModelEntity>> transitiveTraceLinks = Lists.mutable.empty();

    private transient boolean loadedFromPersistence = false;

    public CodeTraceabilityStateImpl() {
        super();
    }

    @Override
    public boolean addSamCodeTraceLinks(Collection<? extends TraceLink<? extends ArchitectureEntity, ? extends ModelEntity>> traceLinks) {
        if (PersistenceBridge.isAvailable()) {
            return PersistenceBridge.getHandler().saveTraceLinks(traceLinks);
        }
        return this.samCodeTraceLinks.addAll(traceLinks);
    }

    @Override
    public ImmutableSet<TraceLink<? extends ArchitectureEntity, ? extends ModelEntity>> getSamCodeTraceLinks() {
        if ((this.samCodeTraceLinks.isEmpty() || !loadedFromPersistence) && PersistenceBridge.isAvailable()) {
            Collection<ArchitectureCodeTraceLink> loadedLinks = PersistenceBridge.getHandler().loadArchitectureCodeTraceLinks();
            this.samCodeTraceLinks = Lists.mutable.withAll(loadedLinks);
            loadedFromPersistence = true;
        }

        return Sets.immutable.withAll(new LinkedHashSet<>(this.samCodeTraceLinks));
    }

    @Override
    public boolean addSadCodeTraceLinks(Collection<? extends TraceLink<SentenceEntity, ? extends ModelEntity>> traceLinks) {
        if (PersistenceBridge.isAvailable()) {
            return PersistenceBridge.getHandler().saveTraceLinks(traceLinks);
        }
        return this.transitiveTraceLinks.addAll(traceLinks);
    }

    @Override
    public ImmutableSet<TraceLink<SentenceEntity, ? extends ModelEntity>> getSadCodeTraceLinks() {
        if (PersistenceBridge.isAvailable()) {
            Collection<? extends TraceLink<SentenceEntity, ? extends ModelEntity>> transitiveLinks = PersistenceBridge.getHandler().loadTransitiveTraceLinks();
            Collection<SentenceModelTraceLink> directLinks = PersistenceBridge.getHandler().loadSentenceModelTraceLinks();
            this.transitiveTraceLinks = Lists.mutable.withAll(transitiveLinks);
            this.transitiveTraceLinks.addAll(directLinks);
            loadedFromPersistence = true;
        }

        return Sets.immutable.withAll(new LinkedHashSet<>(this.transitiveTraceLinks));
    }

}
