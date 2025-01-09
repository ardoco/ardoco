/* Licensed under MIT 2022-2025. */
package edu.kit.kastel.mcse.ardoco.tlr.tests.integration.tlrhelper;

import java.util.Comparator;

import edu.kit.kastel.mcse.ardoco.core.api.entity.Entity;
import edu.kit.kastel.mcse.ardoco.core.api.stage.connectiongenerator.SadModelTraceLink;
import edu.kit.kastel.mcse.ardoco.core.api.text.SentenceEntity;

/**
 * Represents a simple trace link by the id of the model and number of the sentence involved.
 */
public record ModelElementSentenceLink(String modelElementId, int sentenceNumber) implements Comparable<ModelElementSentenceLink> {

    public ModelElementSentenceLink(SadModelTraceLink traceLink) {
        this(traceLink.getEntityId(), traceLink.getSentenceNumber());
    }

    public ModelElementSentenceLink(SentenceEntity sentence, Entity entity) {
        this(entity.getId(), sentence.getSentence().getSentenceNumber());
    }

    @Override
    public int compareTo(ModelElementSentenceLink o) {
        return Comparator.comparing(ModelElementSentenceLink::modelElementId).thenComparing(ModelElementSentenceLink::sentenceNumber).compare(this, o);
    }

}
