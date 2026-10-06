package edu.kit.kastel.mcse.ardoco.id.states;

import java.util.Objects;

public record ProgrammingLanguageInconsistency(ProgrammingLanguageStatement statement, String explanation)
        implements Comparable<ProgrammingLanguageInconsistency> {

    public ProgrammingLanguageInconsistency {
        Objects.requireNonNull(statement, "statement must not be null");
        Objects.requireNonNull(explanation, "explanation must not be null");
    }

    @Override
    public int compareTo(ProgrammingLanguageInconsistency other) {
        int statementComparison = statement.compareTo(other.statement);
        if (statementComparison != 0) {
            return statementComparison;
        }
        return explanation.compareTo(other.explanation);
    }

    @Override
    public String toString() {
        return statement + " :: " + explanation;
    }
}
