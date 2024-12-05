/* Licensed under MIT 2023-2024. */
package edu.kit.kastel.mcse.ardoco.core.textproviderjson;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import edu.kit.kastel.mcse.ardoco.core.api.text.Phrase;
import edu.kit.kastel.mcse.ardoco.core.api.text.Text;
import edu.kit.kastel.mcse.ardoco.core.common.util.DataRepositoryHelper;
import edu.kit.kastel.mcse.ardoco.core.textproviderjson.converter.DtoToObjectConverter;
import edu.kit.kastel.mcse.ardoco.core.textproviderjson.textobject.PhraseImpl;

class PhraseImplTest {

    private static final DtoToObjectConverter CONVERTER = new DtoToObjectConverter();
    private static Phrase baselinePhrase;
    private PhraseImpl phraseImplInstance;

    @BeforeAll
    static void initAll() {
        Text baselineText = TestUtil.generateTextWithMultipleSentences();
        baselinePhrase = baselineText.getSentences().get(1).getPhrases().get(0);
    }

    @BeforeEach
    void init() throws Exception {
        Text textImplInstance = CONVERTER.convertText(TestUtil.generateDTOWithMultipleSentences());
        phraseImplInstance = (PhraseImpl) textImplInstance.getSentences().get(1).getPhrases().get(0);
    }

    @Test
    void testGetSentenceNo() {
        Assertions.assertEquals(baselinePhrase.getSentenceNo(), phraseImplInstance.getSentenceNo());
    }

    @Test
    void testGetText() {
        Assertions.assertEquals(baselinePhrase.getText(), phraseImplInstance.getText());
    }

    @Test
    void testGetPhraseType() {
        Assertions.assertEquals(baselinePhrase.getPhraseType(), phraseImplInstance.getPhraseType());
    }

    @Test
    void testGetContainedWords() {
        Assertions.assertEquals(baselinePhrase.getContainedWords().size(), phraseImplInstance.getContainedWords().size());
    }

    @Test
    void testGetSubPhrases() {
        Assertions.assertEquals(baselinePhrase.getSubPhrases().size(), phraseImplInstance.getSubPhrases().size());
    }

    @Test
    void testIsSuperPhraseOf() {
        Phrase subphrase = phraseImplInstance.getSubPhrases().get(0);
        Assertions.assertAll(//
                () -> Assertions.assertTrue(phraseImplInstance.isSuperPhraseOf(subphrase)), () -> Assertions.assertFalse(phraseImplInstance.isSuperPhraseOf(
                        phraseImplInstance)), () -> Assertions.assertFalse(subphrase.isSuperPhraseOf(phraseImplInstance))//
        );
    }

    @Test
    void testIsSubPhraseOf() {
        Phrase subphrase = phraseImplInstance.getSubPhrases().get(0);
        Assertions.assertAll(//
                () -> Assertions.assertFalse(phraseImplInstance.isSubPhraseOf(subphrase)), () -> Assertions.assertFalse(phraseImplInstance.isSubPhraseOf(
                        phraseImplInstance)), () -> Assertions.assertTrue(subphrase.isSubPhraseOf(phraseImplInstance))//
        );
    }

    @Test
    void testGetPhraseVector() {
        Assertions.assertEquals(baselinePhrase.getPhraseVector().size(), phraseImplInstance.getPhraseVector().size());
    }

    @Test
    void simpleHashCodeTest() {
        Assertions.assertEquals(phraseImplInstance.hashCode(), phraseImplInstance.hashCode());
    }

    @Test
    void serializationTest() {
        var serializedCopy = DataRepositoryHelper.deepCopy(phraseImplInstance);
        Assertions.assertNotNull(serializedCopy);
    }
}
