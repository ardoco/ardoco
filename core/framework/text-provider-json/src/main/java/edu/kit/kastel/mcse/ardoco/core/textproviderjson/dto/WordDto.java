/* Licensed under MIT 2023. */
package edu.kit.kastel.mcse.ardoco.core.textproviderjson.dto;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonProperty;

import edu.kit.kastel.mcse.ardoco.core.api.text.POSTag;

/**
 * Definition of a word
 */
public class WordDto {
    private long id;
    private List<IncomingDependencyDto> incomingDependencies = new ArrayList<>();
    private String lemma;
    private List<OutgoingDependencyDto> outgoingDependencies = new ArrayList<>();
    private long sentenceNo;
    private String text;
    private POSTag posTag;

    /**
     * The id of the word. Should be ascending from 1 for the first word in the text.
     */
    @JsonProperty("id")
    public long getId() {
        return id;
    }

    @JsonProperty("id")
    public void setId(long value) {
        this.id = value;
    }

    /**
     * the incoming dependencies
     */
    @JsonProperty("incomingDependencies")
    public List<IncomingDependencyDto> getIncomingDependencies() {
        return incomingDependencies;
    }

    @JsonProperty("incomingDependencies")
    public void setIncomingDependencies(List<IncomingDependencyDto> value) {
        this.incomingDependencies = value;
    }

    /**
     * the lemma of the word
     */
    @JsonProperty("lemma")
    public String getLemma() {
        return lemma;
    }

    @JsonProperty("lemma")
    public void setLemma(String value) {
        this.lemma = value;
    }

    /**
     * the outgoing dependencies
     */
    @JsonProperty("outgoingDependencies")
    public List<OutgoingDependencyDto> getOutgoingDependencies() {
        return outgoingDependencies;
    }

    @JsonProperty("outgoingDependencies")
    public void setOutgoingDependencies(List<OutgoingDependencyDto> value) {
        this.outgoingDependencies = value;
    }

    @JsonProperty("posTag")
    public POSTag getPosTag() {
        return posTag;
    }

    @JsonProperty("posTag")
    public void setPosTag(POSTag value) {
        this.posTag = value;
    }

    /**
     * index of the sentence the word is contained in
     */
    @JsonProperty("sentenceNo")
    public long getSentenceNo() {
        return sentenceNo;
    }

    @JsonProperty("sentenceNo")
    public void setSentenceNo(long value) {
        this.sentenceNo = value;
    }

    /**
     * the text of the word
     */
    @JsonProperty("text")
    public String getText() {
        return text;
    }

    @JsonProperty("text")
    public void setText(String value) {
        this.text = value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof WordDto word))
            return false;
        return id == word.id && sentenceNo == word.sentenceNo && Objects.equals(incomingDependencies, word.incomingDependencies) && Objects.equals(lemma,
                word.lemma) && Objects.equals(outgoingDependencies, word.outgoingDependencies) && posTag == word.posTag && Objects.equals(text, word.text);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, incomingDependencies, lemma, outgoingDependencies, posTag, sentenceNo, text);
    }
}
