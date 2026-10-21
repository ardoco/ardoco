package edu.kit.kastel.mcse.ardoco.id.states;

import edu.kit.kastel.mcse.ardoco.core.architecture.Deterministic;

import java.util.Locale;
import java.util.Objects;

@Deterministic
public record ArchitecturalStylePatternStatement(int sentenceNumber, String architecturalStylePatternName, String normalizedArchitecturalStylePatternName,
                                                 String sentenceText) implements Comparable<ArchitecturalStylePatternStatement> {

    public ArchitecturalStylePatternStatement(int sentenceNumber, String architecturalStylePatternName, String sentenceText) {
        this(sentenceNumber, architecturalStylePatternName, normalize(architecturalStylePatternName), sentenceText);
    }

    public ArchitecturalStylePatternStatement {
        if (sentenceNumber < 1) {
            throw new IllegalArgumentException("sentenceNumber must be >= 1");
        }
        Objects.requireNonNull(architecturalStylePatternName, "architecturalStylePatternName must not be null");
        Objects.requireNonNull(normalizedArchitecturalStylePatternName, "normalizedArchitecturalStylePatternName must not be null");
        Objects.requireNonNull(sentenceText, "sentenceText must not be null");
    }

    private static String normalize(String name) {
        Objects.requireNonNull(name, "name must not be null");

        String normalized = name.trim().toLowerCase(Locale.ROOT);
        normalized = normalized.replace("_", "-");
        normalized = normalized.replaceAll("\\s+", " ");

        return switch (normalized) {
            case "model-view-controller", "model view controller", "mvc pattern", "model-view-controller pattern" -> "mvc";
            case "layered architecture", "layered architectural style", "layers", "layer architecture", "n-tier", "n tier" -> "layered architecture";
            case "microservice", "microservices architecture", "microservice architecture" -> "microservices";
            case "client server", "client-server architecture", "client server architecture" -> "client-server";
            case "pipe and filter", "pipes and filters", "pipe-and-filter architecture" -> "pipe-and-filter";
            case "event driven architecture", "event-driven", "event driven" -> "event-driven architecture";
            case "ports and adapters", "ports-and-adapters", "hexagonal" -> "hexagonal architecture";
            default -> normalized;
        };
    }

    public String getArchitecturalStylePatternDisplayName() {
        return normalizedArchitecturalStylePatternName;
    }

    @Override
    public int compareTo(ArchitecturalStylePatternStatement other) {
        int sentenceComparison = Integer.compare(sentenceNumber, other.sentenceNumber);
        if (sentenceComparison != 0) {
            return sentenceComparison;
        }

        int nameComparison = normalizedArchitecturalStylePatternName.compareTo(other.normalizedArchitecturalStylePatternName);
        if (nameComparison != 0) {
            return nameComparison;
        }

        return architecturalStylePatternName.compareTo(other.architecturalStylePatternName);
    }

    @Override
    public String toString() {
        return sentenceNumber + " -> " + getArchitecturalStylePatternDisplayName();
    }
}
