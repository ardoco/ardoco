/* Licensed under MIT 2021-2024. */
package edu.kit.kastel.mcse.ardoco.core.api.stage.connectiongenerator;

import java.io.Serializable;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.MutableList;

import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.architecture.legacy.ModelInstance;
import edu.kit.kastel.mcse.ardoco.core.api.stage.recommendationgenerator.RecommendedInstance;
import edu.kit.kastel.mcse.ardoco.core.api.stage.textextraction.NounMapping;
import edu.kit.kastel.mcse.ardoco.core.architecture.Deterministic;
import edu.kit.kastel.mcse.ardoco.core.common.AggregationFunctions;
import edu.kit.kastel.mcse.ardoco.core.data.Confidence;
import edu.kit.kastel.mcse.ardoco.core.pipeline.agent.Claimant;

/**
 * An InstanceLink defines a link between an {@link RecommendedInstance} and an {@link ModelInstance}.
 */
@Deterministic
public class InstanceLink implements Serializable {

    private static final long serialVersionUID = -8630933950725516269L;

    private final RecommendedInstance textualInstance;
    private final ModelInstance modelInstance;
    private final Confidence confidence;

    /**
     * Create a new instance link
     *
     * @param textualInstance the recommended instance
     * @param modelInstance   the model instance
     */
    public InstanceLink(RecommendedInstance textualInstance, ModelInstance modelInstance) {
        this.textualInstance = textualInstance;
        this.modelInstance = modelInstance;
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

    /**
     * Returns the recommended instance.
     *
     * @return the textual instance
     */
    public final RecommendedInstance getTextualInstance() {
        return this.textualInstance;
    }

    /**
     * Returns the model instance.
     *
     * @return the extracted instance
     */
    public final ModelInstance getModelInstance() {
        return this.modelInstance;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.modelInstance, this.textualInstance);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof InstanceLink other)) {
            return false;
        }
        return Objects.equals(this.getModelInstance(), other.getModelInstance()) && Objects.equals(this.getTextualInstance(), other.getTextualInstance());
    }

    @Override
    public String toString() {
        Set<String> names = new LinkedHashSet<>();
        MutableList<Integer> namePositions = Lists.mutable.empty();
        Set<String> types = new LinkedHashSet<>();
        MutableList<Integer> typePositions = Lists.mutable.empty();

        for (NounMapping nameMapping : this.textualInstance.getNameMappings()) {
            names.addAll(nameMapping.getSurfaceForms().castToCollection());
            namePositions.addAll(nameMapping.getMappingSentenceNo().castToCollection());
        }
        for (NounMapping typeMapping : this.textualInstance.getTypeMappings()) {
            types.addAll(typeMapping.getSurfaceForms().castToCollection());
            typePositions.addAll(typeMapping.getMappingSentenceNo().castToCollection());
        }
        return "InstanceMapping [ uid=" + this.modelInstance.getUid() + ", name=" + this.modelInstance.getFullName() + //
                ", as=" + String.join(", ", this.modelInstance.getFullType()) + ", probability=" + this.getConfidence() + ", FOUND: " + //
                this.textualInstance.getName() + " : " + this.textualInstance.getType() + ", occurrences= " + //
                "NameVariants: " + names.size() + ": " + names + " sentences{" + Arrays.toString(namePositions.toArray()) + "}" + //
                ", TypeVariants: " + types.size() + ": " + types + "sentences{" + Arrays.toString(typePositions.toArray()) + "}" + "]";
    }

}
