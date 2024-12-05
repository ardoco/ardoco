/* Licensed under MIT 2022-2024. */
package edu.kit.kastel.mcse.ardoco.core.api.stage.textextraction;

import static edu.kit.kastel.mcse.ardoco.core.common.AggregationFunctions.AVERAGE;

import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.map.sorted.ImmutableSortedMap;
import org.eclipse.collections.api.set.sorted.ImmutableSortedSet;

import edu.kit.kastel.mcse.ardoco.core.api.text.Word;
import edu.kit.kastel.mcse.ardoco.core.common.AggregationFunctions;
import edu.kit.kastel.mcse.ardoco.core.data.Confidence;
import edu.kit.kastel.mcse.ardoco.core.pipeline.agent.Claimant;

/**
 * The Interface for strategies for the text state. Responsible for creating {@link NounMapping NounMappings} from their constituent parts in a variety of
 * situations.
 */
public interface TextStateStrategy {
    /**
     * Aggregation function used to aggregate multiple confidences into a single value
     */
    AggregationFunctions DEFAULT_AGGREGATOR = AVERAGE;

    void setState(TextState textState);

    /**
     * Tries to add a mapping to the state using the specified parameters. If a matching mapping already exists, the mapping is extended instead.
     *
     * @param word         the word
     * @param kind         the kind
     * @param claimant     the claimant of the mapping
     * @param probability  the probability
     * @param surfaceForms the surface forms
     * @return the resulting noun mapping, either new or merged
     */

    NounMapping addOrExtendNounMapping(Word word, MappingKind kind, Claimant claimant, double probability, ImmutableList<String> surfaceForms);

    /**
     * Adds a mapping to the state using the specified parameters. Does not consider whether a matching mapping already exists.
     *
     * @param words          the words
     * @param distribution   the distribution
     * @param referenceWords the reference words
     * @param surfaceForms   the surface forms
     * @param reference      the reference, nullable
     * @return the newly created noun mapping
     */

    NounMapping addNounMapping(ImmutableSortedSet<Word> words, ImmutableSortedMap<MappingKind, Confidence> distribution, ImmutableList<Word> referenceWords,
            ImmutableList<String> surfaceForms, String reference);

    /**
     * Adds a mapping to the state using the specified parameters. Does not consider whether a matching mapping already exists.
     *
     * @param words          the words
     * @param kind           the kind
     * @param claimant       the claimant
     * @param probability    the probability that the mapping is of this kind
     * @param referenceWords the reference words
     * @param surfaceForms   the surface forms
     * @param reference      the reference, nullable
     * @return the newly created noun mapping
     */

    NounMapping addNounMapping(ImmutableSortedSet<Word> words, MappingKind kind, Claimant claimant, double probability, ImmutableList<Word> referenceWords,
            ImmutableList<String> surfaceForms, String reference);

    /**
     * Merges two noun mappings into a new mapping of the given kind, probability and claimant without adding it to the state.
     *
     * @param firstNounMapping  the first mapping
     * @param secondNounMapping the second mapping
     * @param referenceWords    reference words to use, nullable
     * @param reference         reference to use, nullable
     * @param mappingKind       the mapping kind
     * @param claimant          the claimant
     * @param probability       the probability
     * @return the merged noun mapping
     */

    NounMapping mergeNounMappingsStateless(NounMapping firstNounMapping, NounMapping secondNounMapping, ImmutableList<Word> referenceWords, String reference,
            MappingKind mappingKind, Claimant claimant, double probability);

    /**
     * Merges two noun mappings into a new mapping of the given kind, probability and claimant and adds it to the state.
     *
     * @param firstNounMapping  the first mapping
     * @param secondNounMapping the second mapping
     * @param referenceWords    reference words to use, nullable
     * @param reference         reference to use, nullable
     * @param mappingKind       the mapping kind
     * @param claimant          the claimant
     * @param probability       the probability
     * @return the merged noun mapping
     */

    NounMapping mergeNounMappings(NounMapping firstNounMapping, NounMapping secondNounMapping, ImmutableList<Word> referenceWords, String reference,
            MappingKind mappingKind, Claimant claimant, double probability);

    /**
     * Calculates a joined reference for a set of reference words.
     *
     * @param referenceWords the reference words
     * @return a joined reference
     */
    default String calculateNounMappingReference(ImmutableList<Word> referenceWords) {
        StringBuilder refBuilder = new StringBuilder();
        referenceWords.toSortedListBy(Word::getPosition);
        referenceWords.toSortedListBy(Word::getSentenceNo);

        for (int i = 0; i < referenceWords.size() - 1; i++) {
            refBuilder.append(referenceWords.get(i).getText()).append(" ");
        }
        refBuilder.append(referenceWords.get(referenceWords.size() - 1).getText());
        return refBuilder.toString();
    }

    ImmutableList<NounMapping> getNounMappingsWithSimilarReference(String reference);
}
