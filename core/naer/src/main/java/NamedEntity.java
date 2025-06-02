import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import kotlin.Pair;

import java.util.HashSet;
import java.util.List;
import java.util.Set;


/**
 * Represents a named entity.
 */
public class NamedEntity {
    private final String name;
    private final NamedEntityType type;
    /**
     * alternative names of the entity, e.g., if the name is ambiguous
     */
    private Set<String> alternativeNames = new HashSet<>();
    /**
     * all occurrences of the entity in a text, including the sentence number (1-indexed) and the type of how the entity is referenced //todo [Frage] ist es architektur mäßig jetzt schlecht hier von dem text zu reden obwohl der nicht von der Klasse aus referenziert wird oder so?
     */
    private Set<Pair<Integer, NamedEntityReferenceType>> sentenceOccurrences = new HashSet<>();

    @JsonCreator
    public NamedEntity(@JsonProperty("name") String name, @JsonProperty("type") NamedEntityType type, @JsonProperty("alternativeNames") List<String> alternativeNames, @JsonProperty("occurrences") List<Occurrence> occurrences) {
        this.name = name;
        this.type = type;
        if (alternativeNames != null) {
            this.alternativeNames.addAll(alternativeNames);
        }
        if (occurrences != null) {
            for (Occurrence occurrence : occurrences) {
                this.sentenceOccurrences.add(new Pair<>(occurrence.sentenceNumber(), occurrence.referenceType()));
            }
        }
    }

    @Override
    public String toString() {
        return "NamedEntity{" +
                "name='" + name + '\'' +
                ", type=" + type +
                ", alternativeNames=" + alternativeNames +
                ", sentenceOccurrences=" + sentenceOccurrences +
                '}';
    }

    private enum NamedEntityReferenceType {
        DIRECT,
        CO_REFERENCE
    }

    // needed for JSON mapping
    private record Occurrence(int sentenceNumber, NamedEntityReferenceType referenceType) {
        @JsonCreator
        private Occurrence(@JsonProperty("line") int sentenceNumber, @JsonProperty("referenceType") NamedEntityReferenceType referenceType) {
            this.sentenceNumber = sentenceNumber;
            this.referenceType = referenceType;
        }
    }
}
