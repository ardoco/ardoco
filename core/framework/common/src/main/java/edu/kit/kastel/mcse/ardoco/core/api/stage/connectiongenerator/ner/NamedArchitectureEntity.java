/* Licensed under MIT 2025. */
package edu.kit.kastel.mcse.ardoco.core.api.stage.connectiongenerator.ner;

import java.util.List;
import java.util.Objects;
import java.util.SortedSet;

public class NamedArchitectureEntity implements Comparable<NamedArchitectureEntity> {

    private final List<NamedArchitectureEntityOccurrence> occurrences;
    /**
     * alternative names of the entity, e.g., if the name is ambiguous
     */
    private final SortedSet<String> alternativeNames;
    private final String name;

    public NamedArchitectureEntity(String name, SortedSet<String> alternativeNames, List<NamedArchitectureEntityOccurrence> occurrences) {
        this.alternativeNames = alternativeNames;
        this.name = name;
        this.occurrences = occurrences;
    }

    public List<NamedArchitectureEntityOccurrence> getOccurrences() {
        return occurrences;
    }

    public SortedSet<String> getAlternativeNames() {
        return alternativeNames;
    }

    public String getName() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass())
            return false;
        NamedArchitectureEntity that = (NamedArchitectureEntity) o;
        return Objects.equals(alternativeNames, that.alternativeNames) && Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(alternativeNames, name);
    }

    @Override
    public int compareTo(NamedArchitectureEntity o) {
        return this.name.compareTo(o.name);
    }

    @Override
    public String toString() {
        return "NamedArchitectureEntity{" + "name='" + name + '\'' + ", alternativeNames=" + alternativeNames + '}';
    }
}
