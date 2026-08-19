/* Licensed under MIT 2026. */
package edu.kit.kastel.mcse.ardoco.id.tests.integration.inconsistencyhelper;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.factory.Sets;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.list.MutableList;
import org.eclipse.collections.api.set.MutableSet;

import edu.kit.kastel.mcse.ardoco.core.api.entity.Entity;
import edu.kit.kastel.mcse.ardoco.core.api.models.ArchitectureComponentModel;
import edu.kit.kastel.mcse.ardoco.core.api.models.Metamodel;
import edu.kit.kastel.mcse.ardoco.core.api.models.architecture.ArchitectureItem;
import edu.kit.kastel.mcse.ardoco.core.api.output.ArdocoResult;
import edu.kit.kastel.mcse.ardoco.core.api.stage.connectiongenerator.artemis.ArtemisTarget;
import edu.kit.kastel.mcse.ardoco.core.api.stage.connectiongenerator.artemis.NamedArchitectureEntity;
import edu.kit.kastel.mcse.ardoco.id.tests.tasks.ComponentArtemisInconsistencyTask;
import edu.kit.kastel.mcse.ardoco.metrics.ClassificationMetricsCalculator;
import edu.kit.kastel.mcse.ardoco.metrics.result.SingleClassificationResult;
import edu.kit.kastel.mcse.ardoco.naer.model.NamedEntityType;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.architecture.pcm.PcmExtractor;

/**
 * Evaluates ArTEMiS component-based inconsistency detection results.
 */
public class ComponentArtemisInconsistencyEvaluator implements ArtemisInconsistencyEvaluator<ComponentArtemisInconsistencyTask> {
    private static ArchitectureComponentModel createComponentModel(ComponentArtemisInconsistencyTask project) {
        var extractor = new PcmExtractor(project.getArchitectureConfiguration().get().architectureFile().getAbsolutePath(),
                Metamodel.ARCHITECTURE_WITH_COMPONENTS);
        return new ArchitectureComponentModel(extractor.extractModel());
    }

    @Override
    public SingleClassificationResult<String> evaluateMeat(ComponentArtemisInconsistencyTask project, ArdocoResult result) {
        Set<String> expectedUnmentionedModelElements = new LinkedHashSet<>(project.getUnmentionedModelElementIds());

        var model = result.getModelState(Metamodel.ARCHITECTURE_WITH_COMPONENTS);
        Set<String> allModelIds = model.getEndpoints().stream().map(Entity::getId).collect(Collectors.toCollection(LinkedHashSet::new));

        Set<String> linkedModelIds = result.getArtemisConnectionState(new ArtemisTarget(Metamodel.ARCHITECTURE_WITH_COMPONENTS, NamedEntityType.COMPONENT))
                .getTraceLinks()
                .stream()
                .map(traceLink -> traceLink.getSecondEndpoint().getId())
                .collect(Collectors.toCollection(LinkedHashSet::new));

        Set<String> actualUnmentionedModelElements = allModelIds.stream()
                .filter(modelId -> !linkedModelIds.contains(modelId))
                .collect(Collectors.toCollection(LinkedHashSet::new));

        int confusionMatrixSize = result.getSimplePreprocessingData().getText().getLines().size();
        return ClassificationMetricsCalculator.getInstance()
                .calculateMetrics(actualUnmentionedModelElements, expectedUnmentionedModelElements, confusionMatrixSize);
    }

    @Override
    public ImmutableList<SingleClassificationResult<String>> evaluateTeam(ComponentArtemisInconsistencyTask project, Map<ArchitectureItem, ArdocoResult> runs) {
        MutableList<SingleClassificationResult<String>> results = Lists.mutable.empty();

        var goldStandard = project.getGoldstandardForArchitectureModel(createComponentModel(project));

        for (var run : runs.entrySet()) {
            ArchitectureItem heldBackElement = run.getKey();
            ArdocoResult result = run.getValue();

            if (heldBackElement == null) {
                continue;
            }

            MutableSet<String> expectedSentences = goldStandard.getSentencesWithElement(heldBackElement)
                    .toSet()
                    .stream()
                    .map(s -> s + "," + heldBackElement.getName())
                    .collect(Collectors.toCollection(Sets.mutable::empty));

            var actualSentences = collectDetectedTeamSentencesForHeldBackElement(result, heldBackElement);

            var evaluationResult = ClassificationMetricsCalculator.getInstance()
                    .calculateMetrics(actualSentences, expectedSentences, result.getSimplePreprocessingData().getText().getLines().size());

            results.add(evaluationResult);
        }

        return results.toImmutable();
    }

    private Set<String> collectDetectedTeamSentencesForHeldBackElement(ArdocoResult result, ArchitectureItem heldBackElement) {
        var artemisTraceabilityState = result.getArtemisConnectionState(new ArtemisTarget(Metamodel.ARCHITECTURE_WITH_COMPONENTS, NamedEntityType.COMPONENT));

        Set<String> sentencesMentioningHeldBackElement = artemisTraceabilityState.getTraceLinks()
                .stream()
                .filter(traceLink -> traceLink.getSecondEndpoint().getId().equals(heldBackElement.getId()))
                .map(tl -> tl.getFirstEndpoint().getSentenceNumber() + "," + tl.getSecondEndpoint().getName())
                .collect(Collectors.toSet());

        Set<String> unlinkedEntitySentences = artemisTraceabilityState.getUnlinkedNamedEntities()
                .stream()
                .map(NamedArchitectureEntity::getOccurrences)
                .flatMap(java.util.Collection::stream)
                .map(occ -> occ.getSentenceNumber() + "," + occ.getName())
                .collect(Collectors.toSet());

        Set<String> detectedSentences = new LinkedHashSet<>();
        detectedSentences.addAll(sentencesMentioningHeldBackElement);
        detectedSentences.addAll(unlinkedEntitySentences);

        return detectedSentences;
    }
}
