package edu.kit.kastel.mcse.ardoco.id.tests.integration.inconsistencyhelper.artemis.evaluators;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import edu.kit.kastel.mcse.ardoco.id.tests.integration.inconsistencyhelper.artemis.evalruns.ArtemisEvaluationRun;
import edu.kit.kastel.mcse.ardoco.id.tests.integration.inconsistencyhelper.artemis.evalruns.HeldBackClassesRun;

import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.list.MutableList;

import edu.kit.kastel.mcse.ardoco.core.api.models.Metamodel;
import edu.kit.kastel.mcse.ardoco.core.api.output.ArdocoResult;
import edu.kit.kastel.mcse.ardoco.core.api.stage.connectiongenerator.artemis.ArtemisTarget;
import edu.kit.kastel.mcse.ardoco.core.api.stage.connectiongenerator.artemis.NamedArchitectureEntity;
import edu.kit.kastel.mcse.ardoco.id.tests.tasks.ClassArtemisInconsistencyTask;
import edu.kit.kastel.mcse.ardoco.metrics.ClassificationMetricsCalculator;
import edu.kit.kastel.mcse.ardoco.metrics.result.SingleClassificationResult;
import edu.kit.kastel.mcse.ardoco.naer.model.NamedEntityType;

public class ClassArtemisInconsistencyEvaluator implements ArtemisInconsistencyEvaluator<ClassArtemisInconsistencyTask> {

    @Override
    public ImmutableList<SingleClassificationResult<String>> evaluateTeam(ClassArtemisInconsistencyTask project, Map<ArtemisEvaluationRun, ArdocoResult> runs) {
        if (containsClassHoldbackRuns(runs)) {
            return evaluateClassHoldbackTeam(project, runs);
        }

        return evaluateRegularClassTeam(project, runs);
    }

    private boolean containsClassHoldbackRuns(Map<ArtemisEvaluationRun, ArdocoResult> runs) {
        return runs.keySet().stream().anyMatch(HeldBackClassesRun.class::isInstance);
    }

    private ImmutableList<SingleClassificationResult<String>> evaluateRegularClassTeam(ClassArtemisInconsistencyTask project,
            Map<ArtemisEvaluationRun, ArdocoResult> runs) {
        MutableList<SingleClassificationResult<String>> results = Lists.mutable.empty();

        ArdocoResult baseRun = getBaseRun(runs);

        Set<String> expectedSentences = new LinkedHashSet<>(project.getClassTeamInconsistencies());
        Set<String> actualSentences = collectDetectedClassTeamSentences(baseRun);

        var evaluationResult = ClassificationMetricsCalculator.getInstance()
                .calculateMetrics(actualSentences, expectedSentences, baseRun.getSimplePreprocessingData().getText().getLines().size());

        results.add(evaluationResult);
        return results.toImmutable();
    }

    private ImmutableList<SingleClassificationResult<String>> evaluateClassHoldbackTeam(ClassArtemisInconsistencyTask project,
            Map<ArtemisEvaluationRun, ArdocoResult> runs) {
        MutableList<SingleClassificationResult<String>> results = Lists.mutable.empty();

        assertBaseRunExists(runs);

        var tlrGoldStandard = project.getExpectedTraceLinks();

        for (var run : runs.entrySet()) {
            if (!(run.getKey() instanceof HeldBackClassesRun heldBackClassesRun)) {
                continue;
            }

            ArdocoResult result = run.getValue();

            Set<String> expectedSentences = new LinkedHashSet<>(project.getClassTeamInconsistencies());
            expectedSentences.addAll(tlrGoldStandard.stream()
                    .filter(link -> heldBackClassesRun.classNames().contains(link.second()))
                    .map(link -> link.first() + "," + link.second())
                    .collect(Collectors.toCollection(LinkedHashSet::new)));

            Set<String> actualSentences = collectDetectedClassTeamSentences(result);

            var evaluationResult = ClassificationMetricsCalculator.getInstance()
                    .calculateMetrics(actualSentences, expectedSentences, result.getSimplePreprocessingData().getText().getLines().size());

            results.add(evaluationResult);
        }

        return results.toImmutable();
    }

    private ArdocoResult getBaseRun(Map<ArtemisEvaluationRun, ArdocoResult> runs) {
        return runs.entrySet()
                .stream()
                .filter(entry -> entry.getKey().isBaseRun())
                .map(Map.Entry::getValue)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Class TEAM evaluation requires a base run"));
    }

    private void assertBaseRunExists(Map<ArtemisEvaluationRun, ArdocoResult> runs) {
        if (runs.keySet().stream().noneMatch(ArtemisEvaluationRun::isBaseRun)) {
            throw new IllegalArgumentException("Class TEAM holdback evaluation requires a base run");
        }
    }

    private Set<String> collectDetectedClassTeamSentences(ArdocoResult result) {
        var artemisConnectionState = result.getArtemisConnectionState(new ArtemisTarget(Metamodel.CODE_WITH_COMPILATION_UNITS, NamedEntityType.CLASS));

        return artemisConnectionState.getUnlinkedNamedEntities()
                .stream()
                .map(NamedArchitectureEntity::getOccurrences)
                .flatMap(java.util.Collection::stream)
                .map(occ -> {
                    String className = Objects.requireNonNull(occ.getName());
                    int dot = className.lastIndexOf('.');
                    int slash = className.lastIndexOf('/');
                    int index = Math.max(dot, slash);

                    if (index >= 0) {
                        className = className.substring(index + 1);
                    }

                    return occ.getSentenceNumber() + "," + className;
                })
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }
}
