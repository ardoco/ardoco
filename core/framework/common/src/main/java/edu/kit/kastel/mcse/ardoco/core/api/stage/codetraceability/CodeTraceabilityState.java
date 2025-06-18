/* Licensed under MIT 2023-2024. */
package edu.kit.kastel.mcse.ardoco.core.api.stage.codetraceability;

import java.util.Collection;

import org.eclipse.collections.api.set.ImmutableSet;

import edu.kit.kastel.mcse.ardoco.core.api.entity.ArchitectureEntity;
import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.code.CodeItem;
import edu.kit.kastel.mcse.ardoco.core.api.text.SentenceEntity;
import edu.kit.kastel.mcse.ardoco.core.api.tracelink.SadCodeTraceLink;
import edu.kit.kastel.mcse.ardoco.core.api.tracelink.SamCodeTraceLink;
import edu.kit.kastel.mcse.ardoco.core.api.tracelink.TraceLink;
import edu.kit.kastel.mcse.ardoco.core.api.tracelink.TransitiveTraceLink;
import edu.kit.kastel.mcse.ardoco.core.architecture.Deterministic;
import edu.kit.kastel.mcse.ardoco.core.data.PipelineStepData;

@Deterministic
public interface CodeTraceabilityState extends PipelineStepData {
    String ID = "CodeTraceabilityState";

    /**
     * Add a {@link SamCodeTraceLink} to this state.
     *
     * @param traceLink the trace link to add
     * @return whether the operation was successful
     */
    boolean addSamCodeTraceLink(TraceLink<? extends ArchitectureEntity, ? extends CodeItem> traceLink);

    /**
     * Add a collection of {@link SamCodeTraceLink SamCodeTraceLinks} to this state.
     *
     * @param traceLinks the trace links to add
     * @return whether the operation was successful
     */
    boolean addSamCodeTraceLinks(Collection<? extends TraceLink<? extends ArchitectureEntity, ? extends CodeItem>> traceLinks);

    /**
     * Return a set of stored {@link SamCodeTraceLink SamCodeTraceLinks}.
     *
     * @return set of stored {@link SamCodeTraceLink SamCodeTraceLinks}
     */
    ImmutableSet<TraceLink<? extends ArchitectureEntity, ? extends CodeItem>> getSamCodeTraceLinks();

    /**
     * Add a {@link TransitiveTraceLink} to this state.
     *
     * @param traceLink the trace link to add
     * @return whether the operation was successful
     */
    boolean addSadCodeTraceLink(TraceLink<SentenceEntity, ? extends CodeItem> traceLink);

    /**
     * Add a collection of {@link SadCodeTraceLink SadCodeTraceLinks} to this state.
     *
     * @param traceLinks the trace links to add
     * @return whether the operation was successful
     */
    boolean addSadCodeTraceLinks(Collection<? extends TraceLink<SentenceEntity, ? extends CodeItem>> traceLinks);

    /**
     * Return a set of stored {@link TransitiveTraceLink TransitiveTraceLinks}.
     *
     * @return set of stored {@link TransitiveTraceLink TransitiveTraceLinks}
     */
    ImmutableSet<TraceLink<SentenceEntity, ? extends CodeItem>> getSadCodeTraceLinks();
}
