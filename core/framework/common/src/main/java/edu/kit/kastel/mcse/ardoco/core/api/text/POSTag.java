/* Licensed under MIT 2021-2023. */
package edu.kit.kastel.mcse.ardoco.core.api.text;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * This class represents all valid part-of-speech (pos) tags
 */
public enum POSTag {
    //@formatter:off
	ADJECTIVE("JJ"), ADJECTIVE_COMPARATIVE(ADJECTIVE + "R"), ADJECTIVE_SUPERLATIVE(ADJECTIVE + "S"), ADVERB("RB"), ADVERB_COMPARATIVE(ADVERB + "R"), ADVERB_SUPERLATIVE(ADVERB + "S"), ADVERB_WH(
			"W" + ADVERB), CONJUNCTION_COORDINATING("CC"), CONJUNCTION_SUBORDINATING("IN"), CARDINAL_NUMBER("CD"), DETERMINER("DT"), DETERMINER_WH("W" + DETERMINER), EXISTENTIAL_THERE(
			"EX"), FOREIGN_WORD("FW"), LIST_ITEM_MARKER("LS"), NOUN("NN"), NOUN_PLURAL(NOUN + "S"), NOUN_PROPER_SINGULAR(NOUN + "P"), NOUN_PROPER_PLURAL(NOUN + "PS"), PREDETERMINER(
			"PDT"), POSSESSIVE_ENDING("POS"), PRONOUN_PERSONAL("PRP"), PRONOUN_POSSESSIVE("PRP$"), PRONOUN_POSSESSIVE_WH("WP$"), PRONOUN_WH("WP"), PARTICLE("RP"), SYMBOL("SYM"), TO(
			"TO"), INTERJECTION("UH"), VERB("VB"), VERB_PAST_TENSE(VERB + "D"), VERB_PARTICIPLE_PRESENT(VERB + "G"), VERB_PARTICIPLE_PAST(VERB + "N"), VERB_SINGULAR_PRESENT_NONTHIRD_PERSON(
			VERB + "P"), VERB_SINGULAR_PRESENT_THIRD_PERSON(VERB + "Z"), VERB_MODAL("MD"), CLOSER("."), COMMA(","), COLON(":"), LEFT_PAREN("-LRB-"), RIGHT_PAREN("-RRB-"), NONE("-NONE-"), OPEN_QUOTE(
			"``"), CLOSE_QUOTE("''"), DOLLAR("$"), HASHTAG("#"), // added because these tags are now recognized by Stanford
	// they are originally from the Web Treebank corpus and/or the OntoNotes 4.0
	HYPH("HYPH"), NFP("NFP"), ADD("ADD"), AFX("AFX"), GW("GW"), XX("XX");
	//@formatter:on

    private static final Logger logger = LoggerFactory.getLogger(POSTag.class);

    private final String tag;

    POSTag(String tag) {
        this.tag = tag;
    }

    /**
     * Returns the encoding for this part-of-speech.
     *
     * @return A string representing a Penn Treebank encoding for an English part-of-speech.
     */
    @Override
    public String toString() {
        return getTag();
    }

    /**
     * Gets the tag information.
     *
     * @return the tag
     */
    public String getTag() {
        return tag;
    }

    /**
     * Gets the {@link POSTag} by value.
     *
     * @param value the value
     * @return the POS tag
     * @throws IllegalArgumentException iff value was not found
     */
    public static POSTag get(String value) {
        for (POSTag v : values()) {
            if (value.equals(v.getTag())) {
                return v;
            }
        }
        logger.error("Unknown part of speech: {}", value);
        throw new IllegalArgumentException("Unknown part of speech: " + value + ".");
    }

    /**
     * Checks if this is a verb.
     *
     * @return true, if this is a verb
     */
    public boolean isVerb() {
        return getTag().startsWith("VB");
    }

    public boolean isNoun() {
        return getTag().startsWith("NN");
    }

    @JsonValue
    public String toValue() {
        return getTag();
    }

    @JsonCreator
    public static POSTag forValue(String value) throws IOException {
        try {
            return get(value);
        } catch (IllegalArgumentException e) {
            throw new IOException("Cannot deserialize PosTag");
        }
    }

}
