package edu.kit.kastel.mcse.ardoco.id.tests.integration.inconsistencyhelper;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map;

import org.eclipse.collections.api.factory.SortedMaps;
import org.eclipse.collections.api.map.sorted.MutableSortedMap;

import edu.kit.kastel.mcse.ardoco.core.api.models.architecture.ArchitectureComponent;
import edu.kit.kastel.mcse.ardoco.core.api.models.architecture.ArchitectureItem;
import edu.kit.kastel.mcse.ardoco.core.api.output.ArdocoResult;
import edu.kit.kastel.mcse.ardoco.core.data.DataRepository;
import edu.kit.kastel.mcse.ardoco.id.execution.runner.ArtemisInconsistencyDetection;
import edu.kit.kastel.mcse.ardoco.id.tests.tasks.ArtemisInconsistencyTask;
import edu.kit.kastel.mcse.ardoco.id.tests.tasks.ComponentArtemisInconsistencyTask;
import edu.kit.kastel.mcse.ardoco.tlr.models.agents.ArchitectureConfiguration;
import edu.kit.kastel.mcse.ardoco.tlr.models.agents.CodeConfiguration;
import edu.kit.kastel.mcse.ardoco.tlr.models.informants.LargeLanguageModel;

public class ArtemisInconsistencyRunProducer {
    private final LargeLanguageModel llm;
    private final ArtemisInconsistencyApproach approach;

    public ArtemisInconsistencyRunProducer(LargeLanguageModel llm, ArtemisInconsistencyApproach approach) {
        this.llm = llm;
        this.approach = approach;
    }

    private static ComponentArtemisInconsistencyTask asComponentTask(ArtemisInconsistencyTask project) {
        if (project instanceof ComponentArtemisInconsistencyTask componentTask) {
            return componentTask;
        }

        throw new IllegalArgumentException("Hold-back runs require a component ArTEMiS inconsistency task, but got " + project.getClass().getSimpleName());
    }

    private static String sanitize(String value) {
        return value.replaceAll("[^a-zA-Z0-9._-]", "_");
    }

    public ArdocoResult produceBaseRun(ArtemisInconsistencyTask project) {
        var runData = run(project, null);
        return new ArdocoResult(runData);
    }

    public Map<ArchitectureItem, ArdocoResult> produceTeamRuns(ArtemisInconsistencyTask project) {
        if (approach.supportsHoldBack()) {
            return produceHoldBackRuns(asComponentTask(project));
        }

        Map<ArchitectureItem, ArdocoResult> runs = new LinkedHashMap<>();
        runs.put(null, produceBaseRun(project));
        return runs;
    }

    private Map<ArchitectureItem, ArdocoResult> produceHoldBackRuns(ComponentArtemisInconsistencyTask project) {
        Map<ArchitectureItem, ArdocoResult> runs = new LinkedHashMap<>();

        runs.put(null, produceBaseRun(project));

        var modelProvider = new HoldBackArCoTLModelProvider(project.getArchitectureConfiguration().get().architectureFile());
        for (int i = 0; i < modelProvider.numberOfActualInstances(); i++) {
            modelProvider.setCurrentHoldBackIndex(i);
            ArchitectureComponent heldBackComponent = modelProvider.getCurrentHoldBack();

            var runData = run(project, heldBackComponent);
            runs.put(heldBackComponent, new ArdocoResult(runData));
        }

        return runs;
    }

    private DataRepository run(ArtemisInconsistencyTask project, ArchitectureComponent heldBackComponent) {
        File inputText = project.getTextFile();
        File outputDirectory = createOutputDirectory(project, heldBackComponent);
        MutableSortedMap<String, String> additionalConfigs = SortedMaps.mutable.empty();

        ArchitectureConfiguration architectureConfiguration = project.getArchitectureConfiguration().orElse(null);
        CodeConfiguration codeConfiguration = project.getCodeConfiguration().orElse(null);

        var strategies = heldBackComponent == null ? approach.createStrategies() : approach.createStrategiesWithoutModelElement(heldBackComponent.getId());

        var runner = new ArtemisInconsistencyDetection(project.getEvaluationProject().name());
        runner.setUp(inputText, architectureConfiguration, codeConfiguration, additionalConfigs.toImmutable(), outputDirectory, llm, strategies);

        return runner.runWithoutSaving();
    }

    private File createOutputDirectory(ArtemisInconsistencyTask project, ArchitectureComponent heldBackComponent) {
        String runName = heldBackComponent == null ? "base" : sanitize(heldBackComponent.getName());
        File outputDirectory = new File(
                "target/testout/artemis-id-runs/" + approach.name().toLowerCase() + "/" + project.getEvaluationProject().name() + "/" + runName);
        outputDirectory.mkdirs();
        return outputDirectory;
    }
}
