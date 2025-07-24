/* Licensed under MIT 2025. */
package edu.kit.kastel.mcse.ardoco.naer.recognizer;

import java.util.HashSet;
import java.util.Set;

import edu.kit.kastel.mcse.ardoco.naer.model.NamedEntity;

/**
 * Represents the occurrence of a component in a text in a simple way (using only the component name and the line number of the occurrence - in alignment with
 * the information we currently have in the goldstandards).
 *
 * @param componentName  name of the component
 * @param sentenceNumber 1-indexed number of the sentence where the component is mentioned
 */
public record SimpleComponentOccurrence(String componentName, int sentenceNumber) implements Comparable<SimpleComponentOccurrence> {

    public static Set<SimpleComponentOccurrence> fromComponents(Set<NamedEntity> components) {
        Set<SimpleComponentOccurrence> result = new HashSet<>();

        for (NamedEntity component : components) {
            for (int lineNumber : component.getOccurrenceLines()) {
                result.add(new SimpleComponentOccurrence(component.getName(), lineNumber));
            }
        }

        return result;
    }

    @Override
    public int compareTo(SimpleComponentOccurrence other) {
        int sentenceCompare = Integer.compare(this.sentenceNumber, other.sentenceNumber);
        if (sentenceCompare != 0) {
            return sentenceCompare;
        }
        return this.componentName.compareTo(other.componentName);
    }

}
