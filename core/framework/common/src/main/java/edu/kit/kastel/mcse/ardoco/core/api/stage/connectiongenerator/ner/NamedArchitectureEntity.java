/* Licensed under MIT 2025. */
package edu.kit.kastel.mcse.ardoco.core.api.stage.connectiongenerator.ner;

import java.util.List;
import java.util.Objects;
import java.util.SortedSet;

import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.list.MutableList;

import edu.kit.kastel.mcse.ardoco.core.common.util.CommonUtilities;

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
        return "NamedArchitectureEntity{" + "name='" + name + '\'' + ", alternativeNames=" + alternativeNames + ", occurrences=" + occurrences.stream()
                .map(NamedArchitectureEntityOccurrence::getSentenceNumber)
                .toList() + '}';
    }

    /**
     * Returns the parts of the primary name.
     *
     * @return the split primary name
     */
    public ImmutableList<String> getNameParts() {
        return splitIdentifierIntoParts(this.getName()).toImmutable();
    }

    /**
     * Returns the parts of the primary and alternative names.
     *
     * @return the distinct split parts of all names
     */
    public ImmutableList<String> getAllNameParts() {
        MutableList<String> allParts = Lists.mutable.empty();

        allParts.addAllIterable(splitIdentifierIntoParts(this.name));

        for (String alternativeName : this.alternativeNames) {
            allParts.addAllIterable(splitIdentifierIntoParts(alternativeName));
        }

        return allParts.distinct().toImmutable();
    }

    private MutableList<String> splitIdentifierIntoParts(String identifier) {
        String splitName = CommonUtilities.splitCases(identifier);
        var names = Lists.mutable.with(splitName.split(" "));
        if (names.size() > 1) {
            names.add(identifier);
        }
        return names;
    }
}
