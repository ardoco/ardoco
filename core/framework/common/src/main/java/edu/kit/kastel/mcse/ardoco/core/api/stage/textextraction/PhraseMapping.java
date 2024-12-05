/* Licensed under MIT 2022-2024. */
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

    /**
     * Register a listener that will be notified on certain events.
     *
     * @param listener the listener
     * @see #onDelete(PhraseMapping)
     */
    void registerChangeListener(PhraseMappingChangeListener listener);

    /**
     * Will be invoked during the deletion from a state.
     * Note: This can be invoked multiple times if the replacement is not available during deletion of the phrase mapping
     *
     * @param replacement the replacing new phrase mapping (or null if none exist)
     */
    void onDelete(PhraseMapping replacement);
}
