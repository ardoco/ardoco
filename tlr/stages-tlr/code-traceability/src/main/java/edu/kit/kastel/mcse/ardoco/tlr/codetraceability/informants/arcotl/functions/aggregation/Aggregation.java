/* Licensed under MIT 2023. */
package edu.kit.kastel.mcse.ardoco.tlr.codetraceability.informants.arcotl.functions.aggregation;

import java.util.List;
import java.util.Objects;

import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.ArchitectureModel;
import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.CodeModel;
import edu.kit.kastel.mcse.ardoco.tlr.codetraceability.informants.arcotl.computation.NodeResult;

/**
 * An aggregation function. Aggregations are immutable.
 */
public abstract class Aggregation {

    public abstract NodeResult calculateConfidences(ArchitectureModel archModel, CodeModel codeModel, List<NodeResult> childrenResults);

    @Override
    public int hashCode() {
        return Objects.hash(getClass());
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        return (obj instanceof Aggregation);
    }
}
