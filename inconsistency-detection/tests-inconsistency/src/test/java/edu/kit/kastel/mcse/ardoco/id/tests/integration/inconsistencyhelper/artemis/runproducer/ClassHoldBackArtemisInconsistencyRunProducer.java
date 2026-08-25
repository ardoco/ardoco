package edu.kit.kastel.mcse.ardoco.id.tests.integration.inconsistencyhelper.artemis.runproducer;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

import edu.kit.kastel.mcse.ardoco.id.tests.integration.inconsistencyhelper.artemis.*;
import edu.kit.kastel.mcse.ardoco.id.tests.integration.inconsistencyhelper.artemis.evalruns.ArtemisEvaluationRun;
import edu.kit.kastel.mcse.ardoco.id.tests.integration.inconsistencyhelper.artemis.evalruns.BaseEvaluationRun;
import edu.kit.kastel.mcse.ardoco.id.tests.integration.inconsistencyhelper.artemis.evalruns.HeldBackClassesRun;

import org.eclipse.collections.api.factory.SortedMaps;
import org.eclipse.collections.api.map.sorted.MutableSortedMap;
import org.jspecify.annotations.NonNull;

import edu.kit.kastel.mcse.ardoco.core.api.models.Metamodel;
import edu.kit.kastel.mcse.ardoco.core.api.output.ArdocoResult;
import edu.kit.kastel.mcse.ardoco.core.common.tuple.Pair;
import edu.kit.kastel.mcse.ardoco.core.common.util.CommonUtilities;
import edu.kit.kastel.mcse.ardoco.core.common.util.DataRepositoryHelper;
import edu.kit.kastel.mcse.ardoco.core.data.DataRepository;
import edu.kit.kastel.mcse.ardoco.core.execution.runner.AnonymousRunner;
import edu.kit.kastel.mcse.ardoco.core.pipeline.AbstractPipelineStep;
import edu.kit.kastel.mcse.ardoco.id.ArtemisInconsistencyChecker;
import edu.kit.kastel.mcse.ardoco.id.tests.tasks.ClassArtemisInconsistencyTask;
import edu.kit.kastel.mcse.ardoco.id.tests.tasks.ClassSadCodeTlrTask;
import edu.kit.kastel.mcse.ardoco.tlr.artemis.ArtemisNer;
import edu.kit.kastel.mcse.ardoco.tlr.artemis.ArtemisPostprocessing;
import edu.kit.kastel.mcse.ardoco.tlr.artemis.ArtemisPreprocessing;
import edu.kit.kastel.mcse.ardoco.tlr.models.agents.CodeConfiguration;
import edu.kit.kastel.mcse.ardoco.tlr.models.informants.LargeLanguageModel;

public final class ClassHoldBackArtemisInconsistencyRunProducer implements ArtemisInconsistencyRunProducer<ClassArtemisInconsistencyTask> {
    private static final ArtemisInconsistencyEvaluationConfiguration CONFIGURATION = ArtemisInconsistencyEvaluationConfiguration.clazz();

    private final LargeLanguageModel llm;
    private final int numberOfRuns;
    private final int numberOfHeldBackClassesPerRun;
    private final long seed;

    public ClassHoldBackArtemisInconsistencyRunProducer(LargeLanguageModel llm, int numberOfRuns, int numberOfHeldBackClassesPerRun, long seed) {
        this.llm = llm;
        this.numberOfRuns = numberOfRuns;
        this.numberOfHeldBackClassesPerRun = numberOfHeldBackClassesPerRun;
        this.seed = seed;
    }

    private static Set<String> getClassesFromTlrGoldStandard(ClassArtemisInconsistencyTask project) {
        return ClassSadCodeTlrTask.valueOf(project.name())
                .getExpectedTraceLinks()
                .stream()
                .map(Pair::second)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private static List<String> selectRandomClasses(Set<String> candidateClasses, int numberOfHeldBackClassesPerRun, long seed) {
        var shuffledClasses = new ArrayList<>(candidateClasses);
        Collections.shuffle(shuffledClasses, new Random(seed));
        return shuffledClasses.stream().limit(numberOfHeldBackClassesPerRun).collect(Collectors.toCollection(ArrayList::new));
    }

    @Override
    public Map<ArtemisEvaluationRun, ArdocoResult> produceRuns(ClassArtemisInconsistencyTask project) {
        Map<ArtemisEvaluationRun, ArdocoResult> runs = new LinkedHashMap<>();

        var baseRunData = ArtemisInconsistencyRunSupport.run(project, llm, CONFIGURATION.strategies(), CONFIGURATION.outputName(), "base");
        runs.put(new BaseEvaluationRun(), new ArdocoResult(baseRunData));

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

    private DataRepository runClassHoldBackTeam(ClassArtemisInconsistencyTask project, List<String> heldBackClassNames) {
        File inputText = project.getTextFile();
        MutableSortedMap<String, String> additionalConfigs = SortedMaps.mutable.empty();

        CodeConfiguration codeConfiguration = project.getCodeConfiguration().orElse(null);
        var strategies = CONFIGURATION.strategies();

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
                pipelineSteps.add(new HoldbackClassArtemisConnectionInformant(dataRepository, strategies.getFirst(), heldBackClassNames));
                pipelineSteps.add(ArtemisPostprocessing.get(additionalConfigs.toImmutable(), dataRepository));
                pipelineSteps.add(ArtemisInconsistencyChecker.get(additionalConfigs.toImmutable(), dataRepository, strategies));

                return pipelineSteps;
            }
        }.runWithoutSaving();
    }
}
