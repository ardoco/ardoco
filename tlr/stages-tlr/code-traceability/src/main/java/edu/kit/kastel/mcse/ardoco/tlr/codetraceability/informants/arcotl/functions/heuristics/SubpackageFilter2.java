/* Licensed under MIT 2023. */
package edu.kit.kastel.mcse.ardoco.tlr.codetraceability.informants.arcotl.functions.heuristics;

import java.util.SortedSet;

import edu.kit.kastel.mcse.ardoco.core.api.entity.Entity;
import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.architecture.ArchitectureComponent;
import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.architecture.ArchitectureInterface;
import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.architecture.ArchitectureItem;
import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.code.CodeCompilationUnit;
import edu.kit.kastel.mcse.ardoco.core.common.tuple.Pair;
import edu.kit.kastel.mcse.ardoco.tlr.codetraceability.informants.arcotl.computation.Confidence;

public class SubpackageFilter2 extends DependentHeuristic {

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
        int i = 0;
        SortedSet<Entity> linkedArchEndpoints = this.getNodeResult().getLinkedEndpoints(compUnit);
        for (var linkedArchEndpoint : linkedArchEndpoints) {
            if (linkedArchEndpoint instanceof ArchitectureComponent) {
                i++;
            }
            if (i > 1) {
                return new Confidence(1.0);
            }
        }
        return new Confidence();
    }

    @Override
    public String toString() {
        return "SubpackageFilter2";
    }
}
