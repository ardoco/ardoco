/* Licensed under MIT 2022-2024. */
package edu.kit.kastel.mcse.ardoco.tlr.textextraction;

import org.eclipse.collections.api.factory.SortedMaps;
import org.eclipse.collections.api.factory.SortedSets;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.list.MutableList;
import org.eclipse.collections.api.map.sorted.ImmutableSortedMap;
import org.eclipse.collections.api.map.sorted.MutableSortedMap;
import org.eclipse.collections.api.set.sorted.ImmutableSortedSet;

import edu.kit.kastel.mcse.ardoco.core.api.stage.textextraction.MappingKind;
import edu.kit.kastel.mcse.ardoco.core.api.stage.textextraction.NounMapping;
import edu.kit.kastel.mcse.ardoco.core.api.stage.textextraction.TextState;
import edu.kit.kastel.mcse.ardoco.core.api.stage.textextraction.TextStateStrategy;
import edu.kit.kastel.mcse.ardoco.core.api.text.Word;
import edu.kit.kastel.mcse.ardoco.core.architecture.Deterministic;
import edu.kit.kastel.mcse.ardoco.core.common.similarity.SimilarityUtils;
import edu.kit.kastel.mcse.ardoco.core.data.Confidence;
import edu.kit.kastel.mcse.ardoco.core.pipeline.agent.Claimant;

@Deterministic
public abstract class DefaultTextStateStrategy implements TextStateStrategy {
    protected TextStateImpl textState;

    protected DefaultTextStateStrategy() {
        // NOP
    }

    @Override
    public void setState(TextState textState) {
        if (this.textState != null) {
            throw new IllegalStateException("The text state is already set");
        }
        if (!(textState instanceof TextStateImpl)) {
            throw new IllegalArgumentException("The text state must be an instance of TextStateImpl");
        }
        this.textState = (TextStateImpl) textState;
    }

    public TextStateImpl getTextState() {
        return this.textState;
    }

    /**
     * Creates a new noun mapping using the parameters without adding it to the state.
     *
     * @param words          the words
     * @param distribution   the distribution of the mappings kinds
     * @param referenceWords the reference words
     * @param surfaceForms   the surface forms
     * @param reference      the joined reference, nullable
     * @return the created noun mapping
     */
    public NounMapping createNounMappingStateless(ImmutableSortedSet<Word> words, ImmutableSortedMap<MappingKind, Confidence> distribution,
            ImmutableList<Word> referenceWords, ImmutableList<String> surfaceForms, String reference) {
        if (reference == null) {
            reference = this.calculateNounMappingReference(referenceWords);
        }

        return new NounMappingImpl(words, distribution.toImmutable(), referenceWords, surfaceForms, reference);
    }

    @Override
    public ImmutableList<NounMapping> getNounMappingsWithSimilarReference(String reference) {
        return this.textState.getNounMappings().select(nm -> SimilarityUtils.getInstance().areWordsSimilar(reference, nm.getReference())).toImmutable();
    }

    @Override
    public NounMapping addNounMapping(ImmutableSortedSet<Word> words, ImmutableSortedMap<MappingKind, Confidence> distribution,
            ImmutableList<Word> referenceWords, ImmutableList<String> surfaceForms, String reference) {
        //Do not add noun mappings to the state, which do not have any claimants
        if (distribution.valuesView().noneSatisfy(d -> !d.getClaimants().isEmpty())) {
            throw new IllegalArgumentException("Atleast 1 claimant is required");
        }

        NounMapping nounMapping = this.createNounMappingStateless(words, distribution, referenceWords, surfaceForms, reference);
        this.getTextState().addNounMappingAddPhraseMapping(nounMapping);
        return nounMapping;
    }

    @Override
    public NounMapping addNounMapping(ImmutableSortedSet<Word> words, MappingKind kind, Claimant claimant, double probability,
            ImmutableList<Word> referenceWords, ImmutableList<String> surfaceForms, String reference) {
        MutableSortedMap<MappingKind, Confidence> distribution = SortedMaps.mutable.empty();
        distribution.put(MappingKind.NAME, new Confidence(DEFAULT_AGGREGATOR));
        distribution.put(MappingKind.TYPE, new Confidence(DEFAULT_AGGREGATOR));
        var nounMapping = this.createNounMappingStateless(words, distribution.toImmutable(), referenceWords, surfaceForms, reference);
        nounMapping.addKindWithProbability(kind, claimant, probability);
        this.getTextState().addNounMappingAddPhraseMapping(nounMapping);
        return nounMapping;
    }

    public NounMapping mergeNounMappings(NounMapping nounMapping, MutableList<NounMapping> nounMappingsToMerge, Claimant claimant) {
        for (NounMapping nounMappingToMerge : nounMappingsToMerge) {

            if (!this.textState.getNounMappings().contains(nounMappingToMerge)) {

                final NounMapping finalNounMappingToMerge = nounMappingToMerge;
                var fittingNounMappings = this.textState.getNounMappings().select(nm -> nm.getWords().containsAllIterable(finalNounMappingToMerge.getWords()));
                if (fittingNounMappings.isEmpty()) {
                    continue;
                }
                if (fittingNounMappings.size() != 1) {
                    throw new IllegalStateException();
                }
                nounMappingToMerge = fittingNounMappings.get(0);
            }

            assert this.textState.getNounMappings().contains(nounMappingToMerge);

            var references = nounMapping.getReferenceWords().toList();
            references.addAllIterable(nounMappingToMerge.getReferenceWords());
            this.textState.mergeNounMappings(nounMapping, nounMappingToMerge, claimant, references.toImmutable());

            var mergedWords = SortedSets.mutable.empty();
            mergedWords.addAllIterable(nounMapping.getWords());
            mergedWords.addAllIterable(nounMappingToMerge.getWords());

            var mergedNounMapping = this.textState.getNounMappings().select(nm -> nm.getWords().toSortedSet().equals(mergedWords));

            assert (mergedNounMapping.size() == 1);

            nounMapping = mergedNounMapping.get(0);
        }

        return nounMapping;
    }

    protected final Confidence putAllConfidencesTogether(Confidence confidence, Confidence confidence1) {

        Confidence result = confidence.createCopy();
        result.addAllConfidences(confidence1);
        return result;
    }

}
