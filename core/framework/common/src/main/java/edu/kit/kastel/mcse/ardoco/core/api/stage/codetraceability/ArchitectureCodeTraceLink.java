/* Licensed under MIT 2025. */
package edu.kit.kastel.mcse.ardoco.core.api.stage.codetraceability;

import java.io.Serial;

import edu.kit.kastel.mcse.ardoco.core.api.models.architecture.ArchitectureItem;
import edu.kit.kastel.mcse.ardoco.core.api.models.code.CodeItem;
import edu.kit.kastel.mcse.ardoco.core.api.tracelink.TraceLink;

/**
 * Trace link between an architecture item and a code item.
 */
public final class ArchitectureCodeTraceLink extends TraceLink<ArchitectureItem, CodeItem> {
    @Serial
    private static final long serialVersionUID = 7583961097321596737L;

    /**
     * Creates a new model-code trace link.
     *
     * @param firstEndpoint  the architecture item
     * @param secondEndpoint the code item
     */
    public ArchitectureCodeTraceLink(ArchitectureItem firstEndpoint, CodeItem secondEndpoint) {
        super(firstEndpoint, secondEndpoint);
    }
}
