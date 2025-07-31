/* Licensed under MIT 2025. */
package edu.kit.kastel.mcse.ardoco.core.api.stage.connectiongenerator.ner;

import java.io.Serial;

import edu.kit.kastel.mcse.ardoco.core.api.entity.ArchitectureEntity;
import edu.kit.kastel.mcse.ardoco.core.api.entity.CodeEntity;
import edu.kit.kastel.mcse.ardoco.core.api.entity.ModelEntity;
import edu.kit.kastel.mcse.ardoco.core.api.tracelink.TraceLink;
import edu.kit.kastel.mcse.ardoco.core.common.AggregationFunctions;
import edu.kit.kastel.mcse.ardoco.core.data.Confidence;
import edu.kit.kastel.mcse.ardoco.core.pipeline.agent.Claimant;

public final class NamedArchitectureEntityToModelTraceLink extends TraceLink<NamedArchitectureEntityOccurrence, ModelEntity> {

    @Serial
    private static final long serialVersionUID = 6354707742249919076L;
    private final Confidence confidence;

    /**
     * Create a new instance link.
     *
     * @param namedArchitectureEntityOccurrence the recommended instance
     * @param entity                            the model instance
     */
    public NamedArchitectureEntityToModelTraceLink(NamedArchitectureEntityOccurrence namedArchitectureEntityOccurrence, ModelEntity entity) {
        super(namedArchitectureEntityOccurrence, entity);
        this.confidence = new Confidence(AggregationFunctions.AVERAGE);
    }

    /**
     * Creates a new instance link with a claimant and probability.
     *
     * @param namedArchitectureEntityOccurrence the recommended instance
     * @param entity                            the model instance
     * @param claimant                          the claimant
     * @param probability                       the probability of this link
     */
    public NamedArchitectureEntityToModelTraceLink(NamedArchitectureEntityOccurrence namedArchitectureEntityOccurrence, ModelEntity entity, Claimant claimant,
            double probability) {
        this(namedArchitectureEntityOccurrence, entity);
        this.confidence.addAgentConfidence(claimant, probability);
    }

    /**
     * Returns the probability of the correctness of this link.
     *
     * @return the probability of this link
     */
    public double getConfidence() {
        return this.confidence.getConfidence();
    }

    @Override
    public String toString() {
        NamedArchitectureEntityOccurrence namedArchitectureEntityOccurrence = this.getFirstEndpoint();
        ModelEntity modelEntity = this.getSecondEndpoint();

        String typeInfo;
        switch (this.getSecondEndpoint()) {
            case ArchitectureEntity architectureEntity -> typeInfo = architectureEntity.getType().orElseThrow();
            case CodeEntity ignored -> typeInfo = "";
        }

        // TODO
        return "NamedArchitectureEntityToModelTraceLink [ uid=" + modelEntity.getId() + ", name=" + modelEntity.getName() + //
                ", as=" + String.join(", ", typeInfo) + ", probability=" + this.getConfidence() + ", FOUND: " + //
                namedArchitectureEntityOccurrence.getName() + " (" + namedArchitectureEntityOccurrence.getSentenceNumber() + ")]";
    }

    @Override
    public boolean equals(Object obj) {
        // Confidence is not part of the equals check, as it is not relevant for the identity of the trace link
        return super.equals(obj);
    }

    @Override
    public int hashCode() {
        // Confidence is not part of the hash code, as it is not relevant for the identity of the trace link
        return super.hashCode();
    }
}
