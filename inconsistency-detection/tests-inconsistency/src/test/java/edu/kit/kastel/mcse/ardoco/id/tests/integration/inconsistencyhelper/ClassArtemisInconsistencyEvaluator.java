/* Licensed under MIT 2026. */
package edu.kit.kastel.mcse.ardoco.id.tests.integration.inconsistencyhelper;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.list.MutableList;

import edu.kit.kastel.mcse.ardoco.core.api.models.Metamodel;
import edu.kit.kastel.mcse.ardoco.core.api.models.architecture.ArchitectureItem;
import edu.kit.kastel.mcse.ardoco.core.api.output.ArdocoResult;
import edu.kit.kastel.mcse.ardoco.core.api.stage.connectiongenerator.artemis.ArtemisTarget;
import edu.kit.kastel.mcse.ardoco.core.api.stage.connectiongenerator.artemis.NamedArchitectureEntity;
import edu.kit.kastel.mcse.ardoco.id.tests.tasks.ClassArtemisInconsistencyTask;
import edu.kit.kastel.mcse.ardoco.metrics.ClassificationMetricsCalculator;
import edu.kit.kastel.mcse.ardoco.metrics.result.SingleClassificationResult;
import edu.kit.kastel.mcse.ardoco.naer.model.NamedEntityType;

public class ClassArtemisInconsistencyEvaluator implements ArtemisInconsistencyEvaluator<ClassArtemisInconsistencyTask> {

    @Override
    public ImmutableList<SingleClassificationResult<String>> evaluateTeam(ClassArtemisInconsistencyTask project, Map<ArchitectureItem, ArdocoResult> runs) {
        MutableList<SingleClassificationResult<String>> results = Lists.mutable.empty();

        ArdocoResult baseRun = runs.get(null);
        if (baseRun == null) {
            throw new IllegalArgumentException("Class TEAM evaluation requires a base run stored under key null");
        }

        Set<String> expectedSentences = new LinkedHashSet<>(project.getClassTeamInconsistencies());
        Set<String> actualSentences = collectDetectedClassTeamSentences(baseRun);

        var evaluationResult = ClassificationMetricsCalculator.getInstance()
                .calculateMetrics(actualSentences, expectedSentences, baseRun.getSimplePreprocessingData().getText().getLines().size());

        results.add(evaluationResult);
        return results.toImmutable();
    }

    private Set<String> collectDetectedClassTeamSentences(ArdocoResult result) {
        var artemisTraceabilityState = result.getArtemisConnectionState(new ArtemisTarget(Metamodel.CODE_WITH_COMPILATION_UNITS, NamedEntityType.CLASS));

        return artemisTraceabilityState.getUnlinkedNamedEntities()
                .stream()
                .map(NamedArchitectureEntity::getOccurrences)
                .flatMap(java.util.Collection::stream)
                .map(occ -> occ.getSentenceNumber() + "," + occ.getName())
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }
}
