package edu.kit.kastel.mcse.ardoco.id.execution.runner;

import java.io.File;
import java.util.List;

import org.eclipse.collections.api.map.sorted.ImmutableSortedMap;
import org.jspecify.annotations.Nullable;

import edu.kit.kastel.mcse.ardoco.core.api.models.Metamodel;
import edu.kit.kastel.mcse.ardoco.core.common.util.CommonUtilities;
import edu.kit.kastel.mcse.ardoco.core.common.util.DataRepositoryHelper;
import edu.kit.kastel.mcse.ardoco.core.data.DataRepository;
import edu.kit.kastel.mcse.ardoco.core.execution.runner.ArdocoRunner;
import edu.kit.kastel.mcse.ardoco.id.ArtemisInconsistencyChecker;
import edu.kit.kastel.mcse.ardoco.tlr.artemis.ArtemisConnectionGenerator;
import edu.kit.kastel.mcse.ardoco.tlr.artemis.ArtemisNer;
import edu.kit.kastel.mcse.ardoco.tlr.artemis.ArtemisPostprocessing;
import edu.kit.kastel.mcse.ardoco.tlr.artemis.ArtemisPreprocessing;
import edu.kit.kastel.mcse.ardoco.tlr.artemis.strategies.ArtemisNerStrategy;
import edu.kit.kastel.mcse.ardoco.tlr.models.agents.ArchitectureConfiguration;
import edu.kit.kastel.mcse.ardoco.tlr.models.agents.CodeConfiguration;
import edu.kit.kastel.mcse.ardoco.tlr.models.informants.LargeLanguageModel;

public class ArtemisInconsistencyDetection extends ArdocoRunner {

    public ArtemisInconsistencyDetection(String projectName) {
        super(projectName);
    }

    public void setUp(File inputText, @Nullable ArchitectureConfiguration architectureConfiguration, @Nullable CodeConfiguration codeConfiguration,
            ImmutableSortedMap<String, String> additionalConfigs, File outputDir, LargeLanguageModel llm, List<ArtemisNerStrategy> strategies) {
        setOutputDirectory(outputDir);
        setUp(inputText, architectureConfiguration, codeConfiguration, additionalConfigs, llm, strategies);
    }

    public void setUp(File inputText, @Nullable ArchitectureConfiguration architectureConfiguration, @Nullable CodeConfiguration codeConfiguration,
            ImmutableSortedMap<String, String> additionalConfigs, LargeLanguageModel llm, List<ArtemisNerStrategy> strategies) {
        if ((architectureConfiguration != null && architectureConfiguration.metamodel() != null) || (codeConfiguration != null && codeConfiguration.metamodel() != null)) {
            throw new IllegalArgumentException("Metamodel shall not be set in configurations. The runner defines the metamodels.");
        }

        var text = CommonUtilities.readInputText(inputText);
        if (text.isBlank()) {
            throw new IllegalArgumentException("Cannot deal with empty input text. Maybe there was an error reading the file.");
        }

        DataRepository dataRepository = getArdoco().getDataRepository();
        DataRepositoryHelper.putInputText(dataRepository, text);

        definePipeline(architectureConfiguration, codeConfiguration, additionalConfigs, llm, strategies);
        isSetUp = true;
    }

    private void definePipeline(@Nullable ArchitectureConfiguration architectureConfiguration, @Nullable CodeConfiguration codeConfiguration,
            ImmutableSortedMap<String, String> additionalConfigs, LargeLanguageModel llm, List<ArtemisNerStrategy> strategies) {
        var dataRepository = getArdoco().getDataRepository();

        var architectureConfigWithMetamodel = architectureConfiguration == null ?
                null :
                architectureConfiguration.withMetamodel(Metamodel.ARCHITECTURE_WITH_COMPONENTS);
        var codeConfigWithMetamodel = codeConfiguration == null ? null : codeConfiguration.withMetamodel(Metamodel.CODE_WITH_COMPILATION_UNITS);

        getArdoco().addPipelineStep(ArtemisPreprocessing.get(additionalConfigs, dataRepository, architectureConfigWithMetamodel, codeConfigWithMetamodel));
        getArdoco().addPipelineStep(ArtemisNer.get(additionalConfigs, dataRepository, llm, strategies));
        getArdoco().addPipelineStep(ArtemisConnectionGenerator.get(additionalConfigs, dataRepository, strategies));
        getArdoco().addPipelineStep(ArtemisPostprocessing.get(additionalConfigs, dataRepository));
        getArdoco().addPipelineStep(ArtemisInconsistencyChecker.get(additionalConfigs, dataRepository, strategies));
    }
}
