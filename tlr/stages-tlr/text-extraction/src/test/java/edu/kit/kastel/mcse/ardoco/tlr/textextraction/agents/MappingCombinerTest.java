/* Licensed under MIT 2022-2024. */
package edu.kit.kastel.mcse.ardoco.tlr.textextraction.agents;

import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.list.MutableList;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

import edu.kit.kastel.mcse.ardoco.core.api.stage.textextraction.MappingKind;
import edu.kit.kastel.mcse.ardoco.core.api.stage.textextraction.NounMapping;
import edu.kit.kastel.mcse.ardoco.core.api.stage.textextraction.PhraseMapping;
import edu.kit.kastel.mcse.ardoco.core.api.stage.textextraction.TextState;
import edu.kit.kastel.mcse.ardoco.core.api.text.Phrase;
import edu.kit.kastel.mcse.ardoco.core.api.text.PhraseType;
import edu.kit.kastel.mcse.ardoco.core.api.text.Word;
import edu.kit.kastel.mcse.ardoco.core.common.similarity.PhraseMappingAggregatorStrategy;
import edu.kit.kastel.mcse.ardoco.core.common.similarity.SimilarityUtils;
import edu.kit.kastel.mcse.ardoco.core.data.DataRepository;
import edu.kit.kastel.mcse.ardoco.core.pipeline.agent.Claimant;
import edu.kit.kastel.mcse.ardoco.tlr.text.providers.informants.corenlp.PhraseImpl;
import edu.kit.kastel.mcse.ardoco.tlr.textextraction.PhraseConcerningTextStateStrategy;
import edu.kit.kastel.mcse.ardoco.tlr.textextraction.TextStateImpl;

@Disabled("Java 21 vs. Mockito")
class MappingCombinerTest implements Claimant {

    // Has to be aligned to MappingCombinerInformant
    private static final double MIN_COSINE_SIMILARITY = 0.4;

    private MappingCombiner agent;

    private TextStateImpl preTextState;
    private TextStateImpl textState;

    private Word fox0;
    private Word dog1;
    private Word fox2;
    private Word dog3;
    private Word hut3;
    private Word turtle4;

    private Word doggy5;

    private Word dog6;

    private PhraseImpl dogPhrase0;

    private PhraseImpl dogPhrase1;

    private PhraseImpl turtlePhrase2;

    private PhraseImpl dogPhrase3;

    private PhraseImpl dogPhrase4;
    private DataRepository data;

    @BeforeEach
    void setup() {
        this.data = new DataRepository();
        this.agent = new MappingCombiner(this.data);
        PhraseConcerningTextStateStrategy strategy = new PhraseConcerningTextStateStrategy();
        this.preTextState = new TextStateImpl(strategy);

        Word a0 = Mockito.mock(Word.class);
        Word fast0 = Mockito.mock(Word.class);
        Word fox0 = Mockito.mock(Word.class);
        Word the1 = Mockito.mock(Word.class);
        Word brown1 = Mockito.mock(Word.class);
        Word dog1 = Mockito.mock(Word.class);

        PhraseImpl foxPhrase0 = Mockito.mock(PhraseImpl.class);
        PhraseImpl dogPhrase0 = Mockito.mock(PhraseImpl.class);

        this.mockPhrase(foxPhrase0, "A fast fox", 0, Lists.immutable.with(a0, fast0, fox0));
        this.mockPhrase(dogPhrase0, "the brown dog", 0, Lists.immutable.with(the1, brown1, dog1));

        this.mockWord(a0, "a", "a", 0, foxPhrase0, 0);
        this.mockWord(fast0, "fast", "fast", 0, foxPhrase0, 1);
        this.mockWord(fox0, "fox", "fox", 0, foxPhrase0, 2);

        this.mockWord(the1, "the", "the", 0, dogPhrase0, 6);
        this.mockWord(brown1, "brown", "brown", 0, dogPhrase0, 7);
        this.mockWord(dog1, "dog", "dog", 0, dogPhrase0, 8);

        Word the2 = Mockito.mock(Word.class);
        Word lazy2 = Mockito.mock(Word.class);
        Word fox2 = Mockito.mock(Word.class);
        Word the3 = Mockito.mock(Word.class);
        Word brown3 = Mockito.mock(Word.class);
        Word dog3 = Mockito.mock(Word.class);
        Word hut3 = Mockito.mock(Word.class);

        PhraseImpl foxPhrase1 = Mockito.mock(PhraseImpl.class);
        PhraseImpl dogPhrase1 = Mockito.mock(PhraseImpl.class);

        this.mockPhrase(foxPhrase1, "The lazy fox", 1, Lists.immutable.with(the2, lazy2, fox2));
        this.mockPhrase(dogPhrase1, "the brown dog hut", 1, Lists.immutable.with(the3, brown3, dog3, hut3));

        this.mockWord(the2, "the", "the", 1, foxPhrase1, 0);
        this.mockWord(lazy2, "lazy", "lazy", 1, foxPhrase1, 1);
        this.mockWord(fox2, "fox", "fox", 1, foxPhrase1, 2);

        this.mockWord(the3, "the", "the", 1, dogPhrase1, 5);
        this.mockWord(brown3, "brown", "brown", 1, dogPhrase1, 6);
        this.mockWord(dog3, "dog", "dog", 1, dogPhrase1, 7);
        this.mockWord(hut3, "hut", "hut", 1, dogPhrase1, 8);

        Word i4 = Mockito.mock(Word.class);
        Word green4 = Mockito.mock(Word.class);
        Word turtle4 = Mockito.mock(Word.class);
        Word hats4 = Mockito.mock(Word.class);

        PhraseImpl iPhrase2 = Mockito.mock(PhraseImpl.class);
        PhraseImpl turtlePhrase2 = Mockito.mock(PhraseImpl.class);

        this.mockPhrase(iPhrase2, "I", 2, Lists.immutable.with(i4));
        this.mockPhrase(turtlePhrase2, "green turtle hats", 2, Lists.immutable.with(green4, turtle4, hats4));

        this.mockWord(i4, "I", "i", 2, iPhrase2, 0);
        this.mockWord(green4, "green", "green", 2, turtlePhrase2, 2);
        this.mockWord(turtle4, "turtles", "turtles", 2, turtlePhrase2, 3);
        this.mockWord(hats4, "hats", "hat", 2, turtlePhrase2, 4);

        Word a5 = Mockito.mock(Word.class);
        Word brown5 = Mockito.mock(Word.class);
        Word doggy5 = Mockito.mock(Word.class);
        Word hut5 = Mockito.mock(Word.class);
        PhraseImpl dogPhrase3 = Mockito.mock(PhraseImpl.class);

        this.mockPhrase(dogPhrase3, "A brown doggy hut", 3, Lists.immutable.with(a5, brown5, doggy5, hut5));

        this.mockWord(a5, "a", "a", 3, dogPhrase3, 0);
        this.mockWord(brown5, "brown", "brown", 3, dogPhrase3, 1);
        this.mockWord(doggy5, "doggy", "dog", 3, dogPhrase3, 2);
        this.mockWord(hut5, "hut", "hut", 3, dogPhrase3, 3);

        Word green6 = Mockito.mock(Word.class);
        Word dog6 = Mockito.mock(Word.class);
        Word hats6 = Mockito.mock(Word.class);
        PhraseImpl dogPhrase4 = Mockito.mock(PhraseImpl.class);

        this.mockPhrase(dogPhrase4, "green dog hats", 4, Lists.immutable.with(green6, dog6, hats6));

        this.mockWord(green6, "green", "green", 4, dogPhrase4, 2);
        this.mockWord(dog6, "dog", "dog", 4, dogPhrase4, 3);
        this.mockWord(hats6, "hats", "hat", 4, dogPhrase4, 4);

        this.fox0 = fox0;
        this.dog1 = dog1;
        this.fox2 = fox2;
        this.dog3 = dog3;
        this.hut3 = hut3;
        this.turtle4 = turtle4;
        this.doggy5 = doggy5;
        this.dog6 = dog6;
        this.dogPhrase0 = dogPhrase0;
        this.dogPhrase1 = dogPhrase1;
        this.turtlePhrase2 = turtlePhrase2;
        this.dogPhrase3 = dogPhrase3;
        this.dogPhrase4 = dogPhrase4;
    }

    @Test
    void copy() {
        this.preTextState.addNounMapping(this.fox0, MappingKind.NAME, this, 0.5);

        this.textState = this.createCopy(this.preTextState);
        this.data.addData(TextState.ID, this.textState);
        this.agent.run();

        Assertions.assertEquals(this.preTextState.getNounMappings(), this.textState.getNounMappings());
        Assertions.assertEquals(this.preTextState.getPhraseMappings(), this.textState.getPhraseMappings());

        this.preTextState.addNounMapping(this.dog3, MappingKind.NAME, this, 0.5);

        Assertions.assertNotEquals(this.preTextState.getNounMappings(), this.textState.getNounMappings());
        Assertions.assertNotEquals(this.preTextState.getPhraseMappings(), this.textState.getPhraseMappings());
    }

    @Test
    void addEqualNounMappingWithEqualPhrase() {

        this.preTextState.addNounMapping(this.fox0, MappingKind.NAME, this, 0.5);

        var fox0NM = this.preTextState.getNounMappingByWord(this.fox0);
        Assertions.assertNotNull(this.preTextState.getPhraseMappingByNounMapping(fox0NM));

        this.preTextState.addNounMapping(this.fox0, MappingKind.NAME, this, 0.5);
        Assertions.assertEquals(1, this.preTextState.getNounMappings().size());
        Assertions.assertEquals(1, this.preTextState.getPhraseMappings().size());

        this.textState = this.createCopy(this.preTextState);
        this.data.addData(TextState.ID, this.textState);
        this.agent.run();

        Assertions.assertEquals(this.preTextState.getNounMappings(), this.textState.getNounMappings());
        Assertions.assertEquals(this.preTextState.getPhraseMappings(), this.textState.getPhraseMappings());
    }

    @Test
    void addTextualEqualNounMappingWithSimilarPhrase() {

        this.preTextState.addNounMapping(this.dog1, MappingKind.NAME, this, 0.5);
        this.preTextState.addNounMapping(this.dog3, MappingKind.NAME, this, 0.5);

        Assertions.assertTrue(this.phraseMappingsAreSimilar(this.preTextState, this.dog1, this.dog3));
        Assertions.assertTrue(SimilarityUtils.getInstance()
                .areNounMappingsSimilar(this.preTextState.getNounMappingByWord(this.dog1), this.preTextState.getNounMappingByWord(this.dog3)));

        Assertions.assertEquals(2, this.preTextState.getNounMappings().size());
        Assertions.assertEquals(2, this.preTextState.getPhraseMappings().size());

        this.textState = this.createCopy(this.preTextState);
        this.data.addData(TextState.ID, this.textState);
        this.agent.run();

        Assertions.assertTrue(this.nounMappingsWereMerged(this.preTextState, this.dog1, this.dog3, this.textState));
        Assertions.assertTrue(this.phraseMappingsWereMerged(this.preTextState, this.dog1, this.dog3, this.textState));
    }

    @Test
    void addTextualEqualNounMappingWithDifferentPhrase() {

        this.preTextState.addNounMapping(this.fox0, MappingKind.NAME, this, 0.5);
        this.preTextState.addNounMapping(this.fox2, MappingKind.NAME, this, 0.5);

        Assertions.assertFalse(this.phraseMappingsAreSimilar(this.preTextState, this.fox0, this.fox2));

        Assertions.assertEquals(2, this.preTextState.getNounMappings().size());
        Assertions.assertEquals(2, this.preTextState.getPhraseMappings().size());

        this.textState = this.createCopy(this.preTextState);
        this.data.addData(TextState.ID, this.textState);
        this.agent.run();

        Assertions.assertEquals(2, this.textState.getNounMappings().size());
        Assertions.assertEquals(2, this.textState.getPhraseMappings().size());
    }

    @Test
    void addSimilarNounMappingWithEqualPhrase() {

        Word alternativeDog = Mockito.mock(Word.class);
        this.mockWord(alternativeDog, "doggy", "doggy", 0, this.dogPhrase0, 0);

        this.preTextState.addNounMapping(this.dog1, MappingKind.NAME, this, 0.5);
        this.preTextState.addNounMapping(alternativeDog, MappingKind.NAME, this, 0.5);

        this.textState = this.createCopy(this.preTextState);
        this.data.addData(TextState.ID, this.textState);
        this.agent.run();

        Assertions.assertNotEquals(null, this.textState.getNounMappingByWord(this.dog1));
        Assertions.assertEquals(this.textState.getNounMappingByWord(this.dog1), this.textState.getNounMappingByWord(alternativeDog));

        NounMapping doggyNounMapping = this.textState.getNounMappingByWord(alternativeDog);

        Assertions.assertAll(//
                () -> Assertions.assertTrue(doggyNounMapping.getWords().contains(alternativeDog)), //
                () -> Assertions.assertTrue(doggyNounMapping.getWords().contains(this.dog1)), //
                () -> Assertions.assertEquals(1, doggyNounMapping.getPhrases().size())//
        );

        var doggyPhraseMapping = this.textState.getPhraseMappingByNounMapping(doggyNounMapping);
        Assertions.assertEquals(1, doggyPhraseMapping.getPhrases().size());//

    }

    @Test
    void addSimilarNounMappingWithSimilarPhraseContainingSimilarNounMapping() {

        this.preTextState.addNounMapping(this.dog1, MappingKind.NAME, this, 0.5);
        this.preTextState.addNounMapping(this.dog3, MappingKind.TYPE, this, 0.5);

        Assertions.assertTrue(this.phraseMappingsAreSimilar(this.preTextState, this.dog1, this.dog3));

        this.textState = this.createCopy(this.preTextState);
        this.data.addData(TextState.ID, this.textState);
        this.agent.run();

        Assertions.assertTrue(this.nounMappingsWereMerged(this.preTextState, this.dog1, this.dog3, this.textState));
        Assertions.assertEquals(1, this.textState.getNounMappings().size());
    }

    @Test
    void addSimilarNounMappingWithSimilarPhraseContainingNoSimilarNounMapping() {

        this.preTextState.addNounMapping(this.turtle4, MappingKind.TYPE, this, 0.5);
        this.preTextState.addNounMapping(this.dog6, MappingKind.NAME, this, 0.5);

        Assertions.assertTrue(this.phraseMappingsAreSimilar(this.preTextState, this.turtle4, this.dog6));

        this.textState = this.createCopy(this.preTextState);
        this.data.addData(TextState.ID, this.textState);
        this.agent.run();

        Assertions.assertAll(//
                () -> Assertions.assertEquals(this.preTextState.getNounMappings(), this.textState.getNounMappings()), () -> Assertions.assertEquals(
                        this.preTextState.getPhraseMappings(), this.textState.getPhraseMappings()));
    }

    @Test
    void addSimilarNounMappingWithDifferentPhrase() {

        this.preTextState.addNounMapping(this.dog1, MappingKind.NAME, this, 0.5);
        this.preTextState.addNounMapping(this.doggy5, MappingKind.TYPE, this, 0.5);

        var dog = this.dogPhrase1.compareTo(this.dogPhrase3);

        Assertions.assertFalse(this.phraseMappingsAreSimilar(this.preTextState, this.dog1, this.doggy5));

        this.textState = this.createCopy(this.preTextState);
        this.data.addData(TextState.ID, this.textState);
        this.agent.run();

        Assertions.assertAll(//
                () -> Assertions.assertEquals(this.preTextState.getNounMappings(), this.textState.getNounMappings()), () -> Assertions.assertEquals(
                        this.preTextState.getPhraseMappings(), this.textState.getPhraseMappings()));

    }

    @Test
    void addDifferentNounMappingWithEqualPhrase() {

        this.preTextState.addNounMapping(this.dog1, MappingKind.NAME, this, 0.5);
        this.preTextState.addNounMapping(this.dog3, MappingKind.NAME, this, 0.5);
        this.preTextState.addNounMapping(this.hut3, MappingKind.TYPE, this, 0.5);

        Assertions.assertTrue(this.phraseMappingsAreSimilar(this.preTextState, this.dog1, this.dog3));
        Assertions.assertTrue(this.phraseMappingsAreSimilar(this.preTextState, this.dog1, this.hut3));

        this.textState = this.createCopy(this.preTextState);
        this.data.addData(TextState.ID, this.textState);
        this.agent.run();

        PhraseMapping dog1PhraseMapping = this.textState.getPhraseMappings().select(pm -> pm.getPhrases().contains(this.dogPhrase1)).get(0);

        Assertions.assertAll(//

                () -> Assertions.assertEquals(this.preTextState.getNounMappings(), this.textState.getNounMappings()), () -> Assertions.assertEquals(1,
                        this.textState.getNounMappings().select(nm -> nm.getWords().contains(this.hut3)).size()), () -> Assertions.assertEquals(1,
                                this.textState.getNounMappings().select(nm -> nm.getWords().contains(this.dog3)).size()),

                () -> Assertions.assertEquals(this.preTextState.getPhraseMappings(), this.textState.getPhraseMappings()), () -> Assertions.assertEquals(1,
                        this.textState.getPhraseMappings().select(pm -> pm.getPhrases().contains(this.dogPhrase1)).size()), () -> Assertions.assertTrue(
                                this.textState.getNounMappingsByPhraseMapping(this.textState.getPhraseMappings()
                                        .select(pm -> pm.getPhrases().contains(this.dogPhrase0))
                                        .get(0)).select(nm -> nm.getWords().contains(this.dog3)).isEmpty()),

                () -> Assertions.assertEquals(1, this.textState.getNounMappingsByPhraseMapping(dog1PhraseMapping)
                        .select(nm -> nm.getWords().contains(this.dog3))
                        .size()), () -> Assertions.assertEquals(1, this.textState.getNounMappingsByPhraseMapping(dog1PhraseMapping)
                                .select(nm -> nm.getWords().contains(this.hut3))
                                .size()));
    }

    @Test
    void addDifferentNounMappingWithSimilarPhrase() {

        this.preTextState.addNounMapping(this.dog1, MappingKind.NAME, this, 0.5);
        this.preTextState.addNounMapping(this.hut3, MappingKind.TYPE, this, 0.5);

        Assertions.assertTrue(this.phraseMappingsAreSimilar(this.preTextState, this.dog1, this.hut3));

        this.textState = this.createCopy(this.preTextState);
        this.data.addData(TextState.ID, this.textState);
        this.agent.run();

        Assertions.assertAll(//
                () -> Assertions.assertEquals(this.preTextState.getNounMappings(), this.textState.getNounMappings()), () -> Assertions.assertEquals(
                        this.preTextState.getPhraseMappings(), this.textState.getPhraseMappings()));

    }

    @Test
    void addDifferentNounMappingWithDifferentPhrase() {
        this.preTextState.addNounMapping(this.dog1, MappingKind.NAME, this, 0.5);
        this.preTextState.addNounMapping(this.turtle4, MappingKind.NAME, this, 0.5);

        Assertions.assertFalse(this.phraseMappingsAreSimilar(this.preTextState, this.dog1, this.turtle4));

        this.textState = this.createCopy(this.preTextState);
        this.data.addData(TextState.ID, this.textState);
        this.agent.run();

        Assertions.assertAll(//
                () -> Assertions.assertEquals(this.preTextState.getNounMappings(), this.textState.getNounMappings()), () -> Assertions.assertEquals(
                        this.preTextState.getPhraseMappings(), this.textState.getPhraseMappings()));
    }

    private boolean phraseMappingsAreSimilar(TextStateImpl textState, Word word1, Word word2) {
        var nm0 = textState.getNounMappingByWord(word1);
        var nm1 = textState.getNounMappingByWord(word2);

        var pm0 = textState.getPhraseMappingByNounMapping(nm0);
        var pm1 = textState.getPhraseMappingByNounMapping(nm1);

        return SimilarityUtils.getInstance()
                .getPhraseMappingSimilarity(textState, pm0, pm1, PhraseMappingAggregatorStrategy.MAX_SIMILARITY) > MappingCombinerTest.MIN_COSINE_SIMILARITY;
    }

    private boolean nounMappingsWereMerged(TextStateImpl preTextState, Word word1, Word word2, TextStateImpl afterTextState) {

        Assertions.assertNotEquals(preTextState.getNounMappings().size(), afterTextState.getNounMappings().size());
        Assertions.assertNotNull(this.textState.getNounMappingByWord(word1));
        Assertions.assertNotNull(this.textState.getNounMappingByWord(word2));
        Assertions.assertEquals(this.textState.getNounMappingByWord(word1), this.textState.getNounMappingByWord(word2));

        var nounMapping = this.textState.getNounMappingByWord(word1);

        Assertions.assertTrue(nounMapping.getWords().contains(word1));
        Assertions.assertTrue(nounMapping.getWords().contains(word2));

        return true;
    }

    private boolean phraseMappingsWereMerged(TextStateImpl preTextState, Word word1, Word word2, TextStateImpl afterTextState) {

        var nounMapping1 = afterTextState.getNounMappingByWord(word1);
        var nounMapping2 = afterTextState.getNounMappingByWord(word2);

        Assertions.assertNotEquals(preTextState.getPhraseMappings().size(), afterTextState.getPhraseMappings().size());
        Assertions.assertEquals(this.textState.getPhraseMappingByNounMapping(nounMapping1), this.textState.getPhraseMappingByNounMapping(nounMapping2));

        var phraseMapping = this.textState.getPhraseMappingByNounMapping(nounMapping1);

        Assertions.assertTrue(phraseMapping.getPhrases().contains(word1.getPhrase()));
        Assertions.assertTrue(phraseMapping.getPhrases().contains(word2.getPhrase()));

        return true;
    }

    private void mockPhrase(PhraseImpl phrase, String text, int sentenceNumber, ImmutableList<Word> containedWords) {
        Mockito.when(phrase.getText()).thenReturn(text);
        Mockito.when(phrase.getPhraseType()).thenReturn(PhraseType.NP);
        Mockito.when(phrase.getSentenceNo()).thenReturn(sentenceNumber);
        Mockito.when(phrase.getContainedWords()).thenReturn(containedWords);
        Mockito.when(phrase.getPhraseVector()).thenCallRealMethod();
        Mockito.when(phrase.toString()).thenCallRealMethod();
        Mockito.when(phrase.compareTo(ArgumentMatchers.any())).thenCallRealMethod();
    }

    private void mockWord(Word word, String text, String lemma, int sentenceNumber, Phrase phrase, int position) {
        Mockito.when(word.getText()).thenReturn(text);
        Mockito.when(word.getPosition()).thenReturn(position);
        Mockito.when(word.getSentenceNo()).thenReturn(sentenceNumber);
        Mockito.when(word.getLemma()).thenReturn(lemma);
        Mockito.when(word.getPhrase()).thenReturn(phrase);
        Mockito.when(word.compareTo(ArgumentMatchers.any())).thenCallRealMethod();
    }

    private TextStateImpl createCopy(TextStateImpl textState) {
        PhraseConcerningTextStateStrategy strategy = new PhraseConcerningTextStateStrategy();
        TextStateImpl newTextState = new TextStateImpl(strategy);

        MutableList<NounMapping> nounMappings = this.getField(textState, "nounMappings");
        MutableList<PhraseMapping> phraseMappings = this.getField(textState, "phraseMappings");

        MutableList<NounMapping> newNounMappings = this.getField(newTextState, "nounMappings");
        MutableList<PhraseMapping> newPhraseMappings = this.getField(newTextState, "phraseMappings");

        newNounMappings.addAll(nounMappings);
        newPhraseMappings.addAll(phraseMappings);
        return newTextState;
    }

    private <T> T getField(Object data, String fieldName) {
        try {
            var field = data.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            return (T) field.get(data);
        } catch (Exception e) {
            Assertions.fail(e.getMessage());
            throw new Error("Unreachable code!");
        }
    }

}
