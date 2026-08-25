package edu.kit.kastel.mcse.ardoco.id.tests.integration.inconsistencyhelper.artemis.runproducer;

import java.io.File;
import java.util.List;

import org.eclipse.collections.api.factory.SortedMaps;
import org.eclipse.collections.api.map.sorted.MutableSortedMap;

import edu.kit.kastel.mcse.ardoco.core.data.DataRepository;
import edu.kit.kastel.mcse.ardoco.id.execution.runner.ArtemisInconsistencyDetection;
import edu.kit.kastel.mcse.ardoco.id.tests.tasks.ArtemisInconsistencyTask;
import edu.kit.kastel.mcse.ardoco.tlr.artemis.strategies.ArtemisNerStrategy;
import edu.kit.kastel.mcse.ardoco.tlr.models.agents.ArchitectureConfiguration;
import edu.kit.kastel.mcse.ardoco.tlr.models.agents.CodeConfiguration;
import edu.kit.kastel.mcse.ardoco.tlr.models.informants.LargeLanguageModel;

final class ArtemisInconsistencyRunSupport {
    private ArtemisInconsistencyRunSupport() {
        throw new IllegalStateException("Utility class");
    }

    static DataRepository run(ArtemisInconsistencyTask project, LargeLanguageModel llm, List<ArtemisNerStrategy> strategies, String outputName,
            String runName) {
        File inputText = project.getTextFile();
        File outputDirectory = createOutputDirectory(project, outputName, runName);
        MutableSortedMap<String, String> additionalConfigs = SortedMaps.mutable.empty();

        ArchitectureConfiguration architectureConfiguration = project.getArchitectureConfiguration().orElse(null);
        CodeConfiguration codeConfiguration = project.getCodeConfiguration().orElse(null);

        var runner = new ArtemisInconsistencyDetection(project.getEvaluationProject().name());
        runner.setUp(inputText, architectureConfiguration, codeConfiguration, additionalConfigs.toImmutable(), outputDirectory, llm, strategies);

        return runner.runWithoutSaving();
    }

    static File createOutputDirectory(ArtemisInconsistencyTask project, String outputName, String runName) {
        File outputDirectory = new File("target/testout/artemis-id-runs/" + outputName + "/" + project.getEvaluationProject().name() + "/" + sanitize(runName));
        outputDirectory.mkdirs();
        return outputDirectory;
    }

    static String sanitize(String value) {
        return value.replaceAll("[^a-zA-Z0-9._-]", "_");
    }
}
