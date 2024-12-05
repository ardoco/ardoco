/* Licensed under MIT 2023-2024. */
package edu.kit.kastel.mcse.ardoco.core.api.tracelink;

import java.util.Objects;
import java.util.Optional;

import edu.kit.kastel.mcse.ardoco.core.api.entity.Entity;

public final class TransitiveTraceLink<A extends Entity, M extends Entity, B extends Entity> extends TraceLink<A, B> {

    private static final long serialVersionUID = 3781827633038556211L;

    private final TraceLink<A, M> firstTraceLink;
    private final TraceLink<M, B> secondTraceLink;

    private TransitiveTraceLink(TraceLink<A, M> firstTraceLink, TraceLink<M, B> secondTraceLink) {
        super(firstTraceLink.getFirstEndpoint(), secondTraceLink.getSecondEndpoint());
        this.firstTraceLink = firstTraceLink;
        this.secondTraceLink = secondTraceLink;
    }

    public static <A extends Entity, M extends Entity, B extends Entity> Optional<TransitiveTraceLink<A, M, B>> createTransitiveTraceLink(
            TraceLink<A, M> firstTraceLink, TraceLink<M, B> secondTraceLink) {
        if (TransitiveTraceLink.isValidTransitiveTraceLink(firstTraceLink, secondTraceLink)) {
            return Optional.of(new TransitiveTraceLink<>(firstTraceLink, secondTraceLink));
        }
        return Optional.empty();
    }

    public static boolean isValidTransitiveTraceLink(TraceLink<?, ?> firstTraceLink, TraceLink<?, ?> secondTraceLink) {
        var secondEndpointOfFirstTl = firstTraceLink.getSecondEndpoint().getId();
        var firstEndpointOfSecondTl = secondTraceLink.getFirstEndpoint().getId();
        return secondEndpointOfFirstTl.equals(firstEndpointOfSecondTl);
    }

    public TraceLink<A, M> getFirstTraceLink() {
        return this.firstTraceLink;
    }

    public TraceLink<M, B> getSecondTraceLink() {
        return this.secondTraceLink;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.asPair());
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof TransitiveTraceLink<?, ?, ?> other)) {
            return false;
        }
        return Objects.equals(this.getFirstTraceLink(), other.getFirstTraceLink()) && //
                Objects.equals(this.getSecondTraceLink(), other.getSecondTraceLink()) && //
                Objects.equals(this.asPair(), other.asPair());
    }
}
