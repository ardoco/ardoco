package edu.kit.kastel.mcse.ardoco.id.execution.runner;

import java.io.File;
import java.util.List;

import org.eclipse.collections.api.factory.SortedMaps;
import org.eclipse.collections.api.map.sorted.ImmutableSortedMap;

import edu.kit.kastel.mcse.ardoco.core.api.models.Metamodel;
import edu.kit.kastel.mcse.ardoco.core.common.util.CommonUtilities;
import edu.kit.kastel.mcse.ardoco.core.common.util.DataRepositoryHelper;
import edu.kit.kastel.mcse.ardoco.core.data.DataRepository;
import edu.kit.kastel.mcse.ardoco.core.execution.runner.ArdocoRunner;
import edu.kit.kastel.mcse.ardoco.id.ProgrammingLanguageInconsistencyChecker;
import edu.kit.kastel.mcse.ardoco.id.ProgrammingLanguageNerStrategy;
import edu.kit.kastel.mcse.ardoco.tlr.artemis.ArtemisNer;
import edu.kit.kastel.mcse.ardoco.tlr.models.agents.CodeConfiguration;
import edu.kit.kastel.mcse.ardoco.tlr.models.agents.ModelProviderAgent;
import edu.kit.kastel.mcse.ardoco.tlr.models.informants.LargeLanguageModel;
import edu.kit.kastel.mcse.ardoco.tlr.text.providers.SimpleTextPreprocessingAgent;

public class ProgrammingLanguageInconsistencyDetection extends ArdocoRunner {

    public ProgrammingLanguageInconsistencyDetection(String projectName) {
        super(projectName);
    }

    public void setUp(File inputText, CodeConfiguration codeConfiguration, ImmutableSortedMap<String, String> additionalConfigs, File outputDir,
            LargeLanguageModel llmForNer) {
        setOutputDirectory(outputDir);
        setUp(inputText, codeConfiguration, additionalConfigs, llmForNer);
    }

    public void setUp(File inputText, CodeConfiguration codeConfiguration, ImmutableSortedMap<String, String> additionalConfigs, LargeLanguageModel llmForNer) {
        var text = CommonUtilities.readInputText(inputText);
        if (text.isBlank()) {
            throw new IllegalArgumentException("Cannot deal with empty input text. Maybe there was an error reading the file.");
        }

        DataRepository dataRepository = getArdoco().getDataRepository();
        DataRepositoryHelper.putInputText(dataRepository, text);

        definePipeline(codeConfiguration, llmForNer, additionalConfigs);
        isSetUp = true;
    }

    private void definePipeline(CodeConfiguration codeConfiguration, LargeLanguageModel llmForNer, ImmutableSortedMap<String, String> additionalConfigs) {
        var dataRepository = getArdoco().getDataRepository();
        var nerStrategy = new ProgrammingLanguageNerStrategy();

        getArdoco().addPipelineStep(SimpleTextPreprocessingAgent.get(additionalConfigs, dataRepository));
        getArdoco().addPipelineStep(ArtemisNer.get(additionalConfigs, dataRepository, llmForNer, List.of(nerStrategy)));

        switch (codeConfiguration.type()) {
        case CodeConfiguration.CodeConfigurationType.ACM_FILE -> {
            getArdoco().addPipelineStep(ModelProviderAgent.getModelProviderAgent(dataRepository, SortedMaps.immutable.empty(), null,
                    codeConfiguration.withMetamodel(Metamodel.CODE_WITH_COMPILATION_UNITS)));
            getArdoco().addPipelineStep(ProgrammingLanguageInconsistencyChecker.getForCodeModel(additionalConfigs, dataRepository, nerStrategy));
        }
        case CodeConfiguration.CodeConfigurationType.DIRECTORY -> getArdoco().addPipelineStep(
                ProgrammingLanguageInconsistencyChecker.getForCodeDirectory(additionalConfigs, dataRepository, nerStrategy,
                        codeConfiguration.code().getAbsolutePath()));
        default -> throw new IllegalArgumentException(
                "Unsupported code configuration type for programming language inconsistency detection: " + codeConfiguration.type());
        }

    }
}
