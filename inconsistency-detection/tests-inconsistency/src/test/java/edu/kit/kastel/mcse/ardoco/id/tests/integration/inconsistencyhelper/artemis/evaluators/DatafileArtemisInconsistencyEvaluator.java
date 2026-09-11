package edu.kit.kastel.mcse.ardoco.id.tests.integration.inconsistencyhelper.artemis.evaluators;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
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
import edu.kit.kastel.mcse.ardoco.core.common.tuple.Pair;
import edu.kit.kastel.mcse.ardoco.core.tests.eval.EvaluationHelper;
import edu.kit.kastel.mcse.ardoco.id.tests.integration.inconsistencyhelper.artemis.evalruns.ArtemisEvaluationRun;
import edu.kit.kastel.mcse.ardoco.id.tests.integration.inconsistencyhelper.artemis.evalruns.ExtendedSadRun;
import edu.kit.kastel.mcse.ardoco.id.tests.tasks.DatafileArtemisInconsistencyTask;
import edu.kit.kastel.mcse.ardoco.metrics.ClassificationMetricsCalculator;
import edu.kit.kastel.mcse.ardoco.metrics.result.SingleClassificationResult;
import edu.kit.kastel.mcse.ardoco.naer.model.NamedEntityType;

public class DatafileArtemisInconsistencyEvaluator implements ArtemisInconsistencyEvaluator<DatafileArtemisInconsistencyTask> {

    @Override
    public ImmutableList<SingleClassificationResult<String>> evaluateTeam(DatafileArtemisInconsistencyTask project,
            Map<ArtemisEvaluationRun, ArdocoResult> runs) {
        if (containsExtendedSadRuns(runs)) {
            return evaluateExtendedDatafileTeam(project, runs);
        }

        return evaluateRegularDatafileTeam(project, runs);
    }

    private boolean containsExtendedSadRuns(Map<ArtemisEvaluationRun, ArdocoResult> runs) {
        return runs.keySet().stream().anyMatch(ExtendedSadRun.class::isInstance);
    }

    private ImmutableList<SingleClassificationResult<String>> evaluateRegularDatafileTeam(DatafileArtemisInconsistencyTask project,
            Map<ArtemisEvaluationRun, ArdocoResult> runs) {
        MutableList<SingleClassificationResult<String>> results = Lists.mutable.empty();

        ArdocoResult baseRun = getBaseRun(runs);

        Set<String> expectedSentences = getExpectedSentences(project);
        Set<String> actualSentences = collectDetectedDatafileTeamSentences(baseRun);

        var evaluationResult = ClassificationMetricsCalculator.getInstance()
                .calculateMetrics(actualSentences, expectedSentences, baseRun.getSimplePreprocessingData().getText().getLines().size());

        results.add(evaluationResult);
        return results.toImmutable();
    }

    private ImmutableList<SingleClassificationResult<String>> evaluateExtendedDatafileTeam(DatafileArtemisInconsistencyTask project,
            Map<ArtemisEvaluationRun, ArdocoResult> runs) {
        MutableList<SingleClassificationResult<String>> results = Lists.mutable.empty();

        int runNumber = 0;
        for (var run : runs.entrySet()) {
            if (!(run.getKey() instanceof ExtendedSadRun)) {
                continue;
            }

            ArdocoResult result = run.getValue();
            Set<String> expectedSentences = getExpectedSentences(project);
            expectedSentences.addAll(getExpectedExtendedTraceLinks(project, runNumber));
            Set<String> actualSentences = collectDetectedDatafileTeamSentences(result);

            var evaluationResult = ClassificationMetricsCalculator.getInstance()
                    .calculateMetrics(actualSentences, expectedSentences, result.getSimplePreprocessingData().getText().getLines().size());

            results.add(evaluationResult);
        }

        return results.toImmutable();
    }

    private List<String> getExpectedExtendedTraceLinks(DatafileArtemisInconsistencyTask project, int runNumber) {
        File file = EvaluationHelper.loadFileFromResources(
                "/datafile-artemis-text-extensions/" + project.getEvaluationProject().name().toLowerCase() + "/goldstandard-extension-" + runNumber + ".csv");

        List<String> goldLinks;
        try {
            goldLinks = Files.readAllLines(file.toPath());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        goldLinks.removeFirst(); // remove header
        goldLinks.removeIf(String::isBlank);

        List<Pair<Integer, String>> expectedLinks = new ArrayList<>();
        for (String line : goldLinks) {
            String[] parts = line.split(",");
            if (parts.length < 2) {
                throw new IllegalArgumentException("Invalid gold standard format: " + line);
            }
            int sentenceId = Integer.parseInt(parts[0].trim());
            String modelElementId = parts[1].trim();
            expectedLinks.add(new Pair<>(sentenceId, modelElementId));
        }

        return expectedLinks.stream().map(pair -> pair.first() + "," + pair.second()).toList();
    }

    private Set<String> getExpectedSentences(DatafileArtemisInconsistencyTask project) {
        return project.getExpectedTraceLinks().stream().map(link -> link.first() + "," + link.second()).collect(Collectors.toCollection(LinkedHashSet::new));
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
