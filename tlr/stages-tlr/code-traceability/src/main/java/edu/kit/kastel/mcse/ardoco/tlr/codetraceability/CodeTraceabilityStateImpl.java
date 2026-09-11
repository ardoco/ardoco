/* Licensed under MIT 2023-2026. */
package edu.kit.kastel.mcse.ardoco.tlr.codetraceability;

import java.io.Serial;
import java.util.Collection;
import java.util.LinkedHashSet;

import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.MutableList;
import org.eclipse.collections.api.set.ImmutableSet;
import org.eclipse.collections.impl.factory.Sets;

import edu.kit.kastel.mcse.ardoco.core.api.entity.ArchitectureEntity;
import edu.kit.kastel.mcse.ardoco.core.api.entity.ModelEntity;
import edu.kit.kastel.mcse.ardoco.core.api.stage.codetraceability.ArchitectureCodeTraceLink;
import edu.kit.kastel.mcse.ardoco.core.api.stage.codetraceability.CodeTraceabilityState;
import edu.kit.kastel.mcse.ardoco.core.api.stage.connectiongenerator.SentenceModelTraceLink;
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
        // Always keep in-memory copy so Neo4j outages do not lose links (fault isolation).
        boolean added = this.samCodeTraceLinks.addAll(traceLinks);
        if (PersistenceBridge.isAvailable()) {
            PersistenceBridge.runQuietly("saveSamCodeTraceLinks", () -> PersistenceBridge.getHandler().saveTraceLinks(traceLinks));
        }
        return added;
    }

    @Override
    public ImmutableSet<TraceLink<? extends ArchitectureEntity, ? extends ModelEntity>> getSamCodeTraceLinks() {
        if ((this.samCodeTraceLinks.isEmpty() || !loadedFromPersistence) && PersistenceBridge.isAvailable()) {
            Collection<ArchitectureCodeTraceLink> loadedLinks = PersistenceBridge.callQuietly("loadArchitectureCodeTraceLinks",
                    () -> PersistenceBridge.getHandler().loadArchitectureCodeTraceLinks(), java.util.List.of());
            if (!loadedLinks.isEmpty()) {
                this.samCodeTraceLinks = Lists.mutable.withAll(loadedLinks);
                loadedFromPersistence = true;
            }
        }

        return Sets.immutable.withAll(new LinkedHashSet<>(this.samCodeTraceLinks));
    }

    @Override
    public boolean addSadCodeTraceLinks(Collection<? extends TraceLink<SentenceEntity, ? extends ModelEntity>> traceLinks) {
        boolean added = this.transitiveTraceLinks.addAll(traceLinks);
        if (PersistenceBridge.isAvailable()) {
            PersistenceBridge.runQuietly("saveSadCodeTraceLinks", () -> PersistenceBridge.getHandler().saveTraceLinks(traceLinks));
        }
        return added;
    }

    @Override
    public ImmutableSet<TraceLink<SentenceEntity, ? extends ModelEntity>> getSadCodeTraceLinks() {
        if (PersistenceBridge.isAvailable()) {
            Collection<? extends TraceLink<SentenceEntity, ? extends ModelEntity>> transitiveLinks = PersistenceBridge.callQuietly("loadTransitiveTraceLinks",
                    () -> PersistenceBridge.getHandler().loadTransitiveTraceLinks(), java.util.List.of());
            Collection<SentenceModelTraceLink> directLinks = PersistenceBridge.callQuietly("loadSentenceModelTraceLinks",
                    () -> PersistenceBridge.getHandler().loadSentenceModelTraceLinks(), java.util.List.of());
            if (!transitiveLinks.isEmpty() || !directLinks.isEmpty()) {
                this.transitiveTraceLinks = Lists.mutable.withAll(transitiveLinks);
                this.transitiveTraceLinks.addAll(directLinks);
                loadedFromPersistence = true;
            }
        }

        return Sets.immutable.withAll(new LinkedHashSet<>(this.transitiveTraceLinks));
    }

}
