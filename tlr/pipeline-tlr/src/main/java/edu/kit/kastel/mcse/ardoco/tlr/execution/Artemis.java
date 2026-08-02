package edu.kit.kastel.mcse.ardoco.tlr.execution;

import java.io.File;
import java.util.List;

import org.eclipse.collections.api.map.sorted.ImmutableSortedMap;
import org.jspecify.annotations.Nullable;

import edu.kit.kastel.mcse.ardoco.core.api.models.Metamodel;
import edu.kit.kastel.mcse.ardoco.core.common.util.CommonUtilities;
import edu.kit.kastel.mcse.ardoco.core.common.util.DataRepositoryHelper;
import edu.kit.kastel.mcse.ardoco.core.execution.runner.ArdocoRunner;
import edu.kit.kastel.mcse.ardoco.tlr.artemis.ArtemisConnectionGenerator;
import edu.kit.kastel.mcse.ardoco.tlr.artemis.ArtemisNer;
import edu.kit.kastel.mcse.ardoco.tlr.artemis.ArtemisPostprocessing;
import edu.kit.kastel.mcse.ardoco.tlr.artemis.ArtemisPreprocessing;
import edu.kit.kastel.mcse.ardoco.tlr.artemis.strategies.ClassArtemisNerStrategy;
import edu.kit.kastel.mcse.ardoco.tlr.artemis.strategies.ComponentArtemisNerStrategy;
import edu.kit.kastel.mcse.ardoco.tlr.models.agents.ArchitectureConfiguration;
import edu.kit.kastel.mcse.ardoco.tlr.models.agents.CodeConfiguration;
import edu.kit.kastel.mcse.ardoco.tlr.models.informants.LargeLanguageModel;

/**
 * ArTEMiS (Architecture Traceability with Entity Matching via Semantic inference) uses Named Entity Recognition to detect architecturally relevant entities in
 * text (e.g., components, classes, ...) to hunt trace links.
 */
public class Artemis extends ArdocoRunner {
    protected Artemis(String projectName) {
        super(projectName);
    }

    /**
     * Initializes Artemis with the given configuration (incl. output directory) and sets up the pipeline.
     *
     * @param inputText                 the input file containing the text to be processed
     * @param architectureConfiguration the configuration for the architecture model, allowing null if not required
     * @param codeConfiguration         the configuration for the code model, allowing null if not required
     * @param additionalConfigs         an immutable sorted map containing additional configurations as key-value pairs
     * @param outputDir                 the directory where the output will be saved
     * @param llm                       the large language model used, for example, for Named Entity Recognition (NER)
     */
    public void setUp(File inputText, @Nullable ArchitectureConfiguration architectureConfiguration, @Nullable CodeConfiguration codeConfiguration,
            ImmutableSortedMap<String, String> additionalConfigs, File outputDir, LargeLanguageModel llm) {
        setOutputDirectory(outputDir);
        setUp(inputText, architectureConfiguration, codeConfiguration, additionalConfigs, llm);
    }

    /**
     * Initializes Artemis with the given configuration (excl. output directory) and sets up the pipeline.
     *
     * @param inputText                 the input file containing the text to be processed
     * @param architectureConfiguration the configuration for the architecture model, allowing null if not required
     * @param codeConfiguration         the configuration for the code model, allowing null if not required
     * @param additionalConfigs         an immutable sorted map containing additional configurations as key-value pairs
     * @param llm                       the large language model used, for example, for Named Entity Recognition (NER)
     */
    public void setUp(File inputText, @Nullable ArchitectureConfiguration architectureConfiguration, @Nullable CodeConfiguration codeConfiguration,
            ImmutableSortedMap<String, String> additionalConfigs, LargeLanguageModel llm) {
        if ((architectureConfiguration != null && architectureConfiguration.metamodel() != null) || (codeConfiguration != null && codeConfiguration.metamodel() != null)) {
            throw new IllegalArgumentException("Metamodel shall not be set in configurations. The runner defines the metamodels.");
        }
        DataRepositoryHelper.putInputText(getArdoco().getDataRepository(), CommonUtilities.readInputText(inputText));
        definePipeline(architectureConfiguration, codeConfiguration, additionalConfigs, llm);
        isSetUp = true;
    }

    private void definePipeline(@Nullable ArchitectureConfiguration architectureConfiguration, @Nullable CodeConfiguration codeConfiguration,
            ImmutableSortedMap<String, String> additionalConfigs, LargeLanguageModel llm) {
        var dataRepository = getArdoco().getDataRepository();
        var strategies = List.of(new ComponentArtemisNerStrategy(), new ClassArtemisNerStrategy()); //further strategies can be added here

        getArdoco().addPipelineStep(ArtemisPreprocessing.get(additionalConfigs, dataRepository,
                architectureConfiguration != null ? architectureConfiguration.withMetamodel(Metamodel.ARCHITECTURE_WITH_COMPONENTS) : null,
                codeConfiguration != null ? codeConfiguration.withMetamodel(Metamodel.CODE_WITH_COMPILATION_UNITS) : null));

        getArdoco().addPipelineStep(ArtemisNer.get(additionalConfigs, dataRepository, llm, strategies));

        getArdoco().addPipelineStep(ArtemisConnectionGenerator.get(additionalConfigs, dataRepository, strategies));

        getArdoco().addPipelineStep(ArtemisPostprocessing.get(additionalConfigs, dataRepository));
    }

}
