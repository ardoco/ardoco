package edu.kit.kastel.mcse.ardoco.id.tests.tasks;

import java.io.File;
import java.util.Optional;

import edu.kit.kastel.mcse.ardoco.core.tests.eval.EvaluationProject;
import edu.kit.kastel.mcse.ardoco.core.tests.eval.ExpectedResults;
import edu.kit.kastel.mcse.ardoco.tlr.models.agents.ArchitectureConfiguration;
import edu.kit.kastel.mcse.ardoco.tlr.models.agents.CodeConfiguration;

public interface ArtemisInconsistencyTask {
    EvaluationProject getEvaluationProject();

    File getTextFile();

    ExpectedResults getExpectedResults();

    default Optional<ArchitectureConfiguration> getArchitectureConfiguration() {
        return Optional.empty();
    }

    default Optional<CodeConfiguration> getCodeConfiguration() {
        return Optional.empty();
    }
}
