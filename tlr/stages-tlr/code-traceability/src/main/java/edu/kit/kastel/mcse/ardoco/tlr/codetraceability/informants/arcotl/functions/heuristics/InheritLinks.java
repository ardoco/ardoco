/* Licensed under MIT 2023-2026. */
package edu.kit.kastel.mcse.ardoco.tlr.codetraceability.informants.arcotl.functions.heuristics;

import java.util.Objects;
import java.util.SortedSet;

import edu.kit.kastel.mcse.ardoco.core.api.models.architecture.ArchitectureComponent;
import edu.kit.kastel.mcse.ardoco.core.api.models.architecture.ArchitectureInterface;
import edu.kit.kastel.mcse.ardoco.core.api.models.architecture.ArchitectureItem;
import edu.kit.kastel.mcse.ardoco.core.api.models.code.CodeCompilationUnit;
import edu.kit.kastel.mcse.ardoco.core.api.models.code.Datatype;
import edu.kit.kastel.mcse.ardoco.core.common.tuple.Pair;
import edu.kit.kastel.mcse.ardoco.tlr.codetraceability.informants.arcotl.computation.Confidence;

public class InheritLinks extends DependentHeuristic {

    @Override
    protected Confidence calculateConfidence(ArchitectureComponent archComponent, CodeCompilationUnit compUnit) {
        return this.inheritLinks(archComponent, compUnit);
    }

    @Override
    protected Confidence calculateConfidence(ArchitectureInterface archInterface, CodeCompilationUnit compUnit) {
        if (!archInterface.getMethodSignatures().isEmpty()) {
            return new Confidence();
        }
        return this.inheritLinks(archInterface, compUnit);
    }

    private Confidence inheritLinks(ArchitectureItem archEndpoint, CodeCompilationUnit compUnit) {
        if (!this.getNodeResult().getLinkedEndpoints(compUnit).isEmpty()) {
            return new Confidence();
        }
        Confidence maxConfidence = new Confidence();
        for (Datatype codeType : compUnit.getAllDataTypes()) {
            Confidence extendedConfidence = this.inheritLinks(archEndpoint, codeType);
            if (extendedConfidence.compareTo(maxConfidence) > 0) {
                maxConfidence = extendedConfidence;
            }
        }
        return maxConfidence;
    }

    private Confidence inheritLinks(ArchitectureItem archEndpoint, Datatype codeType) {
        SortedSet<Datatype> extendedTypes = MethodResemblance.getAllExtendedTypes(codeType);
        MethodResemblance.getAllImplementedInterfaces(codeType).forEach(i -> extendedTypes.addAll(MethodResemblance.getAllExtendedTypes(i)));

        Confidence maxConfidence = new Confidence();
        for (Datatype extendedType : extendedTypes) {
            var codeTypeUnit = Objects.requireNonNull(codeType.getCompilationUnit(), "Datatype without compilation unit");
            var extendedTypeUnit = Objects.requireNonNull(extendedType.getCompilationUnit(), "Datatype without compilation unit");
            if (areInDifferentPackages(codeTypeUnit, extendedTypeUnit)) {
                continue;
            }
            Confidence extendedConfidence = this.getRequiredConfidence(new Pair<>(archEndpoint, extendedTypeUnit));
            if (extendedConfidence.compareTo(maxConfidence) > 0) {
                maxConfidence = extendedConfidence;
            }
        }
        return maxConfidence;
    }

    public static boolean areInDifferentPackages(CodeCompilationUnit fileA, CodeCompilationUnit fileB) {
        var parentA = fileA.getParent();
        var parentB = fileB.getParent();
        if (parentA == null || parentB == null) {
            return parentA != parentB;
        }
        return !parentA.equals(parentB);
    }

    @Override
    public String toString() {
        return "InheritExtendedLinks";
    }
}
