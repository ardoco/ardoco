package edu.kit.kastel.mcse.ardoco.id.tests.integration.inconsistencyhelper.artemis.runproducer;

import java.util.LinkedHashMap;
import java.util.Map;

import edu.kit.kastel.mcse.ardoco.core.api.output.ArdocoResult;
import edu.kit.kastel.mcse.ardoco.id.tests.integration.inconsistencyhelper.artemis.evalruns.ArtemisEvaluationRun;
import edu.kit.kastel.mcse.ardoco.id.tests.integration.inconsistencyhelper.artemis.ArtemisInconsistencyEvaluationConfiguration;
import edu.kit.kastel.mcse.ardoco.id.tests.integration.inconsistencyhelper.artemis.evalruns.BaseEvaluationRun;
import edu.kit.kastel.mcse.ardoco.id.tests.tasks.ArtemisInconsistencyTask;
import edu.kit.kastel.mcse.ardoco.tlr.models.informants.LargeLanguageModel;

public final class SingleArtemisInconsistencyRunProducer<T extends ArtemisInconsistencyTask> implements ArtemisInconsistencyRunProducer<T> {
    private final LargeLanguageModel llm;
    private final ArtemisInconsistencyEvaluationConfiguration configuration;

    public SingleArtemisInconsistencyRunProducer(LargeLanguageModel llm, ArtemisInconsistencyEvaluationConfiguration configuration) {
        this.llm = llm;
        this.configuration = configuration;
    }

    public ArdocoResult produceBaseRun(T project) {
        var runData = ArtemisInconsistencyRunSupport.run(project, llm, configuration.strategies(), configuration.outputName(), "base");
        return new ArdocoResult(runData);
    }

    @Override
    public Map<ArtemisEvaluationRun, ArdocoResult> produceRuns(T project) {
        Map<ArtemisEvaluationRun, ArdocoResult> runs = new LinkedHashMap<>();
        runs.put(new BaseEvaluationRun(), produceBaseRun(project));
        return runs;
    }
}
