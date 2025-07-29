package edu.kit.kastel.mcse.ardoco.tlr.connectiongenerator.ner;

import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.list.MutableList;

import edu.kit.kastel.mcse.ardoco.core.api.entity.ModelEntity;
import edu.kit.kastel.mcse.ardoco.core.api.stage.connectiongenerator.NamedArchitectureEntityToModelTraceLink;
import edu.kit.kastel.mcse.ardoco.core.api.stage.connectiongenerator.NerConnectionState;
import edu.kit.kastel.mcse.ardoco.core.api.stage.recommendationgenerator.NamedArchitectureEntity;
import edu.kit.kastel.mcse.ardoco.core.api.tracelink.TraceLink;
import edu.kit.kastel.mcse.ardoco.core.data.AbstractState;
import edu.kit.kastel.mcse.ardoco.core.pipeline.agent.Claimant;

public class NerConnectionStateImpl extends AbstractState implements NerConnectionState {

    private final MutableList<TraceLink<NamedArchitectureEntity, ModelEntity>> instanceLinks;

    /**
     * Creates a new connection state.
     */
    public NerConnectionStateImpl() {
        super();
        this.instanceLinks = Lists.mutable.empty();
    }

    @Override
    public ImmutableList<TraceLink<NamedArchitectureEntity, ModelEntity>> getTraceLinks() {
        return Lists.immutable.withAll(this.instanceLinks);
    }

    @Override
    public void addToLinks(NamedArchitectureEntity namedArchitectureEntity, ModelEntity modelEntity, Claimant claimant, double probability) {
        TraceLink<NamedArchitectureEntity, ModelEntity> traceLink = new NamedArchitectureEntityToModelTraceLink(namedArchitectureEntity, modelEntity, claimant,
                probability);
        if (!this.instanceLinks.contains(traceLink)) {
            this.instanceLinks.add(traceLink);
        }
    }

}
