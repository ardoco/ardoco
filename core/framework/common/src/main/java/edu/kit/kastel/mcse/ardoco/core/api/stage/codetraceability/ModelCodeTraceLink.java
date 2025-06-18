/* Licensed under MIT 2025. */
package edu.kit.kastel.mcse.ardoco.core.api.stage.codetraceability;

import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.architecture.ArchitectureItem;
import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.code.CodeItem;
import edu.kit.kastel.mcse.ardoco.core.api.tracelink.TraceLink;

public class ModelCodeTraceLink extends TraceLink<ArchitectureItem, CodeItem> {

    public ModelCodeTraceLink(ArchitectureItem firstEndpoint, CodeItem secondEndpoint) {
        super(firstEndpoint, secondEndpoint);
    }
}
