/* Licensed under MIT 2023. */
package edu.kit.kastel.mcse.ardoco.core.textproviderjson.converter;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.collections.api.factory.Lists;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import edu.kit.kastel.mcse.ardoco.core.api.text.POSTag;
import edu.kit.kastel.mcse.ardoco.core.api.text.Phrase;
import edu.kit.kastel.mcse.ardoco.core.api.text.PhraseType;
import edu.kit.kastel.mcse.ardoco.core.api.text.Word;
import edu.kit.kastel.mcse.ardoco.core.textproviderjson.error.NotConvertableException;
import edu.kit.kastel.mcse.ardoco.core.textproviderjson.textobject.PhraseImpl;
import edu.kit.kastel.mcse.ardoco.core.textproviderjson.textobject.WordImpl;

class TreeParserTest {

    String tree = "(ROOT (S (NP (DT This)) (VP (VBZ is) (NP (PRP me))) (. .)))";

    List<Word> words = new ArrayList<>(List.of(new WordImpl(null, 0, 0, "This", POSTag.DETERMINER, null, null, null), new WordImpl(null, 1, 0, "is",
            POSTag.VERB_SINGULAR_PRESENT_THIRD_PERSON, null, null, null), new WordImpl(null, 2, 0, "me", POSTag.PRONOUN_PERSONAL, null, null, null),
            new WordImpl(null, 3, 0, ".", POSTag.CLOSER, null, null, null)));
    Phrase subsubphrase = new PhraseImpl(Lists.immutable.of(words.get(2)), PhraseType.NP, new ArrayList<>());
    List<Phrase> subphrases = List.of(new PhraseImpl(Lists.immutable.of(words.get(0)), PhraseType.NP, new ArrayList<>()), new PhraseImpl(Lists.immutable.of(
            words.get(1)), PhraseType.VP, List.of(subsubphrase)));
    Phrase phrase = new PhraseImpl(Lists.immutable.of(words.get(3)), PhraseType.S, subphrases);
    Phrase expectedPhrase = new PhraseImpl(Lists.immutable.empty(), PhraseType.ROOT, List.of(phrase));

    @Test
    void parseConstituencyTreeTest() throws NotConvertableException {
        DtoToObjectConverter converter = new DtoToObjectConverter();
        Assertions.assertDoesNotThrow(() -> converter.parseConstituencyTree(tree, new ArrayList<>(words)));
        Phrase parsedPhrase = converter.parseConstituencyTree(tree, new ArrayList<>(words));
        Assertions.assertEquals(expectedPhrase, parsedPhrase);
    }
}
