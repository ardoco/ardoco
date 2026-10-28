/* Licensed under MIT 2026. */
package edu.kit.kastel.mcse.ardoco.id.execution.runner;

import java.io.File;

import org.eclipse.collections.api.factory.SortedMaps;
import org.eclipse.collections.api.map.sorted.ImmutableSortedMap;

import edu.kit.kastel.mcse.ardoco.core.api.models.Metamodel;
import edu.kit.kastel.mcse.ardoco.core.common.util.CommonUtilities;
import edu.kit.kastel.mcse.ardoco.core.common.util.DataRepositoryHelper;
import edu.kit.kastel.mcse.ardoco.core.data.DataRepository;
import edu.kit.kastel.mcse.ardoco.core.execution.Ardoco;
import edu.kit.kastel.mcse.ardoco.core.execution.runner.ArdocoRunner;
import edu.kit.kastel.mcse.ardoco.id.ArchitecturalStylePatternInconsistencyChecker;
import edu.kit.kastel.mcse.ardoco.id.ArchitecturalStylePatternNerStrategy;
import edu.kit.kastel.mcse.ardoco.tlr.artemis.ArtemisNer;
import edu.kit.kastel.mcse.ardoco.tlr.models.agents.CodeConfiguration;
import edu.kit.kastel.mcse.ardoco.tlr.models.agents.ModelProviderAgent;
import edu.kit.kastel.mcse.ardoco.tlr.models.informants.LargeLanguageModel;
import edu.kit.kastel.mcse.ardoco.tlr.text.providers.SimpleTextPreprocessingAgent;

public class ArchitecturalStylePatternInconsistencyDetection extends ArdocoRunner {

    public ArchitecturalStylePatternInconsistencyDetection(String projectName) {
        super(projectName);
    }

    public void setUp(File inputText, CodeConfiguration codeConfiguration, ImmutableSortedMap<String, String> additionalConfigs, File outputDir,
            LargeLanguageModel llmForNer, LargeLanguageModel validationAgentLlm) {
        setOutputDirectory(outputDir);
        setUp(inputText, codeConfiguration, additionalConfigs, llmForNer, validationAgentLlm);
    }

    public void setUp(File inputText, CodeConfiguration codeConfiguration, ImmutableSortedMap<String, String> additionalConfigs, LargeLanguageModel llmForNer,
            LargeLanguageModel validationAgentLlm) {
        var text = CommonUtilities.readInputText(inputText);
        if (text.isBlank()) {
            throw new IllegalArgumentException("Cannot deal with empty input text. Maybe there was an error reading the file.");
        }

        DataRepository dataRepository = getArdoco().getDataRepository();
        DataRepositoryHelper.putInputText(dataRepository, text);

        definePipeline(codeConfiguration, llmForNer, additionalConfigs, validationAgentLlm);
        isSetUp = true;
    }

    private void definePipeline(CodeConfiguration codeConfiguration, LargeLanguageModel llmForNer, ImmutableSortedMap<String, String> additionalConfigs,
            LargeLanguageModel validationAgentLlm) {
        Ardoco ardoco = getArdoco();
        var dataRepository = ardoco.getDataRepository();
        var nerStrategy = new ArchitecturalStylePatternNerStrategy();

        ardoco.addPipelineStep(SimpleTextPreprocessingAgent.get(additionalConfigs, dataRepository));
        ardoco.addPipelineStep(ArtemisNer.get(additionalConfigs, dataRepository, llmForNer, java.util.List.of(nerStrategy)));

        switch (codeConfiguration.type()) {
            case ACM_FILE -> {
                ardoco.addPipelineStep(ModelProviderAgent.getModelProviderAgent(dataRepository, SortedMaps.immutable.empty(), null, codeConfiguration
                        .withMetamodel(Metamodel.CODE_WITH_COMPILATION_UNITS)));
                ardoco.addPipelineStep(ArchitecturalStylePatternInconsistencyChecker.getForCodeModel(additionalConfigs, dataRepository, nerStrategy,
                        validationAgentLlm));
            }
            case DIRECTORY -> ardoco.addPipelineStep(ArchitecturalStylePatternInconsistencyChecker.getForCodeDirectory(additionalConfigs, dataRepository,
                    nerStrategy, codeConfiguration.code().getAbsolutePath(), validationAgentLlm));
            default -> throw new IllegalArgumentException(
                    "Unsupported code configuration type for architectural style/pattern inconsistency detection: " + codeConfiguration.type());
        }
    }
}
