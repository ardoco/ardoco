/* Licensed under MIT 2025-2026. */
package edu.kit.kastel.mcse.ardoco.id.tests.integration.inconsistencyhelper;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.collections.api.map.sorted.ImmutableSortedMap;

import edu.kit.kastel.mcse.ardoco.core.api.models.Metamodel;
import edu.kit.kastel.mcse.ardoco.core.api.models.ModelFormat;
import edu.kit.kastel.mcse.ardoco.core.api.models.architecture.ArchitectureComponent;
import edu.kit.kastel.mcse.ardoco.core.api.models.architecture.ArchitectureItem;
import edu.kit.kastel.mcse.ardoco.core.api.output.ArdocoResult;
import edu.kit.kastel.mcse.ardoco.core.common.util.CommonUtilities;
import edu.kit.kastel.mcse.ardoco.core.common.util.DataRepositoryHelper;
import edu.kit.kastel.mcse.ardoco.core.data.DataRepository;
import edu.kit.kastel.mcse.ardoco.core.execution.ConfigurationHelper;
import edu.kit.kastel.mcse.ardoco.core.execution.runner.AnonymousRunner;
import edu.kit.kastel.mcse.ardoco.core.pipeline.AbstractPipelineStep;
import edu.kit.kastel.mcse.ardoco.id.tests.tasks.InconsistencyDetectionTask;
import edu.kit.kastel.mcse.ardoco.tlr.connectiongenerator.ner.NerConnectionGenerator;
import edu.kit.kastel.mcse.ardoco.tlr.models.informants.LargeLanguageModel;
import edu.kit.kastel.mcse.ardoco.tlr.text.providers.SimpleTextPreprocessingAgent;

/**
 * Produces the inconsistency detection runs using Artemis.
 */
public class ArtemisHoldBackRunResultsProducer {
    protected File inputText;
    protected File inputModel;
    protected LargeLanguageModel llm;
    protected ImmutableSortedMap<String, String> additionalConfigs;

    public ArtemisHoldBackRunResultsProducer(LargeLanguageModel llm) {
        this.llm = llm;
    }

    /**
     * Produces the base run results by executing the baseline task for the specified gold standard project.
     *
     * @param goldStandardProject the project that should be run
     * @return an instance of ArdocoResult containing the results of the baseline run
     */
    public ArdocoResult produceBaseRunResults(InconsistencyDetectionTask goldStandardProject) {
        prepareDetectionInputs(goldStandardProject);

        HoldBackArCoTLModelProvider holdBackArCoTLModelProvider = new HoldBackArCoTLModelProvider(this.inputModel);
        return new ArdocoResult(this.run(goldStandardProject, holdBackArCoTLModelProvider));
    }

    /**
     * Runs Artemis multiple times to produce results. The first run calls Artemis normally, in further runs one element is held back each time (so that each
     * element was held back once). This way, we can simulate MEAT inconsistencies.
     *
     * @param goldStandardProject the project that should be run
     * @return a map containing the mapping from ModelElement that was held back to the DataStructure that was produced when running Artemis without the
     * ModelElement
     */
    public Map<ArchitectureItem, ArdocoResult> produceHoldBackRunResults(InconsistencyDetectionTask goldStandardProject) {
        Map<ArchitectureItem, ArdocoResult> runs = new LinkedHashMap<>();
        prepareDetectionInputs(goldStandardProject);

        HoldBackArCoTLModelProvider holdBackArCoTLModelProvider = new HoldBackArCoTLModelProvider(this.inputModel);

        DataRepository baseRunData = this.run(goldStandardProject, holdBackArCoTLModelProvider);
        runs.put(null, new ArdocoResult(baseRunData));

        for (int i = 0; i < holdBackArCoTLModelProvider.numberOfActualInstances(); i++) {
            holdBackArCoTLModelProvider.setCurrentHoldBackIndex(i);
            var currentHoldBack = holdBackArCoTLModelProvider.getCurrentHoldBack();
            DataRepository currentRunData = this.run(goldStandardProject, holdBackArCoTLModelProvider);
            var result = new ArdocoResult(currentRunData);
            runs.put(currentHoldBack, result);
            writeArtemisResultToFile(goldStandardProject, result, currentHoldBack);
        }

        return runs;
    }

    /**
     * Runs the part that is specific to each run.
     *
     * @param goldStandardProject            the current project
     * @param holdElementsBackModelConnector the model connector with the held-back model element
     * @return the data repository that is produced
     */
    protected DataRepository run(InconsistencyDetectionTask goldStandardProject, HoldBackArCoTLModelProvider holdElementsBackModelConnector) {
        return new AnonymousRunner(goldStandardProject.name()) {
            @Override
            public List<AbstractPipelineStep> initializePipelineSteps(DataRepository dataRepository) {
                var pipelineSteps = new ArrayList<AbstractPipelineStep>();

                var text = CommonUtilities.readInputText(inputText);
                if (text.isBlank()) {
                    throw new IllegalArgumentException("Cannot deal with empty input text. Maybe there was an error reading the file.");
                }
                DataRepositoryHelper.putInputText(dataRepository, text);

                pipelineSteps.add(SimpleTextPreprocessingAgent.get(additionalConfigs, dataRepository));
                pipelineSteps.add(holdElementsBackModelConnector.get(additionalConfigs, dataRepository));
                pipelineSteps.add(NerConnectionGenerator.get(additionalConfigs, dataRepository, llm));

                return pipelineSteps;
            }
        }.runWithoutSaving();
    }

    private void prepareDetectionInputs(InconsistencyDetectionTask goldStandardProject) {
        this.inputModel = goldStandardProject.getArchitectureModelFile(ModelFormat.PCM);
        this.inputText = goldStandardProject.getTextFile();
        this.additionalConfigs = ConfigurationHelper.loadAdditionalConfigs(goldStandardProject.getFilterConfigurationFile());
    }

    private static void writeArtemisResultToFile(InconsistencyDetectionTask goldStandardProject, ArdocoResult result, ArchitectureComponent currentHoldBack) {
        var outputPath = java.nio.file.Path.of("target", "testout", "ner_results", goldStandardProject.name());
        try {
            Files.createDirectories(outputPath);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        var nerConnectionState = result.getNerConnectionState(Metamodel.ARCHITECTURE_WITH_COMPONENTS);
        var sb = new StringBuilder();
        sb.append("### Named Architecture Entities ###").append(System.lineSeparator());
        for (var entity : nerConnectionState.getNamedArchitectureEntities()) {
            sb.append(entity).append(System.lineSeparator());
        }
        sb.append("### Trace Links ###").append(System.lineSeparator());
        for (var link : nerConnectionState.getTraceLinks()) {
            sb.append(link).append(System.lineSeparator());
        }

        var fileName = String.format("ner_results_%s.txt", currentHoldBack == null ? "base" : currentHoldBack.getName());
        var absolutePath = outputPath.resolve(fileName).toFile().getAbsolutePath();
        edu.kit.kastel.mcse.ardoco.core.common.util.FilePrinter.writeToFile(absolutePath, sb.toString());
    }
}
