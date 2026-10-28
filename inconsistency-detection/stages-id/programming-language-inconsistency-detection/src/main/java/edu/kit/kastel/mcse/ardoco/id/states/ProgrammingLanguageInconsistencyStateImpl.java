/* Licensed under MIT 2026. */
package edu.kit.kastel.mcse.ardoco.id.states;

import java.util.SortedSet;
import java.util.TreeSet;

import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.list.MutableList;

public class ProgrammingLanguageInconsistencyStateImpl implements ProgrammingLanguageInconsistencyState {

    private final SortedSet<ProgrammingLanguageStatement> validatedStatements = new TreeSet<>();
    private final SortedSet<ProgrammingLanguageInconsistency> inconsistencies = new TreeSet<>();

    @Override
    public boolean addValidatedStatements(MutableList<ProgrammingLanguageStatement> statements) {
        return validatedStatements.addAll(statements);
    }

    @Override
    public boolean addInconsistencies(MutableList<ProgrammingLanguageInconsistency> inconsistencies) {
        return this.inconsistencies.addAll(inconsistencies);
    }

    @Override
    public ImmutableList<ProgrammingLanguageStatement> getValidatedStatements() {
        return Lists.immutable.ofAll(validatedStatements);
    }

    @Override
    public ImmutableList<ProgrammingLanguageInconsistency> getInconsistencies() {
        return Lists.immutable.ofAll(inconsistencies);
    }
}
