/* Licensed under MIT 2023. */
package edu.kit.kastel.mcse.ardoco.tlr.codetraceability.informants.arcotl.functions.heuristics;

import java.util.Objects;

import edu.kit.kastel.mcse.ardoco.core.api.entity.Entity;
import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.architecture.ArchitectureComponent;
import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.architecture.ArchitectureInterface;
import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.architecture.ArchitectureItem;
import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.code.CodeCompilationUnit;
import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.code.CodeItem;
import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.code.InterfaceUnit;
import edu.kit.kastel.mcse.ardoco.tlr.codetraceability.informants.arcotl.NameComparisonUtils;
import edu.kit.kastel.mcse.ardoco.tlr.codetraceability.informants.arcotl.computation.Confidence;

public class ComponentNameResemblance extends StandaloneHeuristic {

    private final NameConfig nameConfig;
    private final NameComparisonUtils.PreprocessingMethod preprocessConfig;

    public enum NameConfig {
        INTERFACE, COMPONENT, COMPONENT_WITHOUT_PACKAGE
    }

    public ComponentNameResemblance(NameConfig nameConfig, NameComparisonUtils.PreprocessingMethod preprocessConfig) {
        this.nameConfig = nameConfig;
        this.preprocessConfig = preprocessConfig;
    }

    @Override
    protected Confidence calculateConfidence(ArchitectureComponent archComponent, CodeCompilationUnit compUnit) {
        if (nameConfig.equals(NameConfig.INTERFACE)) {
            return new Confidence();
        }
        return calculateNameResemblance(archComponent, compUnit);
    }

    @Override
    protected Confidence calculateConfidence(ArchitectureInterface archInterface, CodeCompilationUnit compUnit) {
        if (nameConfig.equals(NameConfig.INTERFACE) == archInterface.getSignatures().isEmpty()) {
            return new Confidence();
        }
        return calculateNameResemblance(archInterface, compUnit);
    }

    private Confidence calculateNameResemblance(ArchitectureItem archEndpoint, CodeCompilationUnit compUnit) {
        if (nameConfig.equals(NameConfig.COMPONENT_WITHOUT_PACKAGE) && compUnit.hasParent()) {
            return new Confidence();
        }
        Confidence maxConfidence = new Confidence();
        for (var codeEntity : compUnit.getAllDataTypesAndSelf()) {
            Confidence singleConfidence = calculateNameResemblanceSingle(archEndpoint, codeEntity);
            if (singleConfidence.compareTo(maxConfidence) > 0) {
                maxConfidence = singleConfidence;
            }
        }
        return maxConfidence;
    }

    private Confidence calculateNameResemblanceSingle(ArchitectureItem archEndpoint, CodeItem codeItem) {
        boolean areSimilar = NameComparisonUtils.isContained(archEndpoint, codeItem, preprocessConfig);
        if (areSimilar) {
            if (nameConfig.equals(NameConfig.INTERFACE) && codeItem instanceof InterfaceUnit) {
                return new Confidence(1.0);
            }
            double similarity = NameComparisonUtils.getRatio(archEndpoint, codeItem);
            return new Confidence(similarity);
        }
        if (nameConfig.equals(NameConfig.INTERFACE)) {
            return calculateNameResemblanceFallback(archEndpoint, codeItem);
        }
        return new Confidence();
    }

    private Confidence calculateNameResemblanceFallback(ArchitectureItem archInterface, Entity codeEndpoint) {
        boolean areSimilar = NameComparisonUtils.isInterfaceContained(archInterface, codeEndpoint, preprocessConfig);
        if (areSimilar) {
            double similarity = NameComparisonUtils.getInterfaceRatio(archInterface, codeEndpoint);
            return new Confidence(similarity);
        }
        return new Confidence();
    }

    @Override
    public int hashCode() {
        return Objects.hash(getClass(), nameConfig, preprocessConfig);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        ComponentNameResemblance other = (ComponentNameResemblance) obj;
        return Objects.equals(nameConfig, other.nameConfig) && Objects.equals(preprocessConfig, other.preprocessConfig);
    }

    @Override
    public String toString() {
        return "ComponentNameResemblance-" + nameConfig.toString().toLowerCase() + "-" + preprocessConfig.toString().toLowerCase();
    }
}
