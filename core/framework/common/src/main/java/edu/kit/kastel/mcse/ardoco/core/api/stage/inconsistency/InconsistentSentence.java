/* Licensed under MIT 2022-2023. */
package edu.kit.kastel.mcse.ardoco.core.api.stage.inconsistency;

import java.util.List;
import java.util.Locale;

import org.eclipse.collections.api.factory.Lists;

import edu.kit.kastel.mcse.ardoco.core.api.text.Sentence;

/**
 * This record represents an inconsistent sentence consisting of a sentence and all the inconsistencies that were found
 * within this sentence.
 */
public record InconsistentSentence(Sentence sentence, List<Inconsistency> inconsistencies) {

    /**
     * Creates a new instance with only one inconsistency. The underlying list is populated with the given
     * inconsistency.
     *
     * @param sentence      the sentence
     * @param inconsistency the inconsistency
     */
    public InconsistentSentence(Sentence sentence, Inconsistency inconsistency) {
        this(sentence, Lists.mutable.of(inconsistency));
    }

    /**
     * Adds an inconsistency to the list of inconsistencies of this sentence.
     *
     * @param inconsistency the inconsistency
     * @return whether the inconsistency was added successfully
     */
    public boolean addInconsistency(Inconsistency inconsistency) {
        return inconsistencies.add(inconsistency);
    }

    /**
     * Creates and returns an info string that contains the sentence number, the text of the sentence, and the reasons
     * of the inconsistencies
     *
     * @return an info string
     */
    public String getInfoString() {
        StringBuilder reasonsBuilder = new StringBuilder();
        for (var inconsistency : inconsistencies) {
            var reason = inconsistency.getReason();
            reasonsBuilder.append(reason).append("\n");
        }

        String formatString = "S%3d: \"%s\"%n\tInconsistent due to the following reasons:%n%s";
        return String.format(Locale.ENGLISH, formatString, sentence.getSentenceNumberForOutput(), sentence.getText(), reasonsBuilder);
    }
}
