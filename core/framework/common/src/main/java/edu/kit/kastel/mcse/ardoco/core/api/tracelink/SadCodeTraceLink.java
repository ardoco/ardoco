/* Licensed under MIT 2023-2024. */
package edu.kit.kastel.mcse.ardoco.core.api.tracelink;

import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.code.CodeCompilationUnit;
import edu.kit.kastel.mcse.ardoco.core.api.text.SentenceEntity;

// TODO: Replace with SadModelTraceLink
@Deprecated
public class SadCodeTraceLink extends TraceLink<SentenceEntity, CodeCompilationUnit> {

    private static final long serialVersionUID = -1099702076674008083L;

    public SadCodeTraceLink(SentenceEntity e1, CodeCompilationUnit e2) {
        super(e1, e2);
    }
}
