/* Licensed under MIT 2023-2025. */
package edu.kit.kastel.mcse.ardoco.tlr.codetraceability.informants;

import java.io.Serial;

import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.factory.Sets;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.map.sorted.ImmutableSortedMap;
import org.eclipse.collections.api.set.ImmutableSet;
import org.eclipse.collections.api.set.MutableSet;

import edu.kit.kastel.mcse.ardoco.core.api.entity.Entity;
import edu.kit.kastel.mcse.ardoco.core.api.entity.ModelEntity;
import edu.kit.kastel.mcse.ardoco.core.api.models.Metamodel;
import edu.kit.kastel.mcse.ardoco.core.api.models.ModelStates;
import edu.kit.kastel.mcse.ardoco.core.api.stage.codetraceability.CodeTraceabilityState;
import edu.kit.kastel.mcse.ardoco.core.api.stage.connectiongenerator.ConnectionStates;
import edu.kit.kastel.mcse.ardoco.core.api.stage.connectiongenerator.SentenceModelTraceLink;
import edu.kit.kastel.mcse.ardoco.core.api.stage.connectiongenerator.ner.NerConnectionStates;
import edu.kit.kastel.mcse.ardoco.core.api.text.Phrase;
import edu.kit.kastel.mcse.ardoco.core.api.text.Sentence;
import edu.kit.kastel.mcse.ardoco.core.api.text.SentenceEntity;
import edu.kit.kastel.mcse.ardoco.core.api.text.Word;
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
        MutableSet<TraceLink<SentenceEntity, ? extends ModelEntity>> transitiveTraceLinks = Sets.mutable.empty();
        CodeTraceabilityState codeTraceabilityState = DataRepositoryHelper.getCodeTraceabilityState(this.getDataRepository());
        ModelStates modelStatesData = DataRepositoryHelper.getModelStatesData(this.getDataRepository());

        var swattrConnectionState = this.getDataRepository().getData(ConnectionStates.ID, ConnectionStates.class);
        var artemisConnectionState = this.getDataRepository().getData(NerConnectionStates.ID, NerConnectionStates.class);

        if (codeTraceabilityState == null || modelStatesData == null || (swattrConnectionState.isEmpty() && artemisConnectionState.isEmpty())) {
            return;
        }

        var samCodeTraceLinks = codeTraceabilityState.getSamCodeTraceLinks();

        if (swattrConnectionState.isPresent()) {

            var connectionState = swattrConnectionState.get().getConnectionState(Metamodel.ARCHITECTURE_WITH_COMPONENTS);
            //evtl CodeAsArchitecture, assumed that only one leads to transitive tracelinks
            var sadSamTraceLinks = connectionState.getTraceLinks();

            var combinedLinks = this.combineToTransitiveTraceLinks(sadSamTraceLinks, samCodeTraceLinks);
            transitiveTraceLinks.addAll(combinedLinks.toList());

            codeTraceabilityState.addSadCodeTraceLinks(transitiveTraceLinks);
        } else if (artemisConnectionState.isPresent()) {
            var connectionState = artemisConnectionState.get().getNerConnectionState(Metamodel.ARCHITECTURE_WITH_COMPONENTS);
            var nerSamTraceLinks = connectionState.getTraceLinks();

            ImmutableSet<? extends TraceLink<SentenceEntity, ? extends Entity>> sadSamTraceLinks = nerSamTraceLinks.collect(traceLink -> {
                var sentenceNumber = traceLink.getFirstEndpoint().getSentenceNumber() - 1; // Adjusting to 0-based index
                var sentence = new SentenceEntity(new Sentence() {
                    @Serial
                    private static final long serialVersionUID = 3287980300258916368L;

                    @Override
                    public int getSentenceNumber() {
                        return sentenceNumber;
                    }

                    @Override
                    public ImmutableList<Word> getWords() {
                        return Lists.immutable.empty();
                    }

                    @Override
                    public String getText() {
                        return String.valueOf(sentenceNumber);
                    }

                    @Override
                    public ImmutableList<Phrase> getPhrases() {
                        return Lists.immutable.empty();
                    }
                });
                return new SentenceModelTraceLink(sentence, traceLink.getSecondEndpoint());
            }).toImmutableSet();

            var combinedLinks = this.combineToTransitiveTraceLinks(sadSamTraceLinks, samCodeTraceLinks);
            transitiveTraceLinks.addAll(combinedLinks.toList());

            codeTraceabilityState.addSadCodeTraceLinks(transitiveTraceLinks);
        }
    }

    private ImmutableSet<TraceLink<SentenceEntity, ? extends ModelEntity>> combineToTransitiveTraceLinks(
            ImmutableSet<? extends TraceLink<SentenceEntity, ? extends Entity>> sadSamTraceLinks,
            ImmutableSet<? extends TraceLink<? extends Entity, ? extends ModelEntity>> samCodeTraceLinks) {

        MutableSet<TraceLink<SentenceEntity, ? extends ModelEntity>> transitiveTraceLinks = Sets.mutable.empty();

        for (TraceLink<SentenceEntity, ? extends Entity> sadSamTraceLink : sadSamTraceLinks) {
            String modelElementUid = sadSamTraceLink.getSecondEndpoint().getId();
            for (TraceLink<? extends Entity, ? extends ModelEntity> samCodeTraceLink : samCodeTraceLinks) {
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
    protected void delegateApplyConfigurationToInternalObjects(ImmutableSortedMap<String, String> additionalConfiguration) {
        // empty
    }
}
