package edu.kit.kastel.mcse.ardoco.tlr.execution;

import java.io.File;

import org.eclipse.collections.api.map.sorted.ImmutableSortedMap;

import edu.kit.kastel.mcse.ardoco.core.api.models.Metamodel;
import edu.kit.kastel.mcse.ardoco.core.common.util.CommonUtilities;
import edu.kit.kastel.mcse.ardoco.core.common.util.DataRepositoryHelper;
import edu.kit.kastel.mcse.ardoco.core.data.DataRepository;
import edu.kit.kastel.mcse.ardoco.core.execution.runner.ArDoCoRunner;
import edu.kit.kastel.mcse.ardoco.tlr.connectiongenerator.ner.NerConnectionGenerator;
import edu.kit.kastel.mcse.ardoco.tlr.models.agents.ArCoTLModelProviderAgent;
import edu.kit.kastel.mcse.ardoco.tlr.models.agents.ArchitectureConfiguration;
import edu.kit.kastel.mcse.ardoco.tlr.text.providers.SimpleTextPreprocessingAgent;

/**
 * ArTEMiS (Architecture Traceability with Entity Matching via Semantic inference) uses Named Entity Recognition to detect architecturally relevent entities in
 * text (i.e., components) to hunt trace links.
 */
public class Artemis extends ArDoCoRunner {

    public Artemis(String projectName) {
        super(projectName);
    }

    public void setUp(File inputText, ArchitectureConfiguration architectureConfiguration, ImmutableSortedMap<String, String> additionalConfigs,
            File outputDir) {
        if (architectureConfiguration.metamodel() != null) {
            throw new IllegalArgumentException("Metamodel shall not be set in configurations. The runner defines the metamodels.");
        }
        definePipeline(inputText, architectureConfiguration, additionalConfigs);
        setOutputDirectory(outputDir);
        isSetUp = true;
    }

    public void setUp(String inputTextLocation, ArchitectureConfiguration architectureConfiguration, ImmutableSortedMap<String, String> additionalConfigs,
            String outputDirectory) {
        setUp(new File(inputTextLocation), architectureConfiguration, additionalConfigs, new File(outputDirectory));
    }

    private void definePipeline(File inputText, ArchitectureConfiguration architectureConfiguration, ImmutableSortedMap<String, String> additionalConfigs) {
        DataRepository dataRepository = this.getArDoCo().getDataRepository();
        String text = CommonUtilities.readInputText(inputText);
        if (text.isBlank()) {
            throw new IllegalArgumentException("Cannot deal with empty input text. Maybe there was an error reading the file.");
        }
        DataRepositoryHelper.putInputText(dataRepository, text);
        this.getArDoCo().addPipelineStep(SimpleTextPreprocessingAgent.get(additionalConfigs, dataRepository));

        ArchitectureConfiguration architectureConfigurationWithMetamodel = architectureConfiguration.withMetamodel(Metamodel.ARCHITECTURE_WITH_COMPONENTS);
        ArCoTLModelProviderAgent modelProviderAgent = ArCoTLModelProviderAgent.getArCoTLModelProviderAgent(dataRepository, additionalConfigs,
                architectureConfigurationWithMetamodel, null);
        this.getArDoCo().addPipelineStep(modelProviderAgent);

        NerConnectionGenerator nerConnectionGenerator = NerConnectionGenerator.get(additionalConfigs, dataRepository);
        this.getArDoCo().addPipelineStep(nerConnectionGenerator);
    }
}
