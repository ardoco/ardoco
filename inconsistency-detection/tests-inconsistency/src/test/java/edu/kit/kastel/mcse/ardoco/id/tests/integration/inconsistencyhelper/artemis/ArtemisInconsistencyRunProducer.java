package edu.kit.kastel.mcse.ardoco.id.tests.integration.inconsistencyhelper.artemis;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

import edu.kit.kastel.mcse.ardoco.id.tests.integration.inconsistencyhelper.HoldBackArCoTLModelProvider;
import org.eclipse.collections.api.factory.SortedMaps;
import org.eclipse.collections.api.map.sorted.MutableSortedMap;
import org.jspecify.annotations.NonNull;

import edu.kit.kastel.mcse.ardoco.core.api.models.Metamodel;
import edu.kit.kastel.mcse.ardoco.core.api.models.architecture.ArchitectureComponent;
import edu.kit.kastel.mcse.ardoco.core.api.output.ArdocoResult;
import edu.kit.kastel.mcse.ardoco.core.common.tuple.Pair;
import edu.kit.kastel.mcse.ardoco.core.common.util.CommonUtilities;
import edu.kit.kastel.mcse.ardoco.core.common.util.DataRepositoryHelper;
import edu.kit.kastel.mcse.ardoco.core.data.DataRepository;
import edu.kit.kastel.mcse.ardoco.core.execution.runner.AnonymousRunner;
import edu.kit.kastel.mcse.ardoco.core.pipeline.AbstractPipelineStep;
import edu.kit.kastel.mcse.ardoco.id.ArtemisInconsistencyChecker;
import edu.kit.kastel.mcse.ardoco.id.execution.runner.ArtemisInconsistencyDetection;
import edu.kit.kastel.mcse.ardoco.id.tests.tasks.ArtemisInconsistencyTask;
import edu.kit.kastel.mcse.ardoco.id.tests.tasks.ClassArtemisInconsistencyTask;
import edu.kit.kastel.mcse.ardoco.id.tests.tasks.ClassSadCodeTlrTask;
import edu.kit.kastel.mcse.ardoco.id.tests.tasks.ComponentArtemisInconsistencyTask;
import edu.kit.kastel.mcse.ardoco.tlr.artemis.ArtemisNer;
import edu.kit.kastel.mcse.ardoco.tlr.artemis.ArtemisPostprocessing;
import edu.kit.kastel.mcse.ardoco.tlr.artemis.ArtemisPreprocessing;
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

    public Map<ArtemisEvaluationRun, ArdocoResult> produceTeamRuns(ArtemisInconsistencyTask project) {
        if (approach.supportsHoldBack()) {
            return produceHoldBackRuns(asComponentTask(project));
        }

        Map<ArtemisEvaluationRun, ArdocoResult> runs = new LinkedHashMap<>();
        runs.put(new BaseEvaluationRun(), produceBaseRun(project));
        return runs;
    }

    private Map<ArtemisEvaluationRun, ArdocoResult> produceHoldBackRuns(ComponentArtemisInconsistencyTask project) {
        Map<ArtemisEvaluationRun, ArdocoResult> runs = new LinkedHashMap<>();

        runs.put(new BaseEvaluationRun(), produceBaseRun(project));

        var modelProvider = new HoldBackArCoTLModelProvider(project.getArchitectureConfiguration().get().architectureFile());
        for (int i = 0; i < modelProvider.numberOfActualInstances(); i++) {
            modelProvider.setCurrentHoldBackIndex(i);
            ArchitectureComponent heldBackComponent = modelProvider.getCurrentHoldBack();

            var runData = run(project, heldBackComponent);
            runs.put(new HeldBackArchitectureItemRun(heldBackComponent), new ArdocoResult(runData));
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

    private DataRepository runClassHoldBackTeam(ArtemisInconsistencyTask project, List<String> heldBackClassNames) {
        File inputText = project.getTextFile();
        MutableSortedMap<String, String> additionalConfigs = SortedMaps.mutable.empty();

        CodeConfiguration codeConfiguration = project.getCodeConfiguration().orElse(null);

        var strategies = approach.createStrategies();

        return new AnonymousRunner(project.getEvaluationProject().name() + "-class-team-holdback") {
            @Override
            public @NonNull List<AbstractPipelineStep> initializePipelineSteps(@NonNull DataRepository dataRepository) {
                var pipelineSteps = new ArrayList<AbstractPipelineStep>();

                var text = CommonUtilities.readInputText(inputText);
                if (text.isBlank()) {
                    throw new IllegalArgumentException("Cannot deal with empty input text. Maybe there was an error reading the file.");
                }

                DataRepositoryHelper.putInputText(dataRepository, text);

                pipelineSteps.add(ArtemisPreprocessing.get(additionalConfigs.toImmutable(), dataRepository, null,
                        Objects.requireNonNull(codeConfiguration).withMetamodel(Metamodel.CODE_WITH_COMPILATION_UNITS)));
                pipelineSteps.add(ArtemisNer.get(additionalConfigs.toImmutable(), dataRepository, llm, strategies));
                pipelineSteps.add(new HoldbackClassArtemisConnectionInformant(dataRepository, strategies.getFirst(),
                        heldBackClassNames)); //here we alter the normal pipeline to be able to hold back classes
                pipelineSteps.add(ArtemisPostprocessing.get(additionalConfigs.toImmutable(), dataRepository));
                pipelineSteps.add(ArtemisInconsistencyChecker.get(additionalConfigs.toImmutable(), dataRepository, strategies));

                return pipelineSteps;
            }
        }.runWithoutSaving();
    }

    public Map<ArtemisEvaluationRun, ArdocoResult> produceClassHoldBackTeamRuns(ClassArtemisInconsistencyTask project, int numberOfRuns,
            int numberOfHeldBackClassesPerRun, long seed) {
        if (approach != ArtemisInconsistencyApproach.CLASS) {
            throw new IllegalStateException("Class hold-back runs require " + ArtemisInconsistencyApproach.CLASS);
        }

        Map<ArtemisEvaluationRun, ArdocoResult> runs = new LinkedHashMap<>();
        runs.put(new BaseEvaluationRun(), produceBaseRun(project));

        var candidateClasses = getClassesFromTlrGoldStandard(project);
        if (candidateClasses.size() < numberOfHeldBackClassesPerRun) {
            throw new IllegalArgumentException(
                    "Cannot hold back " + numberOfHeldBackClassesPerRun + " classes because only " + candidateClasses.size() + " classes occur in the TLR gold standard");
        }

        for (int runIndex = 0; runIndex < numberOfRuns; runIndex++) {
            var heldBackClasses = selectRandomClasses(candidateClasses, numberOfHeldBackClassesPerRun, seed + runIndex);
            var runData = runClassHoldBackTeam(project, heldBackClasses);
            runs.put(new HeldBackClassesRun(heldBackClasses, runIndex), new ArdocoResult(runData));
        }

        return runs;
    }

    private Set<String> getClassesFromTlrGoldStandard(ClassArtemisInconsistencyTask project) {
        return ClassSadCodeTlrTask.valueOf(project.name())
                .getExpectedTraceLinks()
                .stream()
                .map(Pair::second)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private List<String> selectRandomClasses(Set<String> candidateClasses, int numberOfHeldBackClassesPerRun, long seed) {
        var shuffledClasses = new java.util.ArrayList<>(candidateClasses);
        Collections.shuffle(shuffledClasses, new Random(seed));
        return shuffledClasses.stream().limit(numberOfHeldBackClassesPerRun).collect(Collectors.toCollection(ArrayList::new));
    }

}
