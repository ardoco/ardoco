/* Licensed under MIT 2023-2025. */
package edu.kit.kastel.mcse.ardoco.core.api.stage.codetraceability;

import java.util.Collection;

import org.eclipse.collections.api.set.ImmutableSet;

import edu.kit.kastel.mcse.ardoco.core.api.entity.ArchitectureEntity;
import edu.kit.kastel.mcse.ardoco.core.api.entity.ModelEntity;
import edu.kit.kastel.mcse.ardoco.core.api.text.SentenceEntity;
import edu.kit.kastel.mcse.ardoco.core.api.tracelink.TraceLink;
import edu.kit.kastel.mcse.ardoco.core.api.tracelink.TransitiveTraceLink;
import edu.kit.kastel.mcse.ardoco.core.architecture.Deterministic;
import edu.kit.kastel.mcse.ardoco.core.data.PipelineStepData;

@Deterministic
public interface CodeTraceabilityState extends PipelineStepData {
    String ID = "CodeTraceabilityState";

    /**
     * Add a collection of architecture to model links to this state.
     *
     * @param traceLinks the trace links to add
     * @return whether the operation was successful
     */
    boolean addSamCodeTraceLinks(Collection<? extends TraceLink<? extends ArchitectureEntity, ? extends ModelEntity>> traceLinks);

    /**
     * Return a set of stored architecture to model links.
     *
     * @return set of stored architecture to model links
     */
    ImmutableSet<TraceLink<? extends ArchitectureEntity, ? extends ModelEntity>> getSamCodeTraceLinks();

    /**
     * Add a collection of {@link TraceLink SadCodeTraceLinks} to this state.
     *
     * @param traceLinks the trace links to add
     * @return whether the operation was successful
     */
    boolean addSadCodeTraceLinks(Collection<? extends TraceLink<SentenceEntity, ? extends ModelEntity>> traceLinks);

    /**
     * Return a set of stored {@link TransitiveTraceLink TransitiveTraceLinks}.
     *
     * @return set of stored {@link TransitiveTraceLink TransitiveTraceLinks}
     */
    ImmutableSet<TraceLink<SentenceEntity, ? extends ModelEntity>> getSadCodeTraceLinks();
}
