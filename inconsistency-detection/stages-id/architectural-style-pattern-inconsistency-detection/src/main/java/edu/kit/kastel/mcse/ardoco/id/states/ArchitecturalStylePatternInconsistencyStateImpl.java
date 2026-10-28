/* Licensed under MIT 2026. */
package edu.kit.kastel.mcse.ardoco.id.states;

import java.util.SortedSet;
import java.util.TreeSet;

import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.list.MutableList;

public class ArchitecturalStylePatternInconsistencyStateImpl implements ArchitecturalStylePatternInconsistencyState {

    private final SortedSet<ArchitecturalStylePatternStatement> validatedStatements = new TreeSet<>();
    private final SortedSet<ArchitecturalStylePatternInconsistency> inconsistencies = new TreeSet<>();

    @Override
    public boolean addValidatedStatements(MutableList<ArchitecturalStylePatternStatement> statements) {
        return validatedStatements.addAll(statements);
    }

    @Override
    public boolean addInconsistencies(MutableList<ArchitecturalStylePatternInconsistency> inconsistencies) {
        return this.inconsistencies.addAll(inconsistencies);
    }

    @Override
    public ImmutableList<ArchitecturalStylePatternStatement> getValidatedStatements() {
        return Lists.immutable.ofAll(validatedStatements);
    }

    @Override
    public ImmutableList<ArchitecturalStylePatternInconsistency> getInconsistencies() {
        return Lists.immutable.ofAll(inconsistencies);
    }
}
