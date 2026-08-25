package edu.kit.kastel.mcse.ardoco.id.tests.integration.inconsistencyhelper.artemis.runproducer;

import java.util.Map;

import edu.kit.kastel.mcse.ardoco.core.api.output.ArdocoResult;
import edu.kit.kastel.mcse.ardoco.id.tests.integration.inconsistencyhelper.artemis.evalruns.ArtemisEvaluationRun;
import edu.kit.kastel.mcse.ardoco.id.tests.tasks.ArtemisInconsistencyTask;

public interface ArtemisInconsistencyRunProducer<T extends ArtemisInconsistencyTask> {
    Map<ArtemisEvaluationRun, ArdocoResult> produceRuns(T project);
}
