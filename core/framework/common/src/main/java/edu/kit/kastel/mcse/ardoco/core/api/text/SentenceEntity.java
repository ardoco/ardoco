/* Licensed under MIT 2024. */
package edu.kit.kastel.mcse.ardoco.core.api.text;

import java.util.Objects;

import edu.kit.kastel.mcse.ardoco.core.api.entity.TextEntity;

public final class SentenceEntity extends TextEntity {

    private static final long serialVersionUID = 7370112901785822767L;

    private final Sentence sentence;

    public SentenceEntity(Sentence sentence) {
        super(sentence.getText(), String.valueOf(sentence.getSentenceNumber()));
        this.sentence = sentence;
    }

    public Sentence getSentence() {
        return this.sentence;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        SentenceEntity that = (SentenceEntity) o;
        return Objects.equals(this.sentence, that.sentence);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.sentence);
    }
}
