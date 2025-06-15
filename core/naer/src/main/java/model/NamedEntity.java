package model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
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
    private final Set<String> alternativeNames;

    /**
     * all occurrences of the entity in the {@link #sourceText}
     */
    private final Set<Occurrence> occurrences;
    /**
     * the software architecture documentation (text) in which the named entity has been recognized
     */
    @Nullable
    private SoftwareArchitectureDocumentation sourceText;

    /**
     * Creates a {@link NamedEntity} with the given name, type, alternative names, and occurrences.
     * <p>{@link NamedEntity#sourceText} is initially {@code null} and can be set via {@link #setSourceText(SoftwareArchitectureDocumentation)}.</p>
     *
     * @param name             the entity name
     * @param type             the entity type
     * @param alternativeNames alternative names for the entity
     * @param occurrences      occurrences of the entity
     */
    @JsonCreator
    private NamedEntity(@JsonProperty("name") String name, @JsonProperty("type") NamedEntityType type, @JsonProperty("alternativeNames") List<String> alternativeNames, @JsonProperty("occurrences") List<Occurrence> occurrences) {
        this.name = name;
        this.type = type;
        this.alternativeNames = new HashSet<>(alternativeNames);
        this.occurrences = new HashSet<>(occurrences);
    }

    @Nullable
    public SoftwareArchitectureDocumentation getSourceText() {
        return sourceText;
    }

    public void setSourceText(@NotNull SoftwareArchitectureDocumentation sourceText) {
        this.sourceText = sourceText;
    }

    public String getName() {
        return name;
    }

    public NamedEntityType getType() {
        return type;
    }

    public Set<String> getAlternativeNames() {
        return alternativeNames;
    }

    public void addAlternativeName(String alternativeName) {
        this.alternativeNames.add(alternativeName);
    }

    public String getOccurrencesAsString() {
        StringBuilder sb = new StringBuilder();
        for (Occurrence occurrence : occurrences) {
            if (!sb.isEmpty()) {
                sb.append(", ");
            }
            sb.append(occurrence.toString());
        }
        return sb.toString();
    }

    public void addOccurrence(int sentenceNumber, NamedEntityReferenceType referenceType) {
        this.occurrences.add(new Occurrence(sentenceNumber, referenceType));
    }

    @Override
    public boolean equals(Object o) {
        //Todo 1 evtl anpassen dass man nur checkt: name, type, occurences:sentenceNumbers (wegen goldstandards...) => oder das als weak equals oder so verwenden - dann auch analog für hash machen (& SAD mit rein oder nicht?)
        if (o == null || getClass() != o.getClass()) return false;
        NamedEntity that = (NamedEntity) o;
        return Objects.equals(getName(), that.getName()) && getType() == that.getType() && Objects.equals(getAlternativeNames(), that.getAlternativeNames()) && Objects.equals(occurrences, that.occurrences);
    }

    @Override
    public int hashCode() {
        return Objects.hash(getName(), getType(), getAlternativeNames(), occurrences);
    }

    @Override
    public String toString() {
        return "model.NamedEntity{" + "name='" + name + '\'' + ", type=" + type + ", alternativeNames=" + alternativeNames + ", occurrences=" + occurrences + "}\n";
    }

    /**
     * Represents an occurrence of a {@link NamedEntity} in its {@link #sourceText}.
     * (needed for JSON mapping)
     *
     * @param sentenceNumber starting at {@code 1}
     * @param referenceType  type of how the entity is referenced
     */
    private record Occurrence(int sentenceNumber, NamedEntityReferenceType referenceType) {
        @JsonCreator
        private Occurrence(@JsonProperty("line") int sentenceNumber, @JsonProperty("referenceType") NamedEntityReferenceType referenceType) {
            this.sentenceNumber = sentenceNumber;
            this.referenceType = referenceType;
        }

        @NotNull
        @Override
        public String toString() {
            return '{' + sentenceNumber + ":" + referenceType;
        }
    }
}
