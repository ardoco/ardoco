/* Licensed under MIT 2023-2024. */
package edu.kit.kastel.mcse.ardoco.tlr.codetraceability;

import java.util.Collection;
import java.util.LinkedHashSet;

import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.MutableList;
import org.eclipse.collections.api.set.ImmutableSet;
import org.eclipse.collections.impl.factory.Sets;

import edu.kit.kastel.mcse.ardoco.core.api.entity.ArchitectureEntity;
import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.code.CodeCompilationUnit;
import edu.kit.kastel.mcse.ardoco.core.api.stage.codetraceability.CodeTraceabilityState;
import edu.kit.kastel.mcse.ardoco.core.api.text.SentenceEntity;
import edu.kit.kastel.mcse.ardoco.core.api.tracelink.TraceLink;
import edu.kit.kastel.mcse.ardoco.core.architecture.Deterministic;
import edu.kit.kastel.mcse.ardoco.core.data.AbstractState;

@Deterministic
public class CodeTraceabilityStateImpl extends AbstractState implements CodeTraceabilityState {

    private MutableList<TraceLink<ArchitectureEntity, CodeCompilationUnit>> samCodeTraceLinks = Lists.mutable.empty();
    private MutableList<TraceLink<SentenceEntity, CodeCompilationUnit>> transitiveTraceLinks = Lists.mutable.empty();

    public CodeTraceabilityStateImpl() {
        super();
    }

    @Override
    public boolean addSamCodeTraceLink(TraceLink<ArchitectureEntity, CodeCompilationUnit> traceLink) {
        return this.samCodeTraceLinks.add(traceLink);
    }

    @Override
    public boolean addSamCodeTraceLinks(Collection<? extends TraceLink<ArchitectureEntity, CodeCompilationUnit>> traceLinks) {
        return this.samCodeTraceLinks.addAll(traceLinks);
    }

    @Override
    public ImmutableSet<TraceLink<ArchitectureEntity, CodeCompilationUnit>> getSamCodeTraceLinks() {
        return Sets.immutable.withAll(new LinkedHashSet<>(this.samCodeTraceLinks));
    }

    @Override
    public boolean addSadCodeTraceLink(TraceLink<SentenceEntity, CodeCompilationUnit> traceLink) {
        return this.transitiveTraceLinks.add(traceLink);
    }

    @Override
    public boolean addSadCodeTraceLinks(Collection<? extends TraceLink<SentenceEntity, CodeCompilationUnit>> traceLinks) {
        return this.transitiveTraceLinks.addAll(traceLinks);
    }

    @Override
    public ImmutableSet<TraceLink<SentenceEntity, CodeCompilationUnit>> getSadCodeTraceLinks() {
        return this.transitiveTraceLinks.toImmutableSet();
    }

}
