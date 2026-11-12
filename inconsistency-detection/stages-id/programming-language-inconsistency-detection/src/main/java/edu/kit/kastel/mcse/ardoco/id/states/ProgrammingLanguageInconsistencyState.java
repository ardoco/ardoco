/* Licensed under MIT 2026. */
package edu.kit.kastel.mcse.ardoco.id.states;

import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.list.MutableList;

import edu.kit.kastel.mcse.ardoco.core.data.PipelineStepData;

public interface ProgrammingLanguageInconsistencyState extends PipelineStepData {

    String ID = "ProgrammingLanguageInconsistencyState";

    boolean addValidatedStatements(MutableList<ProgrammingLanguageStatement> statements);

    boolean addInconsistencies(MutableList<ProgrammingLanguageInconsistency> inconsistencies);

    ImmutableList<ProgrammingLanguageStatement> getValidatedStatements();

    ImmutableList<ProgrammingLanguageInconsistency> getInconsistencies();
}
