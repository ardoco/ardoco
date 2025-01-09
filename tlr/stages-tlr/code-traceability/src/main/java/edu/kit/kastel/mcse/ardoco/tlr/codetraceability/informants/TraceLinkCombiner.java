/* Licensed under MIT 2023-2025. */
package edu.kit.kastel.mcse.ardoco.tlr.codetraceability.informants;

import java.util.SortedMap;

import org.eclipse.collections.api.factory.Sets;
import org.eclipse.collections.api.set.ImmutableSet;
import org.eclipse.collections.api.set.MutableSet;

import edu.kit.kastel.mcse.ardoco.core.api.entity.Entity;
import edu.kit.kastel.mcse.ardoco.core.api.models.ModelStates;
import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.code.CodeCompilationUnit;
import edu.kit.kastel.mcse.ardoco.core.api.stage.codetraceability.CodeTraceabilityState;
import edu.kit.kastel.mcse.ardoco.core.api.stage.connectiongenerator.ConnectionStates;
import edu.kit.kastel.mcse.ardoco.core.api.text.SentenceEntity;
import edu.kit.kastel.mcse.ardoco.core.api.tracelink.TraceLink;
import edu.kit.kastel.mcse.ardoco.core.api.tracelink.TransitiveTraceLink;
import edu.kit.kastel.mcse.ardoco.core.architecture.Deterministic;
import edu.kit.kastel.mcse.ardoco.core.common.util.DataRepositoryHelper;
import edu.kit.kastel.mcse.ardoco.core.data.DataRepository;
import edu.kit.kastel.mcse.ardoco.core.pipeline.agent.Informant;

@Deterministic
public class TraceLinkCombiner extends Informant {

    public TraceLinkCombiner(DataRepository dataRepository) {
        super(TraceLinkCombiner.class.getSimpleName(), dataRepository);
    }

    @Override
    public void process() {
        MutableSet<TraceLink<SentenceEntity, CodeCompilationUnit>> transitiveTraceLinks = Sets.mutable.empty();
        CodeTraceabilityState codeTraceabilityState = DataRepositoryHelper.getCodeTraceabilityState(this.getDataRepository());
        ModelStates modelStatesData = DataRepositoryHelper.getModelStatesData(this.getDataRepository());
        ConnectionStates connectionStates = DataRepositoryHelper.getConnectionStates(this.getDataRepository());

        if (codeTraceabilityState == null || modelStatesData == null || connectionStates == null) {
            return;
        }
        var samCodeTraceLinks = codeTraceabilityState.getSamCodeTraceLinks();
        for (var metamodel : modelStatesData.metamodels()) {
            var connectionState = connectionStates.getConnectionState(metamodel);
            var sadSamTraceLinks = connectionState.getTraceLinks();

            var combinedLinks = this.combineToTransitiveTraceLinks(sadSamTraceLinks, samCodeTraceLinks);
            transitiveTraceLinks.addAll(combinedLinks.toList());
        }

        codeTraceabilityState.addSadCodeTraceLinks(transitiveTraceLinks);
    }

    private ImmutableSet<TraceLink<SentenceEntity, CodeCompilationUnit>> combineToTransitiveTraceLinks(
            ImmutableSet<? extends TraceLink<SentenceEntity, ? extends Entity>> sadSamTraceLinks,
            ImmutableSet<? extends TraceLink<? extends Entity, CodeCompilationUnit>> samCodeTraceLinks) {

        MutableSet<TraceLink<SentenceEntity, CodeCompilationUnit>> transitiveTraceLinks = Sets.mutable.empty();

        for (TraceLink<SentenceEntity, ? extends Entity> sadSamTraceLink : sadSamTraceLinks) {
            String modelElementUid = sadSamTraceLink.getSecondEndpoint().getId();
            for (TraceLink<? extends Entity, CodeCompilationUnit> samCodeTraceLink : samCodeTraceLinks) {
                String samCodeTraceLinkModelElementId = samCodeTraceLink.getFirstEndpoint().getId();
                if (modelElementUid.equals(samCodeTraceLinkModelElementId)) {
                    var transitiveTraceLinkOptional = TransitiveTraceLink.createTransitiveTraceLink(sadSamTraceLink, samCodeTraceLink);
                    transitiveTraceLinkOptional.ifPresent(transitiveTraceLinks::add);
                }
            }
        }
        return transitiveTraceLinks.toImmutable();
    }

    @Override
    protected void delegateApplyConfigurationToInternalObjects(SortedMap<String, String> additionalConfiguration) {
        // empty
    }
}
