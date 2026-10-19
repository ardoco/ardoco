package edu.kit.kastel.mcse.ardoco.id.states;

import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.list.MutableList;

import edu.kit.kastel.mcse.ardoco.core.data.PipelineStepData;

public interface ArchitecturalStylePatternInconsistencyState extends PipelineStepData {

    String ID = "ArchitecturalStylePatternInconsistencyState";

    boolean addValidatedStatements(MutableList<ArchitecturalStylePatternStatement> statements);

    boolean addInconsistencies(MutableList<ArchitecturalStylePatternInconsistency> inconsistencies);

    ImmutableList<ArchitecturalStylePatternStatement> getValidatedStatements();

    ImmutableList<ArchitecturalStylePatternInconsistency> getInconsistencies();
}
