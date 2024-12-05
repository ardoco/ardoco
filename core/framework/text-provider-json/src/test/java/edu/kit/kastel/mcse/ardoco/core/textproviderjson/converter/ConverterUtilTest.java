/* Licensed under MIT 2023-2024. */
package edu.kit.kastel.mcse.ardoco.core.textproviderjson.converter;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.collections.api.factory.Lists;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import edu.kit.kastel.mcse.ardoco.core.api.text.Phrase;
import edu.kit.kastel.mcse.ardoco.core.textproviderjson.textobject.PhraseImpl;
import edu.kit.kastel.mcse.ardoco.core.textproviderjson.textobject.SentenceImpl;

class ConverterUtilTest {

    @Test
    void testGetChildPhrases() {
        // no child phrases
        Phrase parentPhrase1 = new PhraseImpl(null, null, new ArrayList<>());
        List<Phrase> expected1 = new ArrayList<>();
        List<Phrase> actual1 = ConverterUtil.getChildPhrases(parentPhrase1);
        Assertions.assertEquals(expected1, actual1);

        // some child phrases
        Phrase childPhrase1 = new PhraseImpl(null, null, new ArrayList<>());
        Phrase childPhrase2 = new PhraseImpl(null, null, new ArrayList<>());
        Phrase parentPhrase2 = new PhraseImpl(null, null, List.of(childPhrase1, childPhrase2));
        List<Phrase> expected2 = List.of(childPhrase1, childPhrase2);
        List<Phrase> actual2 = ConverterUtil.getChildPhrases(parentPhrase2);
        Assertions.assertEquals(expected2, actual2);

        // some child phrases with subphrases
        Phrase childPhrase3 = new PhraseImpl(null, null, new ArrayList<>());
        Phrase childPhrase4 = new PhraseImpl(null, null, new ArrayList<>());
        Phrase childPhrase5 = new PhraseImpl(null, null, List.of(childPhrase3, childPhrase4));
        Phrase parentPhrase3 = new PhraseImpl(null, null, List.of(childPhrase5));
        List<Phrase> expected3 = List.of(childPhrase5);
        List<Phrase> actual3 = ConverterUtil.getChildPhrases(parentPhrase3);
        Assertions.assertEquals(expected3, actual3);
    }

    @Test
    void testGetChildPhrasesSentence() {
        // no child phrases
        SentenceImpl sentence1 = new SentenceImpl(0, null, null);
        sentence1.setPhrases(Lists.mutable.empty());
        List<Phrase> expected1 = new ArrayList<>();
        List<Phrase> actual1 = ConverterUtil.getChildPhrases(sentence1);
        Assertions.assertEquals(expected1, actual1);

        // some child phrases
        Phrase childPhrase1 = new PhraseImpl(null, null, new ArrayList<>());
        Phrase childPhrase2 = new PhraseImpl(null, null, new ArrayList<>());
        SentenceImpl sentence2 = new SentenceImpl(0, null, null);
        sentence2.setPhrases(Lists.mutable.of(childPhrase1, childPhrase2));
        List<Phrase> expected2 = List.of(childPhrase1, childPhrase2);
        List<Phrase> actual2 = ConverterUtil.getChildPhrases(sentence2);
        Assertions.assertEquals(expected2, actual2);

        // some child phrases with subphrases
        Phrase childPhrase3 = new PhraseImpl(null, null, new ArrayList<>());
        Phrase childPhrase4 = new PhraseImpl(null, null, new ArrayList<>());
        Phrase parentPhrase3 = new PhraseImpl(null, null, List.of(childPhrase3, childPhrase4));
        SentenceImpl sentence3 = new SentenceImpl(0, null, null);
        sentence3.setPhrases(Lists.mutable.of(parentPhrase3));
        List<Phrase> expected3 = List.of(parentPhrase3);
        List<Phrase> actual3 = ConverterUtil.getChildPhrases(sentence3);
        Assertions.assertEquals(expected3, actual3);
    }
}
