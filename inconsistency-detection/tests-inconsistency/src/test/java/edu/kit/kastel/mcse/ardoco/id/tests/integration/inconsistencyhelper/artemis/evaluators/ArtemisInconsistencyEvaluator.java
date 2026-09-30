/* Licensed under MIT 2026. */
package edu.kit.kastel.mcse.ardoco.id.tests.integration.inconsistencyhelper.artemis.evaluators;

import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.map.MutableMap;

import edu.kit.kastel.mcse.ardoco.core.api.output.ArdocoResult;
import edu.kit.kastel.mcse.ardoco.id.tests.integration.inconsistencyhelper.artemis.evalruns.ArtemisEvaluationRun;
import edu.kit.kastel.mcse.ardoco.id.tests.tasks.ArtemisInconsistencyTask;
import edu.kit.kastel.mcse.ardoco.metrics.result.SingleClassificationResult;

public interface ArtemisInconsistencyEvaluator<T extends ArtemisInconsistencyTask> {

    default SingleClassificationResult<String> evaluateMeat(T project, ArdocoResult result) {
        throw new UnsupportedOperationException(getClass().getSimpleName() + " does not support MEAT evaluation");
    }

    ImmutableList<SingleClassificationResult<String>> evaluateTeam(T project, MutableMap<ArtemisEvaluationRun, ArdocoResult> runs);
}
