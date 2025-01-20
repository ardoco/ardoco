/* Licensed under MIT 2022-2025. */
package edu.kit.kastel.mcse.ardoco.core.api.stage.textextraction;

import java.io.Serializable;

import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.map.sorted.ImmutableSortedMap;
import org.eclipse.collections.api.set.sorted.ImmutableSortedSet;

import edu.kit.kastel.mcse.ardoco.core.api.text.Phrase;
import edu.kit.kastel.mcse.ardoco.core.api.text.PhraseType;
import edu.kit.kastel.mcse.ardoco.core.api.text.Word;

public interface PhraseMapping extends Serializable {

    ImmutableList<NounMapping> getNounMappings(TextState textState);

    ImmutableSortedSet<Phrase> getPhrases();

    PhraseType getPhraseType();

    ImmutableSortedMap<Word, Integer> getPhraseVector();

    void removePhrase(Phrase phrase);
}
