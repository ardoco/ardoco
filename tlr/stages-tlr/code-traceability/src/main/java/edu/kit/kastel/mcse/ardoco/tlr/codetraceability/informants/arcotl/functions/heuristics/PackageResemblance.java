/* Licensed under MIT 2023. */
package edu.kit.kastel.mcse.ardoco.tlr.codetraceability.informants.arcotl.functions.heuristics;

import java.util.List;
import java.util.Objects;

import edu.kit.kastel.mcse.ardoco.core.api.entity.Entity;
import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.architecture.ArchitectureComponent;
import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.architecture.ArchitectureInterface;
import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.code.CodeCompilationUnit;
import edu.kit.kastel.mcse.ardoco.tlr.codetraceability.informants.arcotl.NameComparisonUtils;
import edu.kit.kastel.mcse.ardoco.tlr.codetraceability.informants.arcotl.computation.Confidence;

public class PackageResemblance extends StandaloneHeuristic {

    private NameComparisonUtils.PreprocessingMethod config;

    public PackageResemblance(NameComparisonUtils.PreprocessingMethod config) {
        this.config = config;
    }

    @Override
    protected Confidence calculateConfidence(ArchitectureComponent archComponent, CodeCompilationUnit compUnit) {
        return calculatePackageResemblance(archComponent, compUnit);
    }

    @Override
    protected Confidence calculateConfidence(ArchitectureInterface archInterface, CodeCompilationUnit compUnit) {
        if (!archInterface.getSignatures().isEmpty()) {
            return new Confidence();
        }
        return calculatePackageResemblance(archInterface, compUnit);
    }

    private Confidence calculatePackageResemblance(Entity archEndpoint, CodeCompilationUnit compUnit) {
        if (!compUnit.hasParent()) {
            return new Confidence();
        }

        List<String> p = compUnit.getParentPackageNames();
        double similarity = NameComparisonUtils.getContainedRatio(archEndpoint, p, config);
        if (similarity == 0) {
            return new Confidence();
        }
        return new Confidence(similarity);
    }

    @Override
    public int hashCode() {
        return Objects.hash(getClass(), config);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        PackageResemblance other = (PackageResemblance) obj;
        return Objects.equals(config, other.config);
    }

    @Override
    public String toString() {
        return "PackageResemblance-" + config.toString().toLowerCase();
    }
}
