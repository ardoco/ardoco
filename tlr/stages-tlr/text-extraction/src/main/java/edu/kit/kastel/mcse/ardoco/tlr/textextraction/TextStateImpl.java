/* Licensed under MIT 2021-2024. */
package edu.kit.kastel.mcse.ardoco.tlr.textextraction;

import java.util.Comparator;
import java.util.SortedMap;

import org.eclipse.collections.api.block.predicate.Predicate;
import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.factory.SortedSets;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.list.MutableList;
import org.eclipse.collections.api.ordered.SortedIterable;
import org.eclipse.collections.api.set.sorted.MutableSortedSet;

import edu.kit.kastel.mcse.ardoco.core.api.stage.textextraction.MappingKind;
import edu.kit.kastel.mcse.ardoco.core.api.stage.textextraction.NounMapping;
import edu.kit.kastel.mcse.ardoco.core.api.stage.textextraction.PhraseMapping;
import edu.kit.kastel.mcse.ardoco.core.api.stage.textextraction.TextState;
import edu.kit.kastel.mcse.ardoco.core.api.stage.textextraction.TextStateStrategy;
import edu.kit.kastel.mcse.ardoco.core.api.text.Phrase;
import edu.kit.kastel.mcse.ardoco.core.api.text.Word;
import edu.kit.kastel.mcse.ardoco.core.common.tuple.Pair;
import edu.kit.kastel.mcse.ardoco.core.common.util.Comparators;
import edu.kit.kastel.mcse.ardoco.core.data.AbstractState;
import edu.kit.kastel.mcse.ardoco.core.pipeline.agent.Claimant;

/**
 * The Class TextState defines the basic implementation of a {@link TextState}.
 */
public class TextStateImpl extends AbstractState implements TextState {

    private static final Comparator<NounMapping> ORDER_NOUNMAPPING = (n1, n2) -> {
        if (n1.equals(n2)) {
            return 0;
        }
        var nm1 = (NounMappingImpl) n1;
        var nm2 = (NounMappingImpl) n2;
        int compare = Long.compare(nm1.earliestCreationTime(), nm2.earliestCreationTime());
        if (compare != 0) {
            return compare;
        }
        throw new IllegalStateException("NounMappings are not equal but have same creation time");
    };

    /**
     * Minimum difference that need to shall not be reached to identify a NounMapping as NameOrType.
     *
     * @see #getMappingsThatCouldBeOfKind(Word, MappingKind)
     */
    private static final double MAPPING_KIND_MAX_DIFF = 0.1;
    private MutableList<NounMapping> nounMappings;
    private MutableList<PhraseMapping> phraseMappings;
    private final TextStateStrategy strategy;

    // Configuration Test
    @SuppressWarnings("unused")
    private TextStateImpl() {
        this.strategy = null;
    }

    public TextStateImpl(TextStateStrategy strategy) {
        this.strategy = strategy;
        this.nounMappings = Lists.mutable.empty();
        this.phraseMappings = Lists.mutable.empty();
        this.strategy.setState(this);
    }

    @Override
    public TextStateStrategy getTextStateStrategy() {
        return this.strategy;
    }

    @Override
    public ImmutableList<NounMapping> getNounMappings() {
        return this.nounMappings.toImmutableList();
    }

    @Override
    public ImmutableList<PhraseMapping> getPhraseMappings() {
        return this.phraseMappings.toImmutableList();
    }

    public ImmutableList<PhraseMapping> getPhraseMappingsByNounMapping(NounMapping nounMapping) {

        MutableList<PhraseMapping> result = Lists.mutable.empty();

        for (Phrase phrase : nounMapping.getPhrases()) {
            result.addAll(this.phraseMappings.select(pm -> pm.getPhrases().contains(phrase)));
        }

        return result.toImmutable();
    }

    @Override
    public PhraseMapping getPhraseMappingByNounMapping(NounMapping nounMapping) {
        ImmutableList<PhraseMapping> phraseMappingsByNounMapping = this.getPhraseMappingsByNounMapping(nounMapping);
        assert (!phraseMappingsByNounMapping.isEmpty()) : "Every noun mapping should be connected to a phrase mapping";
        return phraseMappingsByNounMapping.get(0);
    }

    @Override
    public ImmutableList<NounMapping> getNounMappingsByPhraseMapping(PhraseMapping phraseMapping) {
        return this.getNounMappings()
                .select(nm -> Comparators.collectionsEqualsAnyOrder(phraseMapping.getPhrases().castToCollection(), nm.getPhrases().castToCollection()));
    }

    /**
     * Returns all type mappings.
     *
     * @param kind searched mappingKind
     * @return all type mappings as list
     */
    @Override
    public ImmutableList<NounMapping> getNounMappingsOfKind(MappingKind kind) {
        return this.getNounMappings().select(this.nounMappingIsOfKind(kind)).toImmutable();
    }

    @Override
    public ImmutableList<NounMapping> getNounMappingsThatBelongToTheSamePhraseMapping(NounMapping nounMapping) {

        return this.getNounMappingsByPhraseMapping(this.getPhraseMappingByNounMapping(nounMapping)).select(nm -> !nm.equals(nounMapping));
    }

    @Override
    public void mergeNounMappings(NounMapping nounMapping, NounMapping otherNounMapping, Claimant claimant, ImmutableList<Word> referenceWords) {
        this.strategy.mergeNounMappings(nounMapping, otherNounMapping, referenceWords, null, nounMapping.getKind(), claimant, nounMapping.getProbabilityForKind(
                nounMapping.getKind()));
    }

    @Override
    public NounMapping setReferenceOfNounMapping(NounMapping nounMapping, ImmutableList<Word> referenceWords, String reference) {

        return this.addNounMapping(nounMapping.getWords().toImmutableSortedSet(), nounMapping.getDistribution(), referenceWords, nounMapping.getSurfaceForms(),
                reference);

    }

    @Override
    public ImmutableList<NounMapping> getMappingsThatCouldBeOfKind(Word word, MappingKind kind) {
        return this.getNounMappingsByWord(word).select(mapping -> mapping.getProbabilityForKind(kind) > 0);
    }

    @Override
    public ImmutableList<NounMapping> getMappingsThatCouldBeMultipleKinds(Word word, MappingKind... kinds) {
        if (kinds.length == 0) {
            throw new IllegalArgumentException("You need to provide some mapping kinds!");
        }

        if (kinds.length < 2) {
            return this.getNounMappingsOfKind(kinds[0]);
        }

        MutableList<NounMapping> result = Lists.mutable.empty();
        ImmutableList<NounMapping> mappings = this.getNounMappingsByWord(word);

        for (NounMapping mapping : mappings) {
            ImmutableList<Double> probabilities = Lists.immutable.with(kinds).collect(mapping::getProbabilityForKind);
            if (probabilities.anySatisfy(p -> p <= 0)) {
                continue;
            }

            boolean similar = probabilities.allSatisfy(p1 -> probabilities.allSatisfy(p2 -> Math.abs(p1 - p2) < MAPPING_KIND_MAX_DIFF));
            if (similar) {
                result.add(mapping);
            }

        }

        return result.toImmutable();
    }

    @Override
    public ImmutableList<NounMapping> getNounMappingsByWord(Word word) {
        return this.getNounMappings().select(nm -> nm.getWords().contains(word));
    }

    @Override
    public ImmutableList<NounMapping> getNounMappingsByWordAndKind(Word word, MappingKind kind) {
        return this.getNounMappings().select(n -> n.getWords().contains(word)).select(this.nounMappingIsOfKind(kind)).toImmutable();
    }

    @Override
    public boolean isWordContainedByMappingKind(Word word, MappingKind kind) {
        return this.getNounMappings().select(n -> n.getWords().contains(word)).anySatisfy(this.nounMappingIsOfKind(kind));
    }

    @Override
    public ImmutableList<NounMapping> getNounMappingsWithSimilarReference(String reference) {
        return this.strategy.getNounMappingsWithSimilarReference(reference);
    }

    @Override
    public NounMapping mergeNounMappings(NounMapping nounMapping, NounMapping textuallyEqualNounMapping, Claimant claimant) {
        return this.strategy.mergeNounMappings(nounMapping, textuallyEqualNounMapping, null, null, nounMapping.getKind(), claimant, nounMapping
                .getProbabilityForKind(nounMapping.getKind()));

    }

    @Override
    public void mergePhraseMappingsAndNounMappings(PhraseMapping phraseMapping, PhraseMapping similarPhraseMapping,
            MutableList<Pair<NounMapping, NounMapping>> similarNounMappings, Claimant claimant) {
        this.mergePhraseMappings(phraseMapping, similarPhraseMapping);
        for (Pair<NounMapping, NounMapping> nounMappingPair : similarNounMappings) {
            this.mergeNounMappings(nounMappingPair.first(), nounMappingPair.second(), claimant);
        }
    }

    @Override
    public PhraseMapping mergePhraseMappings(PhraseMapping phraseMapping, PhraseMapping similarPhraseMapping) {

        MutableSortedSet<Phrase> mergedPhrases = phraseMapping.getPhrases().toSortedSet();
        mergedPhrases.addAll(similarPhraseMapping.getPhrases().toList());

        PhraseMapping mergedPhraseMapping = new PhraseMappingImpl(mergedPhrases.toImmutable());

        this.phraseMappings.add(mergedPhraseMapping);

        this.removePhraseMappingFromState(phraseMapping, mergedPhraseMapping);
        this.removePhraseMappingFromState(similarPhraseMapping, mergedPhraseMapping);
        return mergedPhraseMapping;
    }

    @Override
    public NounMapping getNounMappingByWord(Word word) {
        var result = this.getNounMappings().select(nMapping -> nMapping.getWords().contains(word)).toImmutable();

        assert (result.size() <= 1) : "A word should only contained by one noun mapping";
        if (result.isEmpty()) {
            return null;
        }
        return result.get(0);
    }

    /**
     * Returns a list of all references of kind mappings.
     *
     * @return all references of type mappings as list.
     */
    @Override
    public ImmutableList<String> getListOfReferences(MappingKind kind) {
        MutableSortedSet<String> referencesOfKind = SortedSets.mutable.empty();
        var kindMappings = this.getNounMappingsOfKind(kind);
        for (NounMapping nnm : kindMappings) {
            referencesOfKind.add(nnm.getReference());
        }
        return Lists.immutable.withAll(referencesOfKind);
    }

    private Predicate<? super NounMapping> nounMappingIsOfKind(MappingKind mappingKind) {
        return n -> n.getKind() == mappingKind;
    }

    void addNounMappingAddPhraseMapping(NounMapping nounMapping) {
        this.addNounMappingToState(nounMapping);
        if (this.phraseMappings.anySatisfy(it -> {
            SortedIterable<Phrase> sortedIt = it.getPhrases();
            SortedIterable<Phrase> phrases = nounMapping.getPhrases();
            return Comparators.collectionsIdentityAnyOrder(sortedIt, phrases);
        })) {
            return;
        }
        PhraseMapping phraseMappingImpl = new PhraseMappingImpl(nounMapping.getPhrases());
        this.phraseMappings.add(phraseMappingImpl);
    }

    @Override
    public void removeNounMapping(NounMapping nounMapping, NounMapping replacement) {
        PhraseMapping phraseMapping = this.getPhraseMappingByNounMapping(nounMapping);

        var otherNounMappings = this.getNounMappingsThatBelongToTheSamePhraseMapping(nounMapping);
        if (!otherNounMappings.isEmpty()) {
            var phrases = nounMapping.getPhrases().select(p -> !otherNounMappings.flatCollect(NounMapping::getPhrases).contains(p));
            phrases.forEach(phraseMapping::removePhrase);
        }
        this.removeNounMappingFromState(nounMapping, replacement);
    }

    private void addNounMappingToState(NounMapping nounMapping) {
        if (this.nounMappings.contains(nounMapping)) {
            throw new IllegalArgumentException("Nounmapping was already in state");
        }
        this.nounMappings.add(nounMapping);
        this.nounMappings.sortThis(ORDER_NOUNMAPPING);
    }

    /**
     * Removes the specified phrase mapping from the state and replaces it with an (optional) replacement
     *
     * @param phraseMapping the mapping
     * @param replacement   the replacement
     * @return true if removed, false otherwise
     */
    boolean removePhraseMappingFromState(PhraseMapping phraseMapping, PhraseMapping replacement) {
        var success = this.phraseMappings.remove(phraseMapping);
        phraseMapping.onDelete(replacement);
        return success;
    }

    /**
     * Removes the specified noun mapping from the state and replaces it with an (optional) replacement
     *
     * @param nounMapping the mapping
     * @param replacement the replacement
     * @return true if removed, false otherwise
     */
    boolean removeNounMappingFromState(NounMapping nounMapping, NounMapping replacement) {
        var success = this.nounMappings.remove(nounMapping);
        nounMapping.onDelete(replacement);
        return success;
    }

    @Override
    public String toString() {
        return "TextExtractionState [NounMappings: \n" + this.getNounMappings() + "\n PhraseMappings: \n" + this.getPhraseMappings() + "]";
    }

    @Override
    protected void delegateApplyConfigurationToInternalObjects(SortedMap<String, String> additionalConfiguration) {
        // handle additional configuration
    }
}
