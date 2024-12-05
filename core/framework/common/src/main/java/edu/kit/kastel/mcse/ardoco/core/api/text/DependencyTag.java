/* Licensed under MIT 2021-2024. */
package edu.kit.kastel.mcse.ardoco.core.api.text;

/**
 * All possible dependency tags in the framework.
 */
public enum DependencyTag {

    /**
     * An appositional modifier of an NP is an NP immediately to the right of the first NP that serves to define or
     * modify that NP. It includes parenthesized examples, as well as defining abbreviations in one of these structures.
     */
    APPOS,

    /**
     * A nominal subject is a noun phrase which is the syntactic subject of a clause. The governor of this relation
     * might not always be a verb: when the verb is a copular verb, the root of the clause is the complement of the
     * copular verb, which can be an adjective or noun.
     */
    NSUBJ,

    /**
     * The possession modifier relation holds between the head of an NP and its possessive determiner, or agenitive's
     * complement.
     */
    POSS,

    /**
     * The direct object of a VP is the noun phrase which is the (accusative) object of the verb.
     */
    OBJ,
    /**
     * The indirect object of a VP is the noun phrase which is the (dative) object of the verb.
     */
    IOBJ,

    /**
     * The nmod relation is used for nominal modifiers of nouns or clausal predicates. nmod is a noun functioning as a
     * non-core (oblique) argument or adjunct. In English, nmod is used - for prepositional complements - for ‘s
     * genitives
     */
    NMOD,

    /**
     * A passive nominal subject is a noun phrase which is the syntactic subject of a passive clause.
     */
    NSUBJPASS,

    /**
     * The object of a preposition is the head of a noun phrase following the preposition, or the adverbs "here" and
     * "there".
     */
    POBJ,

    /**
     * An agent is the complement of a passive verb which is introduced by the preposition "by" and does the action.
     * This relation only appears in the collapsed dependencies, where it can replace prep_by, where appropriate. It
     * does not appear in basic dependencies output.
     */
    AGENT,

    /**
     * A numeric modifier of a noun is any number phrase that serves to modify the meaning of the noun with a quantity.
     */
    NUM,

    /**
     * A predeterminer is the relation between the head of an NP and a word that precedes and modifies the meaning of
     * the NP determiner.
     */
    PREDET,

    /**
     * A relative clause modifier of an NP is a relative clause modifying the NP. The relation points from the head noun
     * of the NP to the head of the relative clause, normally a verb.
     */
    RCMOD,

    /*
     * ================== Newly added types ==================
     */

    /**
     * A clausal subject is a clausal syntactic subject of a clause, i.e., the subject is itself a clause. The governor
     * of this relation might not always be a verb: when the verb is a copular verb, the root of the clause is the
     * complement of the copular verb. In the two following examples, "what she said" is the subject.
     */
    CSUBJ,
    /**
     * A clausal complement of a verb or adjective is a dependent clause with an internal subject which functions like
     * an object of the verb, or adjective. Clausal complements for nouns are limited to complement clauses with a
     * subset of nouns like "fact" or "report". We analyze them the same (parallel to the analysis of this class as
     * "content clauses" in Huddleston and Pullum 2002). Such clausal complements are usually finite (though there are
     * occasional remnant English subjunctives).
     */
    CCOMP,
    /**
     * An open clausal complement (xcomp) of a verb or an adjective is a predicative or clausal complement without its
     * own subject. The reference of the subject is necessarily determined by an argument external to the xcomp
     * (normally by the object of the next higher clause, if there is one, or else by the subject of the next higher
     * clause. These complements are always non-finite, and they are complements (arguments of the higher verb or
     * adjective) rather than adjuncts/modifiers, such as a purpose clause. The name xcompis borrowed from
     * Lexical-Functional Grammar.
     */
    XCOMP,

    /**
     * "obl", "oblique modifier"
     */
    OBL,

    /**
     * "vocative", "vocative"
     */
    VOCATIVE,
    /**
     * This relation captures an existential "there". The main verb of the clause is the governor.
     */
    EXPL,

    /**
     * "dislocated", "dislocated"
     */
    DISLOCATED,

    /**
     * "advcl", "adverbial clause modifier"
     */
    ADVCL,

    /**
     * "advmod", "adverbial modifier"
     */
    ADVMOD,
    /**
     * This is used for interjections and other discourse particles and elements (which are not clearly linked to the
     * structure of the sentence, except in an expressive way). We generally follow the guidelines of what the Penn
     * Treebanks count as an INTJ. They define this to include: interjections (oh,uh-huh,Welcome), fillers (um,ah), and
     * discourse markers (well,like,actually, but not you know).
     */
    DISCOURSE,
    /**
     * An auxiliary of a clause is a non-main verb of the clause, e.g., a modal auxiliary, or a form of "be", "do" or
     * "have" in a periphrastic tense.
     */
    AUXILIARY,
    /**
     * A copula is the relation between the complement of a copular verb and the copular verb. (We normally take a
     * copula as a dependent of its complement; see the discussion in section 4.)
     */
    COP,
    /**
     * A marker is the word introducing a finite clause subordinate to another clause. For a complement clause, this
     * will typically be "that" or "whether". For an adverbial clause, the marker is typically a preposition like
     * "while" or "although". The mark is a dependent of the subordinate clause head.
     */
    MARK,

    /**
     * "acl", "clausal modifier of a noun (adjectival clause)"
     */
    ACL,

    /**
     * "amod", "adjectival modifier"
     */
    AMOD,
    /**
     * A determiner is the relation between the head of an NP and its determiner.
     */
    DET,

    /**
     * "clf", "classifier"
     */
    CLF,

    /**
     * "case", "case marker"
     */
    CASE,

    /**
     * A conjunct is the relation between two elements connected by a coordinating conjunction, such as "and","or", etc.
     * We treat conjunctions asymmetrically: The head of the relation is the first conjunct and other conjunctions
     * depend on it via the conj relation.
     */
    CONJ,

    /**
     * A coordination is the relation between an element of a conjunct and the coordinating conjunction word of the
     * conjunct. (Note: different dependency grammars have different treatments of coordination. We take one conjunct of
     * a conjunction (normally the first) as the head of the conjunction.) A conjunction may also appear at the
     * beginning of a sentence. This is also called a cc, and dependent on the root predicate of the sentence.
     */
    CC,

    /**
     * "fixed", "fixed multiword expression"
     */
    FIXED,

    /**
     * "flat", "flat multiword expression"
     */
    FLAT,

    COMPOUND,

    LIST,

    PARATAXIS,

    ORPHAN,

    /**
     * This relation links two parts of a word that are separated in text that is not well edited. We follow the
     * treebank: The GW part is the dependent and the head is in some sense the "main" part, often the second part.
     */
    GOES_WITH,

    REPARANDUM,

    /**
     * This is used for any piece of punctuation in a clause, if punctuation is being retained in the typed
     * dependencies. By default, punctuation is not retained in the output.
     */
    PUNCT,

    /**
     * A clausal passive subject is a clausal syntactic subject of a passive clause. In the example below, "that she
     * lied" is the subject.
     */
    CSUBJ_PASS,

    /**
     * "acl:relcl", "relative clause"
     */
    ACL_RELCL,

    /**
     * "compound:prt", "phrasal verb particle"
     */
    COMPOUND_PRT,

    /**
     * "nmod:poss", "possessor"
     */
    NMOD_POSS,

    /**
     * "ref", "pronominal referent"
     */
    REF,

    /**
     * "nsubj:xsubj", "controlling nominal subject"
     */
    NSUBJ_XSUBJ,

    /**
     * "nsubj:pass:xsubj", "controlling nominal passive subject"
     */
    NSUBJ_PASS_XSUBJ,

    /**
     * "nsubj:relsubj", "relative nominal subject"
     */
    NSUBJ_RELSUBJ,

    /**
     * "nsubj:pass:relsubj", "relative nominal passive subject"
     */
    NSUBJ_PASS_RELSUBJ,

    /**
     * "obl:relobj", "relative object"
     */
    OBJ_RELOBJ
}
