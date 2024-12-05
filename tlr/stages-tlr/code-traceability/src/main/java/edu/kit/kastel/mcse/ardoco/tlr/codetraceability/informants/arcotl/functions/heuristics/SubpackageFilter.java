/* Licensed under MIT 2023-2024. */
package edu.kit.kastel.mcse.ardoco.tlr.codetraceability.informants.arcotl.functions.heuristics;

import java.util.List;
import java.util.SortedSet;

import edu.kit.kastel.mcse.ardoco.core.api.entity.Entity;
import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.architecture.ArchitectureComponent;
import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.architecture.ArchitectureInterface;
import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.architecture.ArchitectureItem;
import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.code.CodeCompilationUnit;
import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.code.CodePackage;
import edu.kit.kastel.mcse.ardoco.core.common.tuple.Pair;
import edu.kit.kastel.mcse.ardoco.tlr.codetraceability.informants.arcotl.NameComparisonUtils;
import edu.kit.kastel.mcse.ardoco.tlr.codetraceability.informants.arcotl.computation.Confidence;

public class SubpackageFilter extends DependentHeuristic {

    @Override
    protected Confidence calculateConfidence(ArchitectureComponent archComponent, CodeCompilationUnit compUnit) {
        return this.calculateSubpackageFilter(archComponent, compUnit);
    }

    @Override
    protected Confidence calculateConfidence(ArchitectureInterface archInterface, CodeCompilationUnit compUnit) {
        if (!archInterface.getSignatures().isEmpty()) {
            return new Confidence();
        }
        return this.calculateSubpackageFilter(archInterface, compUnit);
    }

    private Confidence calculateSubpackageFilter(ArchitectureItem archEndpoint, CodeCompilationUnit compUnit) {
        Pair<ArchitectureItem, CodeCompilationUnit> thisTuple = new Pair<>(archEndpoint, compUnit);
        if (!this.getNodeResult().getConfidence(thisTuple).hasValue()) {
            return new Confidence();
        }
        List<CodePackage> thisPackages = NameComparisonUtils.getMatchedPackages(archEndpoint, compUnit);
        SortedSet<Entity> linkedArchitectureEndpoints = this.getNodeResult().getLinkedEndpoints(compUnit);
        linkedArchitectureEndpoints.remove(archEndpoint);
        for (var linkedArchitectureEndpoint : linkedArchitectureEndpoints) {
            List<CodePackage> otherPackages = NameComparisonUtils.getMatchedPackages(linkedArchitectureEndpoint, compUnit);
            if (thisPackages.isEmpty() || otherPackages.isEmpty()) {
                return new Confidence();
            }
            List<CodePackage> parentPackages = NameComparisonUtils.getPackageList(thisPackages.getFirst());
            parentPackages.remove(thisPackages.getFirst());
            if (parentPackages.contains(otherPackages.getLast())) {
                return new Confidence(1.0);
            }
        }
        return new Confidence();
    }

    @Override
    public String toString() {
        return "SubpackageFilter";
    }
}
