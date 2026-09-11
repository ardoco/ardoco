package edu.kit.kastel.mcse.ardoco.id.tests.integration.inconsistencyhelper.artemis.runproducer;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

import org.eclipse.collections.api.factory.SortedMaps;
import org.eclipse.collections.api.map.sorted.MutableSortedMap;

import edu.kit.kastel.mcse.ardoco.core.api.output.ArdocoResult;
import edu.kit.kastel.mcse.ardoco.core.data.DataRepository;
import edu.kit.kastel.mcse.ardoco.id.execution.runner.ArtemisInconsistencyDetection;
import edu.kit.kastel.mcse.ardoco.id.tests.integration.inconsistencyhelper.artemis.ArtemisInconsistencyEvaluationConfiguration;
import edu.kit.kastel.mcse.ardoco.id.tests.integration.inconsistencyhelper.artemis.evalruns.ArtemisEvaluationRun;
import edu.kit.kastel.mcse.ardoco.id.tests.integration.inconsistencyhelper.artemis.evalruns.BaseEvaluationRun;
import edu.kit.kastel.mcse.ardoco.id.tests.integration.inconsistencyhelper.artemis.evalruns.ExtendedSadRun;
import edu.kit.kastel.mcse.ardoco.id.tests.tasks.DatafileArtemisInconsistencyTask;
import edu.kit.kastel.mcse.ardoco.tlr.models.agents.CodeConfiguration;
import edu.kit.kastel.mcse.ardoco.tlr.models.informants.LargeLanguageModel;

public class DatafileExtendedArtemisInconsistencyRunProducer implements ArtemisInconsistencyRunProducer<DatafileArtemisInconsistencyTask> {
    private static final ArtemisInconsistencyEvaluationConfiguration CONFIGURATION = ArtemisInconsistencyEvaluationConfiguration.datafile();
    private final LargeLanguageModel llm;
    private final int numberOfRuns;

    public DatafileExtendedArtemisInconsistencyRunProducer(LargeLanguageModel llm, int numberOfRuns) {
        this.llm = llm;
        this.numberOfRuns = numberOfRuns;
    }

    @Override
    public Map<ArtemisEvaluationRun, ArdocoResult> produceRuns(DatafileArtemisInconsistencyTask project) {
        Map<ArtemisEvaluationRun, ArdocoResult> runs = new LinkedHashMap<>();

        var baseRunData = ArtemisInconsistencyRunSupport.run(project, llm, CONFIGURATION.strategies(), CONFIGURATION.name(), "base");
        runs.put(new BaseEvaluationRun(), new ArdocoResult(baseRunData));

        for (int runIndex = 0; runIndex < numberOfRuns; runIndex++) {
            var runData = runDatafileExtendedTeam(project, runIndex);
            runs.put(new ExtendedSadRun(runIndex), new ArdocoResult(runData));
        }

        return runs;
    }

    private DataRepository runDatafileExtendedTeam(DatafileArtemisInconsistencyTask project, int runIndex) {
        File sadFile = new File(Objects.requireNonNull(this.getClass()
                        .getResource("/datafile-artemis-text-extensions/" + project.getEvaluationProject().name().toLowerCase() + "/extension-" + runIndex + ".txt"))
                .getFile());
        File outputFile = new File("target/testout/artemis-id-runs/" + "datafile-extended" + "/" + project.getEvaluationProject().name() + "-" + runIndex);

        MutableSortedMap<String, String> additionalConfigs = SortedMaps.mutable.empty();
        CodeConfiguration codeConfiguration = project.getCodeConfiguration().orElse(null);
        var strategies = CONFIGURATION.strategies();

        var runner = new ArtemisInconsistencyDetection(project.getEvaluationProject().name());
        runner.setUp(sadFile, null, codeConfiguration, additionalConfigs.toImmutable(), outputFile, llm, strategies);
        return runner.runWithoutSaving();
    }

}
