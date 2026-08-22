package edu.kit.kastel.mcse.ardoco.tlr.tests.integration.evaluation;

import java.io.File;
import java.util.List;

import org.eclipse.collections.api.factory.SortedMaps;
import org.eclipse.collections.api.set.sorted.MutableSortedSet;

import edu.kit.kastel.mcse.ardoco.core.execution.runner.ArdocoRunner;
import edu.kit.kastel.mcse.ardoco.core.api.stage.connectiongenerator.artemis.ArtemisConnectionState;
import edu.kit.kastel.mcse.ardoco.tlr.artemis.strategies.ArtemisNerStrategy;
import edu.kit.kastel.mcse.ardoco.tlr.artemis.strategies.ClassArtemisNerStrategy;
import edu.kit.kastel.mcse.ardoco.tlr.models.agents.CodeConfiguration;
import edu.kit.kastel.mcse.ardoco.tlr.models.informants.LargeLanguageModel;
import edu.kit.kastel.mcse.ardoco.tlr.tests.approach.ArtemisEvaluationProject;

public class ClassArtemisEvaluation extends AbstractArtemisEvaluation {

    private final ArtemisNerStrategy strategy = new ClassArtemisNerStrategy();

    public ClassArtemisEvaluation(ArtemisEvaluationProject project, LargeLanguageModel llmForNer) {
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
        File outputDirectory = new File("target", projectName + "-class-artemis-output");
        outputDirectory.mkdirs();

        CodeConfiguration codeConfiguration = new CodeConfiguration(project.getTlrTask().getEvaluationProject().getCodeModelFromResources(),
                CodeConfiguration.CodeConfigurationType.ACM_FILE);

        return ArtemisEvaluationRunnerFactory.createRunner(projectName, documentationFile, null, codeConfiguration, SortedMaps.immutable.empty(),
                outputDirectory, llmForNer, List.of(strategy));
    }

    @Override
    protected MutableSortedSet<String> getTraceLinksAsStrings(ArtemisConnectionState state) {
        return state.getTraceLinks()
                .stream()
                .map(tl -> tl.getFirstEndpoint().getSentenceNumber() + " -> " + tl.getSecondEndpoint().toString().toLowerCase())
                .collect(org.eclipse.collections.impl.collector.Collectors2.toSortedSet());
    }
}
