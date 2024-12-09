/* Licensed under MIT 2021-2024. */
package edu.kit.kastel.mcse.ardoco.core.api.stage.connectiongenerator;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.MutableList;

import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.architecture.legacy.ModelInstance;
import edu.kit.kastel.mcse.ardoco.core.api.stage.recommendationgenerator.RecommendedInstance;
import edu.kit.kastel.mcse.ardoco.core.api.stage.textextraction.NounMapping;
import edu.kit.kastel.mcse.ardoco.core.api.tracelink.TraceLink;
import edu.kit.kastel.mcse.ardoco.core.architecture.Deterministic;
import edu.kit.kastel.mcse.ardoco.core.common.AggregationFunctions;
import edu.kit.kastel.mcse.ardoco.core.data.Confidence;
import edu.kit.kastel.mcse.ardoco.core.pipeline.agent.Claimant;

/**
 * An InstanceLink defines a link between an {@link RecommendedInstance} and an {@link ModelInstance}.
 */
@Deterministic
public class InstanceLink extends TraceLink<RecommendedInstance, ModelInstance> {

    private static final long serialVersionUID = -8630933950725516269L;
    private final Confidence confidence;

    /**
     * Create a new instance link
     *
     * @param textualInstance the recommended instance
     * @param modelInstance   the model instance
     */
    public InstanceLink(RecommendedInstance textualInstance, ModelInstance modelInstance) {
        super(textualInstance, modelInstance);
        this.confidence = new Confidence(AggregationFunctions.AVERAGE);
    }

    /**
     * Creates a new instance link.
     *
     * @param textualInstance the recommended instance
     * @param modelInstance   the model instance
     * @param claimant        the claimant
     * @param probability     the probability of this link
     */
    public InstanceLink(RecommendedInstance textualInstance, ModelInstance modelInstance, Claimant claimant, double probability) {
        this(textualInstance, modelInstance);
        this.confidence.addAgentConfidence(claimant, probability);
    }

    /**
     * Add confidence to this link.
     *
     * @param claimant   the claimant that wants to change the confidence
     * @param confidence the confidence value to add
     */
    public final void addConfidence(Claimant claimant, double confidence) {
        this.confidence.addAgentConfidence(claimant, confidence);
    }

    /**
     * Returns the probability of the correctness of this link.
     *
     * @return the probability of this link
     */
    public final double getConfidence() {
        return this.confidence.getConfidence();
    }

    @Override
    public String toString() {
        Set<String> names = new LinkedHashSet<>();
        MutableList<Integer> namePositions = Lists.mutable.empty();
        Set<String> types = new LinkedHashSet<>();
        MutableList<Integer> typePositions = Lists.mutable.empty();

        for (NounMapping nameMapping : this.getFirstEndpoint().getNameMappings()) {
            names.addAll(nameMapping.getSurfaceForms().castToCollection());
            namePositions.addAll(nameMapping.getMappingSentenceNo().castToCollection());
        }
        for (NounMapping typeMapping : this.getFirstEndpoint().getTypeMappings()) {
            types.addAll(typeMapping.getSurfaceForms().castToCollection());
            typePositions.addAll(typeMapping.getMappingSentenceNo().castToCollection());
        }
        return "InstanceMapping [ uid=" + this.getSecondEndpoint().getUid() + ", name=" + this.getSecondEndpoint().getFullName() + //
                ", as=" + String.join(", ", this.getSecondEndpoint().getFullType()) + ", probability=" + this.getConfidence() + ", FOUND: " + //
                this.getFirstEndpoint().getName() + " : " + this.getFirstEndpoint().getType() + ", occurrences= " + //
                "NameVariants: " + names.size() + ": " + names + " sentences{" + Arrays.toString(namePositions.toArray()) + "}" + //
                ", TypeVariants: " + types.size() + ": " + types + "sentences{" + Arrays.toString(typePositions.toArray()) + "}" + "]";
    }

}
