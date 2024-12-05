/* Licensed under MIT 2023. */
package edu.kit.kastel.mcse.ardoco.core.textproviderjson.dto;

import java.util.List;
import java.util.Objects;

import com.fasterxml.jackson.annotation.*;

/**
 * A definition of a text
 */
public class TextDto {
    private List<SentenceDto> sentences;

    /**
     * the words that are contained in this sentence
     */
    @JsonProperty("sentences")
    public List<SentenceDto> getSentences() {
        return sentences;
    }

    @JsonProperty("sentences")
    public void setSentences(List<SentenceDto> value) {
        this.sentences = value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof TextDto textDTO))
            return false;
        return Objects.equals(sentences, textDTO.sentences);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sentences);
    }
}
