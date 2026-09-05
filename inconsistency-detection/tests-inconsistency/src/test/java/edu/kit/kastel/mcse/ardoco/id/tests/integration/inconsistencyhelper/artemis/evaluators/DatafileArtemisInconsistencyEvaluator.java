package edu.kit.kastel.mcse.ardoco.id.tests.integration.inconsistencyhelper.artemis.evaluators;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.list.MutableList;

import edu.kit.kastel.mcse.ardoco.core.api.models.Metamodel;
import edu.kit.kastel.mcse.ardoco.core.api.output.ArdocoResult;
import edu.kit.kastel.mcse.ardoco.core.api.stage.connectiongenerator.artemis.ArtemisTarget;
import edu.kit.kastel.mcse.ardoco.core.api.stage.connectiongenerator.artemis.NamedArchitectureEntity;
import edu.kit.kastel.mcse.ardoco.id.tests.integration.inconsistencyhelper.artemis.evalruns.ArtemisEvaluationRun;
import edu.kit.kastel.mcse.ardoco.id.tests.tasks.DatafileArtemisInconsistencyTask;
import edu.kit.kastel.mcse.ardoco.metrics.ClassificationMetricsCalculator;
import edu.kit.kastel.mcse.ardoco.metrics.result.SingleClassificationResult;
import edu.kit.kastel.mcse.ardoco.naer.model.NamedEntityType;

public class DatafileArtemisInconsistencyEvaluator implements ArtemisInconsistencyEvaluator<DatafileArtemisInconsistencyTask> {

    @Override
    public ImmutableList<SingleClassificationResult<String>> evaluateTeam(DatafileArtemisInconsistencyTask project,
            Map<ArtemisEvaluationRun, ArdocoResult> runs) {
        MutableList<SingleClassificationResult<String>> results = Lists.mutable.empty();

        ArdocoResult baseRun = getBaseRun(runs);

        Set<String> expectedSentences = project.getExpectedTraceLinks()
                .stream()
                .map(link -> link.first() + "," + link.second())
                .collect(Collectors.toCollection(LinkedHashSet::new));

        Set<String> actualSentences = collectDetectedDatafileTeamSentences(baseRun);

        var evaluationResult = ClassificationMetricsCalculator.getInstance()
                .calculateMetrics(actualSentences, expectedSentences, baseRun.getSimplePreprocessingData().getText().getLines().size());

        results.add(evaluationResult);
        return results.toImmutable();
    }

    private ArdocoResult getBaseRun(Map<ArtemisEvaluationRun, ArdocoResult> runs) {
        return runs.entrySet()
                .stream()
                .filter(entry -> entry.getKey().isBaseRun())
                .map(Map.Entry::getValue)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Datafile TEAM evaluation requires a base run"));
    }

    private Set<String> collectDetectedDatafileTeamSentences(ArdocoResult result) {
        var artemisConnectionState = result.getArtemisConnectionState(new ArtemisTarget(Metamodel.CODE_WITH_COMPILATION_UNITS, NamedEntityType.DATAFILE));
        //TODO in the future use: ID state probably...
        return artemisConnectionState.getUnlinkedNamedEntities()
                .stream()
                .map(NamedArchitectureEntity::getOccurrences)
                .flatMap(java.util.Collection::stream)
                .map(occurrence -> occurrence.getSentenceNumber() + "," + Objects.requireNonNull(occurrence.getName()))
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }
}
