/* Licensed under MIT 2025. */
package edu.kit.kastel.mcse.ardoco.core.api.stage.connectiongenerator;

import java.util.Objects;

import edu.kit.kastel.mcse.ardoco.core.api.entity.ModelEntity;
import edu.kit.kastel.mcse.ardoco.core.api.text.Sentence;
import edu.kit.kastel.mcse.ardoco.core.api.text.SentenceEntity;
import edu.kit.kastel.mcse.ardoco.core.api.tracelink.TraceLink;

public class SadModelTraceLink extends TraceLink<SentenceEntity, ModelEntity> {

    private final Sentence sentence;
    private final ModelEntity modelEntity;

    /**
     * Create a trace link based on a {@link Sentence} and a concrete {@link ModelEntity} .
     */
    public SadModelTraceLink(Sentence sentence, ModelEntity modelEntity) {
        super(new SentenceEntity(sentence), modelEntity);
        this.sentence = sentence;
        this.modelEntity = modelEntity;
    }

    /**
     * Create a trace link based on a {@link SentenceEntity} and a concrete {@link ModelEntity} .
     */
    public SadModelTraceLink(SentenceEntity sentenceEntity, ModelEntity modelEntity) {
        super(sentenceEntity, modelEntity);
        this.sentence = sentenceEntity.getSentence();
        this.modelEntity = modelEntity;
    }

    /**
     * Get the sentence number of the word that the trace link is based on.
     *
     * @return sentence number of the word that the trace link is based on.
     */
    public int getSentenceNumber() {
        return this.sentence.getSentenceNumber();
    }

    /**
     * Returns the sentence of the word that the trace link is based on.
     *
     * @return the sentence of the word that the trace link is based on.
     */
    public Sentence getSentence() {
        return this.sentence;
    }

    /**
     * Get the UID of the model element that the trace link is based on.
     *
     * @return Uid of the model element that the trace link is based on.
     */
    public String getEntityId() {
        return this.modelEntity.getId();
    }

    /**
     * See {@link Object#equals(Object)}. Uses the Uid of the model element and the sentence number of the word
     */
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof SadModelTraceLink other) {
            var otherId = other.getEntityId();
            var otherSentenceNo = other.getSentenceNumber();
            return this.getEntityId().equals(otherId) && this.getSentenceNumber() == otherSentenceNo;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.getEntityId(), this.getSentenceNumber());
    }

}
