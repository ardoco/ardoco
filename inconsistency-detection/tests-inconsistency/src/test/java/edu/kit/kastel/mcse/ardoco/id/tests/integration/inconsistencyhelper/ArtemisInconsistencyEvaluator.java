package edu.kit.kastel.mcse.ardoco.id.tests.integration.inconsistencyhelper;

import java.util.Map;

import org.eclipse.collections.api.list.ImmutableList;

import edu.kit.kastel.mcse.ardoco.core.api.models.architecture.ArchitectureItem;
import edu.kit.kastel.mcse.ardoco.core.api.output.ArdocoResult;
import edu.kit.kastel.mcse.ardoco.id.tests.tasks.ArtemisInconsistencyTask;
import edu.kit.kastel.mcse.ardoco.metrics.result.SingleClassificationResult;

public interface ArtemisInconsistencyEvaluator<T extends ArtemisInconsistencyTask> {

    default SingleClassificationResult<String> evaluateMeat(T project, ArdocoResult result) {
        throw new UnsupportedOperationException(getClass().getSimpleName() + " does not support MEAT evaluation");
    }

    ImmutableList<SingleClassificationResult<String>> evaluateTeam(T project, Map<ArchitectureItem, ArdocoResult> runs);
}
