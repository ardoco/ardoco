package edu.kit.kastel.mcse.ardoco.id.tests.integration.inconsistencyhelper.artemis.runproducer;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import edu.kit.kastel.mcse.ardoco.core.api.models.architecture.ArchitectureComponent;
import edu.kit.kastel.mcse.ardoco.core.api.output.ArdocoResult;
import edu.kit.kastel.mcse.ardoco.id.tests.integration.inconsistencyhelper.HoldBackArCoTLModelProvider;
import edu.kit.kastel.mcse.ardoco.id.tests.integration.inconsistencyhelper.artemis.*;
import edu.kit.kastel.mcse.ardoco.id.tests.integration.inconsistencyhelper.artemis.evalruns.ArtemisEvaluationRun;
import edu.kit.kastel.mcse.ardoco.id.tests.integration.inconsistencyhelper.artemis.evalruns.BaseEvaluationRun;
import edu.kit.kastel.mcse.ardoco.id.tests.integration.inconsistencyhelper.artemis.evalruns.HeldBackArchitectureItemRun;
import edu.kit.kastel.mcse.ardoco.id.tests.tasks.ComponentArtemisInconsistencyTask;
import edu.kit.kastel.mcse.ardoco.tlr.artemis.strategies.ArtemisNerStrategy;
import edu.kit.kastel.mcse.ardoco.tlr.models.informants.LargeLanguageModel;

public final class ComponentHoldBackArtemisInconsistencyRunProducer implements ArtemisInconsistencyRunProducer<ComponentArtemisInconsistencyTask> {
    private static final ArtemisInconsistencyEvaluationConfiguration CONFIGURATION = ArtemisInconsistencyEvaluationConfiguration.component();

    private final LargeLanguageModel llm;

    public ComponentHoldBackArtemisInconsistencyRunProducer(LargeLanguageModel llm) {
        this.llm = llm;
    }

    private static List<ArtemisNerStrategy> createStrategiesWithoutComponent(ArchitectureComponent heldBackComponent) {
        return List.of(new HoldBackComponentArtemisNerStrategy(heldBackComponent.getId()));
    }

    @Override
    public Map<ArtemisEvaluationRun, ArdocoResult> produceRuns(ComponentArtemisInconsistencyTask project) {
        Map<ArtemisEvaluationRun, ArdocoResult> runs = new LinkedHashMap<>();

        var baseRunData = ArtemisInconsistencyRunSupport.run(project, llm, CONFIGURATION.strategies(), CONFIGURATION.outputName(), "base");
        runs.put(new BaseEvaluationRun(), new ArdocoResult(baseRunData));

        var modelProvider = new HoldBackArCoTLModelProvider(project.getArchitectureConfiguration().orElseThrow().architectureFile());

        for (int i = 0; i < modelProvider.numberOfActualInstances(); i++) {
            modelProvider.setCurrentHoldBackIndex(i);
            ArchitectureComponent heldBackComponent = modelProvider.getCurrentHoldBack();

            var strategies = createStrategiesWithoutComponent(heldBackComponent);
            var runData = ArtemisInconsistencyRunSupport.run(project, llm, strategies, CONFIGURATION.outputName(), heldBackComponent.getName());

            runs.put(new HeldBackArchitectureItemRun(heldBackComponent), new ArdocoResult(runData));
        }

        return runs;
    }
}
