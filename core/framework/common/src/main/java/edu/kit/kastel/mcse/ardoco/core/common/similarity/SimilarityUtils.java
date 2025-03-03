/* Licensed under MIT 2021-2025. */
package edu.kit.kastel.mcse.ardoco.core.common.similarity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.text.similarity.CosineSimilarity;
import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.list.MutableList;
import org.eclipse.collections.api.map.sorted.ImmutableSortedMap;

import edu.kit.kastel.mcse.ardoco.core.api.entity.Entity;
import edu.kit.kastel.mcse.ardoco.core.api.entity.ModelEntity;
import edu.kit.kastel.mcse.ardoco.core.api.stage.recommendationgenerator.RecommendedInstance;
import edu.kit.kastel.mcse.ardoco.core.api.stage.textextraction.NounMapping;
import edu.kit.kastel.mcse.ardoco.core.api.stage.textextraction.PhraseMapping;
import edu.kit.kastel.mcse.ardoco.core.api.stage.textextraction.TextState;
import edu.kit.kastel.mcse.ardoco.core.api.text.PhraseType;
import edu.kit.kastel.mcse.ardoco.core.api.text.Word;
import edu.kit.kastel.mcse.ardoco.core.architecture.Deterministic;
import edu.kit.kastel.mcse.ardoco.core.common.similarity.wordsim.WordSimUtils;
import edu.kit.kastel.mcse.ardoco.core.common.tuple.Pair;
import edu.kit.kastel.mcse.ardoco.core.common.util.CommonTextToolsConfig;
import edu.kit.kastel.mcse.ardoco.core.common.util.CommonUtilities;

/**
 * This class is a utility class.
 */
@Deterministic
public final class SimilarityUtils {
    private static final SimilarityUtils INSTANCE = new SimilarityUtils(new WordSimUtils());

    private final WordSimUtils wordSimUtils;

    public SimilarityUtils(WordSimUtils wordSimUtils) {
        this.wordSimUtils = wordSimUtils;
    }

    public static SimilarityUtils getInstance() {
        return INSTANCE;
    }

    private static boolean coversOtherPhraseVector(PhraseMapping phraseMapping1, PhraseMapping phraseMapping2) {

        ImmutableSortedMap<Word, Integer> phraseVector1 = phraseMapping1.getPhraseVector();
        ImmutableSortedMap<Word, Integer> phraseVector2 = phraseMapping2.getPhraseVector();

        return phraseVector1.keysView().containsAll(phraseVector2.keysView().toSortedSet());
    }

    static double cosineSimilarity(Map<Word, Integer> firstPhraseVector, Map<Word, Integer> secondPhraseVector) {

        CosineSimilarity cosineSimilarity = new CosineSimilarity();

        Map<CharSequence, Integer> firstVector = firstPhraseVector.entrySet()
                .stream()
                .collect(Collectors.toMap(e -> e.getKey().getText(), Map.Entry::getValue));
        Map<CharSequence, Integer> secondVector = secondPhraseVector.entrySet()
                .stream()
                .collect(Collectors.toMap(e -> e.getKey().getText(), Map.Entry::getValue));

        return cosineSimilarity.cosineSimilarity(firstVector, secondVector);
    }

    public static <A extends Serializable, B extends Serializable> ImmutableList<Pair<A, B>> uniqueDot(ImmutableList<A> first, ImmutableList<B> second) {
        List<Pair<A, B>> result = new ArrayList<>();
        for (A a : first) {
            for (B b : second) {
                result.add(new Pair<>(a, b));
            }
        }
        return Lists.immutable.withAll(result);
    }

    private static int similarEntriesOfList(ImmutableList<String> list1, ImmutableList<String> list2) {
        MutableList<String> removed = Lists.mutable.empty();

        for (var element : list1) {
            if (list2.contains(element) || (list2.select(e -> !removed.contains(e) && (e.contains(element) || element.contains(e))).size() == 1)) {
                removed.add(element);
            }
        }

        return removed.size();
    }

    /**
     * Checks the similarity of two {@link NounMapping}s.
     *
     * @param nm1 the first NounMapping
     * @param nm2 the second NounMapping
     * @return true, if the {@link NounMapping}s are similar; false if not.
     */
    public boolean areNounMappingsSimilar(NounMapping nm1, NounMapping nm2) {
        var nm1Words = nm1.getReferenceWords();
        var nm2Words = nm2.getReferenceWords();
        var nm1Reference = nm1.getReference();
        var nm2Reference = nm2.getReference();
        var nm1SplitAtSeparators = CommonUtilities.splitAtSeparators(nm1Reference);
        var nm2SplitAtSeparators = CommonUtilities.splitAtSeparators(nm2Reference);

        if (nm1SplitAtSeparators.isEmpty() || nm2SplitAtSeparators.isEmpty()) {
            return false;
        }
        var nm1FirstPart = nm1SplitAtSeparators.get(0);
        var nm2FirstPart = nm2SplitAtSeparators.get(0);

        if (nm1Words.size() == 1 && nm2Words.size() == 1) {
            var nm1Word = nm1Words.get(0);
            var nm2Word = nm2Words.get(0);
            return this.areWordsSimilar(nm1FirstPart, nm2FirstPart) || this.areWordsSimilar(nm1Word, nm2Word);
        }
        return this.areWordsSimilar(nm1Reference, nm2Reference);
    }

    /**
     * Compares a given {@link NounMapping} with a given {@link Entity} for similarity. Checks if all names, the longest name or a single name are similar to
     * the reference of the NounMapping.
     *
     * @param nounMapping the {@link NounMapping}
     * @param modelEntity the {@link Entity}
     * @return true, iff the {@link NounMapping} and {@link Entity} are similar.
     */
    public boolean isNounMappingSimilarToModelInstance(NounMapping nounMapping, ModelEntity modelEntity) {
        var nameParts = modelEntity.getNameParts();
        if (nameParts.isEmpty())
            return false;
        if (this.areWordsOfListsSimilar(nameParts, Lists.immutable.with(nounMapping.getReference())) || this.areWordsSimilar(modelEntity.getName(),
                nounMapping.getReference())) {
            return true;
        }

        for (String name : nameParts) {
            if (this.areWordsSimilar(name, nounMapping.getReference())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Compares a given {@link Word} with a given {@link ModelEntity} for similarity.
     *
     * @param word        the {@link Word}
     * @param modelEntity the {@link ModelEntity} of a model
     * @return true, iff the {@link Word} and {@link ModelEntity} are similar.
     */
    public boolean isWordSimilarToEntity(Word word, ModelEntity modelEntity) {
        if (modelEntity.getNameParts().isEmpty()) {
            return false;
        }
        return this.compareWordWithStringListEntries(word, modelEntity.getNameParts());
    }

    //FIXME this method is a duplicate of an existing method in WordSimUtils and should be removed

    /**
     * Compares a given {@link RecommendedInstance} with a given {@link ModelEntity} for similarity.
     *
     * @param ri          the {@link RecommendedInstance}
     * @param modelEntity the {@link ModelEntity}
     * @return true, iff the {@link RecommendedInstance} and {@link ModelEntity} are similar.
     */
    public boolean isRecommendedInstanceSimilarToModelInstance(RecommendedInstance ri, ModelEntity modelEntity) {
        var result = modelEntity.getName().equalsIgnoreCase(ri.getName());

        var name = ri.getName();
        var nameList = Lists.immutable.with(name.split(" "));

        if (modelEntity.getNameParts().isEmpty()) {
            return result;
        }
        return result || this.areWordsOfListsSimilar(modelEntity.getNameParts(), nameList);
    }

    //FIXME this method is a duplicate of an existing method in WordSimUtils and should be removed

    /**
     * Compares a given {@link Word} with the type of a given {@link ModelEntity} for similarity.
     *
     * @param word        the {@link Word}
     * @param modelEntity the {@link ModelEntity}
     * @return true, iff the {@link Word} and the type of the {@link ModelEntity} are similar.
     */
    public boolean isWordSimilarToModelInstanceType(Word word, ModelEntity modelEntity) {

        if (modelEntity.getTypeParts().isEmpty()) {
            return false;
        }
        return this.compareWordWithStringListEntries(word, modelEntity.getTypeParts().get());
    }

    private boolean compareWordWithStringListEntries(Word word, ImmutableList<String> names) {
        return this.compareWordWithStringListEntries(word.getText(), names);
    }

    private boolean compareWordWithStringListEntries(String word, ImmutableList<String> names) {
        for (String name : names) {
            if (this.areWordsSimilar(name, word)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks the similarity of two {@link Word}s.
     *
     * @param word1 the first word
     * @param word2 the second word
     * @return true, if the words are similar; false if not.
     */
    public boolean areWordsSimilar(Word word1, Word word2) {
        return this.wordSimUtils.areWordsSimilar(word1, word2);
    }

    /**
     * Checks the similarity of two string. Uses Jaro-Winkler similarity and Levenshtein to assess the similarity.
     *
     * @param word1 String of first word
     * @param word2 String of second word
     * @return true, if the test string is similar to the original; false if not.
     */
    public boolean areWordsSimilar(String word1, String word2) {
        return this.wordSimUtils.areWordsSimilar(word1, word2);
    }

    /**
     * Checks the similarity of a list with test strings to a list of "original" strings. In this method all test strings are compared to all originals. For
     * this the method uses the areWordsSimilar method with a given threshold. All matches are counted. If the proportion of similarities between the lists is
     * greater than the given threshold the method returns true.
     *
     * @param originals     list of original strings
     * @param words2test    list of test strings
     * @param minProportion threshold for proportional similarity between the lists
     * @return true if the list are similar, false if not
     */
    public boolean areWordsOfListsSimilar(ImmutableList<String> originals, ImmutableList<String> words2test, double minProportion) {

        if (this.areWordsSimilar(String.join(" ", originals), String.join(" ", words2test))) {
            return true;
        }

        var max = Math.max(originals.size(), words2test.size());
        var counterSimilar = 0;
        var counterDissimilar = 0;
        var possiblySimilar = originals.size() * words2test.size();
        for (String o : originals) {
            for (String wd : words2test) {
                if (this.areWordsSimilar(o, wd)) {
                    counterSimilar++;
                    if (1.0 * counterSimilar / max >= minProportion) {
                        return true;
                    }
                } else {
                    counterDissimilar++;
                    if (1.0 * (possiblySimilar - counterDissimilar) / max < minProportion) {
                        return false; //minProportion can no longer be achieved, can stop here
                    }
                }
            }
        }

        return 1.0 * counterSimilar / max >= minProportion;
    }

    /**
     * Checks the similarity of a list, containing test strings, and a list of originals. This check is not bidirectional! This method uses the areWordsSimilar
     * method with a given threshold.
     *
     * @param originals  list of original strings
     * @param words2test list of test strings
     * @return true if the list are similar, false if not
     */
    public boolean areWordsOfListsSimilar(ImmutableList<String> originals, ImmutableList<String> words2test) {
        return this.areWordsOfListsSimilar(originals, words2test, CommonTextToolsConfig.JAROWINKLER_SIMILARITY_THRESHOLD);
    }

    /**
     * Extracts most likely matches of a list of recommended instances by similarity to a given instance. For this, the method uses an increasing minimal
     * proportional threshold with the method areWordsOfListsSimilar method. If all lists are similar to the given instance by a threshold of 1-increase value
     * the while loop can be left. If the while loop ends with more than one possibility or all remaining lists are sorted out in the same run, all are
     * returned. Elsewhere only the remaining recommended instance is returned within the list.
     *
     * @param modelEntity          instance to use as original for compare
     * @param recommendedInstances recommended instances to check for similarity
     * @return a list of the most similar recommended instances (to the instance names)
     */
    public ImmutableList<RecommendedInstance> getMostRecommendedInstancesToInstanceByReferences(ModelEntity modelEntity,
            ImmutableList<RecommendedInstance> recommendedInstances) {

        var instanceNames = modelEntity.getNameParts();
        var similarity = CommonTextToolsConfig.JAROWINKLER_SIMILARITY_THRESHOLD;
        var selection = recommendedInstances.select(ri -> this.checkRecommendedInstanceForSelection(modelEntity, ri, similarity));

        var getMostRecommendedIByRefMinProportion = CommonTextToolsConfig.GET_MOST_RECOMMENDED_I_BY_REF_MIN_PROPORTION;
        var getMostRecommendedIByRefIncrease = CommonTextToolsConfig.GET_MOST_RECOMMENDED_I_BY_REF_INCREASE;

        MutableList<RecommendedInstance> whileSelection = Lists.mutable.withAll(selection);
        var allListsSimilar = 0;

        while (whileSelection.size() > 1 && getMostRecommendedIByRefMinProportion <= 1) {
            selection = Lists.immutable.withAll(whileSelection);
            getMostRecommendedIByRefMinProportion += getMostRecommendedIByRefIncrease;
            MutableList<RecommendedInstance> risToRemove = Lists.mutable.empty();
            for (RecommendedInstance ri : whileSelection) {
                if (this.checkRecommendedInstanceWordSimilarityToInstance(modelEntity, ri)) {
                    allListsSimilar++;
                }

                if (!this.areWordsOfListsSimilar(instanceNames, Lists.immutable.with(ri.getName()), getMostRecommendedIByRefMinProportion)) {
                    risToRemove.add(ri);
                }

            }
            whileSelection.removeAll(risToRemove);
            if (allListsSimilar == whileSelection.size()) {
                return whileSelection.toImmutable();
            }
            allListsSimilar = 0;
        }
        if (whileSelection.isEmpty()) {
            return selection;
        }
        return whileSelection.toImmutable();

    }

    private boolean checkRecommendedInstanceWordSimilarityToInstance(ModelEntity modelEntity, RecommendedInstance ri) {

        var instanceNames = modelEntity.getNameParts();

        if (instanceNames.isEmpty())
            return false;

        for (var sf : ri.getNameMappings().flatCollect(NounMapping::getSurfaceForms)) {
            var splitSF = CommonUtilities.splitCases(String.join(" ", CommonUtilities.splitAtSeparators(sf)));
            if (this.areWordsSimilar(String.join(" ", instanceNames), splitSF)) {
                return true;
            }
        }
        return false;
    }

    private boolean checkRecommendedInstanceForSelection(ModelEntity modelEntity, RecommendedInstance ri, double similarity) {
        var entityNameParts = modelEntity.getNameParts();
        ImmutableList<String> longestNameSplit = Lists.immutable.of(CommonUtilities.splitCases(modelEntity.getName()).split(" "));
        ImmutableList<String> recommendedInstanceNames = Lists.immutable.with(ri.getName());

        boolean instanceNameAndRIName = this.areWordsSimilar(modelEntity.getName(), ri.getName());
        boolean longestNameSplitAndRINames = this.areWordsOfListsSimilar(longestNameSplit, recommendedInstanceNames, similarity);

        boolean instanceNamesAndRIs = this.areWordsOfListsSimilar(entityNameParts, recommendedInstanceNames, similarity);
        boolean listOfNamesSimilarEnough = 1.0 * similarEntriesOfList(entityNameParts, recommendedInstanceNames) / Math.max(entityNameParts.size(),
                recommendedInstanceNames.size()) >= similarity;
        boolean listOfNameSplitSimilarEnough = 1.0 * similarEntriesOfList(longestNameSplit, recommendedInstanceNames) / Math.max(entityNameParts.size(),
                recommendedInstanceNames.size()) >= similarity;

        if (instanceNameAndRIName || instanceNamesAndRIs || longestNameSplitAndRINames || listOfNamesSimilarEnough || listOfNameSplitSimilarEnough) {
            return true;
        }
        for (var nounMapping : ri.getNameMappings()) {
            for (var surfaceForm : nounMapping.getSurfaceForms()) {
                var splitSurfaceForm = CommonUtilities.splitCases(surfaceForm);
                var surfaceFormWords = CommonUtilities.splitAtSeparators(splitSurfaceForm);

                boolean longestNameXSurfaceForms = this.areWordsOfListsSimilar(longestNameSplit, surfaceFormWords, similarity);

                boolean instanceNamesXSurfaceForms = this.areWordsOfListsSimilar(entityNameParts, surfaceFormWords, similarity);
                boolean listOfNamesXSurfaceFormSimilarEnough = 1.0 * similarEntriesOfList(entityNameParts, surfaceFormWords) / Math.max(entityNameParts.size(),
                        surfaceFormWords.size()) >= similarity;

                boolean listOfSplitNamesXSurfaceFormSimilarEnough = 1.0 * similarEntriesOfList(longestNameSplit, surfaceFormWords) / Math.max(
                        longestNameSplit.size(), surfaceFormWords.size()) >= similarity;

                if (instanceNamesXSurfaceForms || longestNameXSurfaceForms || listOfNamesXSurfaceFormSimilarEnough || listOfSplitNamesXSurfaceFormSimilarEnough) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean containsAllNounMappingsOfPhraseMapping(TextState textState, PhraseMapping phraseMapping1, PhraseMapping phraseMapping2) {
        return phraseMapping1.getNounMappings(textState).containsAllIterable(phraseMapping2.getNounMappings(textState));
    }

    public PhraseMapping getMostSimilarPhraseMapping(TextState textState, PhraseMapping phraseMapping, ImmutableList<PhraseMapping> otherPhraseMappings,
            double minCosineSimilarity) {

        if (otherPhraseMappings.isEmpty()) {
            return null;
        }

        double currentMinSimilarity = minCosineSimilarity;
        PhraseMapping mostSimilarPhraseMapping = otherPhraseMappings.get(0);
        for (PhraseMapping otherPhraseMapping : otherPhraseMappings) {
            double similarity = this.getPhraseMappingSimilarity(textState, phraseMapping, otherPhraseMapping, PhraseMappingAggregatorStrategy.MAX_SIMILARITY);
            if (similarity > currentMinSimilarity) {
                currentMinSimilarity = similarity;
                mostSimilarPhraseMapping = otherPhraseMapping;
            }

        }
        return mostSimilarPhraseMapping;
    }

    public double getPhraseMappingSimilarity(TextState textState, PhraseMapping firstPhraseMapping, PhraseMapping secondPhraseMapping,
            PhraseMappingAggregatorStrategy strategy) {
        PhraseType firstPhraseType = firstPhraseMapping.getPhraseType();
        PhraseType secondPhraseType = secondPhraseMapping.getPhraseType();
        if (!firstPhraseType.equals(secondPhraseType)) {
            return 0;
        }

        // Maybe REWORK. Remove NounMappings?
        if ((coversOtherPhraseVector(firstPhraseMapping, secondPhraseMapping) || coversOtherPhraseVector(secondPhraseMapping,
                firstPhraseMapping)) && this.containsAllNounMappingsOfPhraseMapping(textState, firstPhraseMapping,
                secondPhraseMapping) && this.containsAllNounMappingsOfPhraseMapping(textState, secondPhraseMapping, firstPhraseMapping)) {
            // HARD CODED... Change?
            return 1.0;
        }

        return strategy.applyAsDouble(firstPhraseMapping, secondPhraseMapping);
    }

}
