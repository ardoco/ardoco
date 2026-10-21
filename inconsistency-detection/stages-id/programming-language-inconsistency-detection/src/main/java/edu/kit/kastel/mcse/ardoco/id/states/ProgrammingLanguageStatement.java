package edu.kit.kastel.mcse.ardoco.id.states;

import java.util.Arrays;
import java.util.Locale;
import java.util.Objects;

import edu.kit.kastel.mcse.ardoco.core.api.models.code.ProgrammingLanguage;
import edu.kit.kastel.mcse.ardoco.core.architecture.Deterministic;

@Deterministic
public record ProgrammingLanguageStatement(int sentenceNumber, ProgrammingLanguage programmingLanguage, String originalProgrammingLanguageName,
                                           String sentenceText) implements Comparable<ProgrammingLanguageStatement> {

    public ProgrammingLanguageStatement(int sentenceNumber, String programmingLanguageName, String sentenceText) {
        this(sentenceNumber, resolveProgrammingLanguage(programmingLanguageName), programmingLanguageName, sentenceText);
    }

    public ProgrammingLanguageStatement {
        if (sentenceNumber < 1) {
            throw new IllegalArgumentException("sentenceNumber must be >= 1");
        }
        Objects.requireNonNull(programmingLanguage, "programmingLanguage must not be null");
        Objects.requireNonNull(originalProgrammingLanguageName, "originalProgrammingLanguageName must not be null");
        Objects.requireNonNull(sentenceText, "sentenceText must not be null");
    }

    private static ProgrammingLanguage resolveProgrammingLanguage(String programmingLanguageName) {
        Objects.requireNonNull(programmingLanguageName, "programmingLanguageName must not be null");

        String normalizedName = programmingLanguageName.trim().toLowerCase(Locale.ROOT);

        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
        return Arrays.stream(ProgrammingLanguage.values())
                .filter(language -> language != ProgrammingLanguage.OTHER)
                .filter(language -> language.name().toLowerCase(Locale.ROOT).equals(normalizedName))
                .findFirst()
                .orElse(Arrays.stream(ProgrammingLanguage.values())
                        .filter(language -> language != ProgrammingLanguage.OTHER)
                        .filter(language -> language.getExtensions().contains(normalizedName))
                        .findFirst()
                        .orElse(ProgrammingLanguage.OTHER));
    }

    public String getProgrammingLanguageDisplayName() {
        if (programmingLanguage == ProgrammingLanguage.OTHER) {
            return originalProgrammingLanguageName.toLowerCase(Locale.ROOT);
        }
        return programmingLanguage.name().toLowerCase(Locale.ROOT);
    }

    @Override
    public int compareTo(ProgrammingLanguageStatement other) {
        int sentenceComparison = Integer.compare(sentenceNumber, other.sentenceNumber);
        if (sentenceComparison != 0) {
            return sentenceComparison;
        }

        int languageComparison = programmingLanguage.compareTo(other.programmingLanguage);
        if (languageComparison != 0) {
            return languageComparison;
        }

        return originalProgrammingLanguageName.compareTo(other.originalProgrammingLanguageName);
    }

    @Override
    public String toString() {
        return sentenceNumber + " -> " + getProgrammingLanguageDisplayName();
    }
}
