package edu.kit.kastel.mcse.ardoco.tlr.tests.integration.evaluation;

import java.io.File;
import java.util.List;

import org.eclipse.collections.api.factory.SortedMaps;
import org.eclipse.collections.api.set.sorted.MutableSortedSet;

import edu.kit.kastel.mcse.ardoco.core.api.models.ModelFormat;
import edu.kit.kastel.mcse.ardoco.core.execution.runner.ArdocoRunner;
import edu.kit.kastel.mcse.ardoco.tlr.artemis.states.ArtemisTraceabilityState;
import edu.kit.kastel.mcse.ardoco.tlr.artemis.strategies.ArtemisNerStrategy;
import edu.kit.kastel.mcse.ardoco.tlr.artemis.strategies.ComponentArtemisNerStrategy;
import edu.kit.kastel.mcse.ardoco.tlr.models.agents.ArchitectureConfiguration;
import edu.kit.kastel.mcse.ardoco.tlr.models.informants.LargeLanguageModel;
import edu.kit.kastel.mcse.ardoco.tlr.tests.approach.ArtemisEvaluationProject;

public class ComponentArtemisEvaluation extends AbstractArtemisEvaluation {

    private final ArtemisNerStrategy strategy = new ComponentArtemisNerStrategy();

    public ComponentArtemisEvaluation(ArtemisEvaluationProject project, LargeLanguageModel llmForNer) {
        super(project, llmForNer);
    }

    @Override
    protected ArtemisNerStrategy getStrategy() {
        return strategy;
    }

    @Override
    protected ArdocoRunner createArtemisRunner() {
        String projectName = project.getName();
        File documentationFile = project.getTlrTask().getEvaluationProject().getTextFile();
        File outputDirectory = new File("target", projectName + "-component-artemis-output");
        outputDirectory.mkdirs();

        ModelFormat architectureModelFormat = ModelFormat.PCM;
        ArchitectureConfiguration architectureConfiguration = new ArchitectureConfiguration(
                project.getTlrTask().getEvaluationProject().getArchitectureModel(architectureModelFormat), architectureModelFormat);

        return ArtemisEvaluationRunnerFactory.createRunner(projectName, documentationFile, architectureConfiguration, null, SortedMaps.immutable.empty(),
                outputDirectory, llmForNer, List.of(strategy));
    }

    @Override
    protected MutableSortedSet<String> getTraceLinksAsStrings(ArtemisTraceabilityState state) {
        return state.getTraceLinks()
                .stream()
                .map(tl -> tl.getFirstEndpoint().getSentenceNumber() + " -> " + tl.getSecondEndpoint().getId())
                .collect(org.eclipse.collections.impl.collector.Collectors2.toSortedSet());
    }
}
