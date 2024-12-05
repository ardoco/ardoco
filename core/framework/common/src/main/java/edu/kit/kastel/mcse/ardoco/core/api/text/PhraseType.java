/* Licensed under MIT 2022-2023. */
package edu.kit.kastel.mcse.ardoco.core.api.text;

public enum PhraseType {
    /**
     * Adjective Phrase
     */
    ADJP,
    /**
     * Adverb Phrase
     */
    ADVP,
    /**
     * Conjunction Phrase
     */
    CONJP,
    /**
     * Fragment
     */
    FRAG,
    /**
     * Interjection. Corresponds approximately to the part-of-speech tag UH
     */
    INTJ,
    /**
     * List marker. Includes surrounding punctuation
     */
    LST,
    /**
     * Not a Constituent; used to show the scope of certain prenominal modifiers within an NP
     */
    NAC,
    /**
     * Noun Phrase
     */
    NP,
    /**
     * Used within certain complex NPs to mark the head of the NP. Corresponds very roughly to N-bar level but used
     * quite differently
     */
    NX,
    /**
     * Prepositional Phrase
     */
    PP,
    /**
     * Parenthetical
     */
    PRN,
    /**
     * Particle. Category for words that should be tagged RP
     */
    PRT,
    /**
     * Quantifier Phrase (i.e. complex measure/amount phrase); used within NP
     */
    QP,
    /**
     * Reduced Relative Clause
     */
    RRC,
    /**
     * Sentence
     */
    S,
    /**
     * Subordinate Clause
     */
    SBAR,
    /**
     * Direct Questions introduced by wh-element
     */
    SBARQ,
    /**
     * Declatative sentence with subject-aux inversion
     */
    SINV,
    /**
     * Yes/no questions and subconstituent of SBARQ excluding wh-element
     */
    SQ,
    /**
     * Trace of wh-Constituent
     */
    T,
    /**
     * Unlike Coordinated Phrase
     */
    UCP,
    /**
     * Verb Phrase
     */
    VP,
    /**
     * Wh-adjective Phrase. Adjectival phrase containing a wh-adverb, as in how hot.
     */
    WHADJP,
    /**
     * Wh-adverb Phrase. Introduces a clause with an NP gap. May be null (containing the 0 complementizer) or lexical,
     * containing a wh-adverb such as how or why.
     */
    WHAVP, WHADVP,
    /**
     * Wh-noun Phrase. Introduces a clause with an NP gap. May be null (containing the 0 complementizer) or lexical,
     * containing some wh-word, e.g. who, which book, whose daughter, none of which, or how many leopards.
     */
    WHNP,
    /**
     * Wh-prepositional Phrase. Prepositional phrase containing a wh-noun phrase (such as of which or by whose
     * authority) that either introduces a PP gap or is contained by a WHNP.
     */
    WHPP,
    /**
     * Unknown, uncertain, or unbracketable. X is often used for bracketing typos and in bracketing
     * the...the-constructions.
     */
    X,
    /**
     * Root. Overarching the whole sentence.
     */
    ROOT;

    public static PhraseType get(String type) {
        for (var phraseType : PhraseType.values()) {
            if (phraseType.toString().equalsIgnoreCase(type)) {
                return phraseType;
            }
        }
        return X;
    }
}
