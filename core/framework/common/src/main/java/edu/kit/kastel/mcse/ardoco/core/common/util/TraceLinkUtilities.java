/* Licensed under MIT 2023-2025. */
package edu.kit.kastel.mcse.ardoco.core.common.util;

import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.list.MutableList;

import edu.kit.kastel.mcse.ardoco.core.api.entity.ArchitectureEntity;
import edu.kit.kastel.mcse.ardoco.core.api.entity.ModelEntity;
import edu.kit.kastel.mcse.ardoco.core.api.text.SentenceEntity;
import edu.kit.kastel.mcse.ardoco.core.api.tracelink.TraceLink;
import edu.kit.kastel.mcse.ardoco.core.common.tuple.Pair;

public class TraceLinkUtilities {

    private static final String ENTRY_SEPARATOR = ",";

    private TraceLinkUtilities() {
        throw new IllegalStateException();
    }

    public static String createTraceLinkString(String firstElementId, String secondElementId) {
        return firstElementId + ENTRY_SEPARATOR + secondElementId;
    }

    public static ImmutableList<String> getSadSamTraceLinksAsStringList(ImmutableList<TraceLink<SentenceEntity, ModelEntity>> sadSamTraceLinks) {
        return sadSamTraceLinks.collect(tl -> createTraceLinkString(tl.getSecondEndpoint().getId(), String.valueOf(tl.getFirstEndpoint()
                .getSentence()
                .getSentenceNumber() + 1)));
    }

    public static ImmutableList<String> getSamCodeTraceLinksAsStringList(
            ImmutableList<TraceLink<? extends ArchitectureEntity, ? extends ModelEntity>> samCodeTraceLinks) {
        MutableList<String> resultsMut = Lists.mutable.empty();
        for (var traceLink : samCodeTraceLinks) {
            Pair<? extends ArchitectureEntity, ? extends ModelEntity> endpointTuple = traceLink.asPair();
            var modelElement = endpointTuple.first();
            var codeElement = endpointTuple.second();
            String traceLinkString = createTraceLinkString(modelElement.getId(), codeElement.toString());
            resultsMut.add(traceLinkString);
        }
        return resultsMut.toImmutable();
    }

    public static ImmutableList<String> getSadCodeTraceLinksAsStringList(ImmutableList<TraceLink<SentenceEntity, ? extends ModelEntity>> sadCodeTraceLinks) {
        MutableList<String> resultsMut = Lists.mutable.empty();
        for (var traceLink : sadCodeTraceLinks) {
            Pair<SentenceEntity, ? extends ModelEntity> endpointTuple = traceLink.asPair();
            var codeElement = endpointTuple.second();
            String sentenceNumber = String.valueOf(endpointTuple.first().getSentence().getSentenceNumber() + 1);
            String traceLinkString = TraceLinkUtilities.createTraceLinkString(sentenceNumber, codeElement.toString());
            resultsMut.add(traceLinkString);
        }
        return resultsMut.toImmutable();
    }
}
