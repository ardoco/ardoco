package edu.kit.kastel.mcse.ardoco.tlr.execution;

import java.io.File;

import org.eclipse.collections.api.map.sorted.ImmutableSortedMap;
import org.jspecify.annotations.Nullable;

import edu.kit.kastel.mcse.ardoco.core.common.util.CommonUtilities;
import edu.kit.kastel.mcse.ardoco.core.common.util.DataRepositoryHelper;
import edu.kit.kastel.mcse.ardoco.core.execution.Ardoco;
import edu.kit.kastel.mcse.ardoco.core.execution.runner.ArdocoRunner;
import edu.kit.kastel.mcse.ardoco.tlr.connectiongenerator.ner.NerConnectionGenerator;
import edu.kit.kastel.mcse.ardoco.tlr.connectiongenerator.ner.informants.NerStrategy;
import edu.kit.kastel.mcse.ardoco.tlr.models.agents.ArchitectureConfiguration;
import edu.kit.kastel.mcse.ardoco.tlr.models.agents.CodeConfiguration;
import edu.kit.kastel.mcse.ardoco.tlr.models.agents.ModelProviderAgent;
import edu.kit.kastel.mcse.ardoco.tlr.models.informants.LargeLanguageModel;
import edu.kit.kastel.mcse.ardoco.tlr.text.providers.SimpleTextPreprocessingAgent;

/**
 * Abstract base class for ArTEMiS runners.
 * <p>
 * ArTEMiS (Architecture Traceability with Entity Matching via Semantic inference) uses Named Entity Recognition to detect architecturally relevant entities in
 * text (e.g., components, classes, ...) to hunt trace links.
 * </p>
 * <p>
 * Subclasses are responsible for providing the model provider pipeline step by implementing
 * {@link #addModelProviderPipelineStep(ImmutableSortedMap, LargeLanguageModel, ArchitectureConfiguration, CodeConfiguration)}. They may also override
 * {@link #addPostProcessingPipelineSteps(ImmutableSortedMap, LargeLanguageModel)} to append further processing steps.
 * </p>
 */
public abstract class AbstractArtemis extends ArdocoRunner {

    public AbstractArtemis(String projectName) {
        super(projectName);
    }

    public void setUp(File inputText, @Nullable ArchitectureConfiguration architectureConfiguration, @Nullable CodeConfiguration codeConfiguration,
            ImmutableSortedMap<String, String> additionalConfigs, File outputDir, LargeLanguageModel llmForNer) {
        //TODO use record ArtemisConfiguration?? andere nutzen das halt nicht...
        if ((architectureConfiguration != null && architectureConfiguration.metamodel() != null) || (codeConfiguration != null && codeConfiguration.metamodel() != null)) {
            throw new IllegalArgumentException("Metamodel shall not be set in configurations. The runner defines the metamodels.");
        }
        definePipeline(inputText, architectureConfiguration, codeConfiguration, additionalConfigs, llmForNer);
        setOutputDirectory(outputDir);
        isSetUp = true;
    }

    public void setUp(String inputTextLocation, @Nullable ArchitectureConfiguration architectureConfiguration, @Nullable CodeConfiguration codeConfiguration,
            ImmutableSortedMap<String, String> additionalConfigs, String outputDirectory, LargeLanguageModel llmForNer) {
        setUp(new File(inputTextLocation), architectureConfiguration, codeConfiguration, additionalConfigs, new File(outputDirectory), llmForNer);
    }

    private void definePipeline(File inputText, @Nullable ArchitectureConfiguration architectureConfiguration, @Nullable CodeConfiguration codeConfiguration,
            ImmutableSortedMap<String, String> additionalConfigs, LargeLanguageModel llmForNer) {
        Ardoco ardoco = this.getArdoco();

        addPreprocessingPipelineStep(inputText, additionalConfigs);

        addModelProviderPipelineStep(additionalConfigs, llmForNer, architectureConfiguration, codeConfiguration);

        addNerPipelineStep(additionalConfigs, llmForNer);

        addPostProcessingPipelineSteps(additionalConfigs, llmForNer);
    }

    private void addPreprocessingPipelineStep(File inputText, ImmutableSortedMap<String, String> additionalConfigs) {
        String text = CommonUtilities.readInputText(inputText);
        if (text.isBlank()) {
            throw new IllegalArgumentException("Cannot deal with empty input text. Maybe there was an error reading the file.");
        }
        DataRepositoryHelper.putInputText(this.getArdoco().getDataRepository(), text);
        this.getArdoco().addPipelineStep(SimpleTextPreprocessingAgent.get(additionalConfigs, this.getArdoco().getDataRepository()));
    }

    /**
     * Adds the model provider pipeline step.
     * <p>
     * Implementations are responsible for creating and registering the appropriate {@link ModelProviderAgent}.
     * </p>
     *
     * @param additionalConfigs         the additional configuration
     * @param llmForNer                 the large language model used for named entity recognition
     * @param architectureConfiguration the architecture model configuration, or {@code null}
     * @param codeConfiguration         the code model configuration, or {@code null}
     */
    protected abstract void addModelProviderPipelineStep(ImmutableSortedMap<String, String> additionalConfigs, LargeLanguageModel llmForNer,
            @Nullable ArchitectureConfiguration architectureConfiguration, @Nullable CodeConfiguration codeConfiguration);

    private void addNerPipelineStep(ImmutableSortedMap<String, String> additionalConfigs, LargeLanguageModel llmForNer) {
        NerConnectionGenerator nerConnectionGenerator = NerConnectionGenerator.get(additionalConfigs, this.getArdoco().getDataRepository(), llmForNer,
                getNerStrategy());
        this.getArdoco().addPipelineStep(nerConnectionGenerator);
    }

    /**
     * Returns the named entity recognition (NER) strategy used by this ArTEMiS runner.
     *
     * @return the NER strategy for this ArTEMiS runner
     */
    protected abstract NerStrategy getNerStrategy();

    /**
     * Adds optional post-processing pipeline steps.
     * <p>
     * The default implementation does nothing. Subclasses may override this method to append additional processing steps after named entity recognition.
     * </p>
     *
     * @param additionalConfigs the additional configuration
     * @param llmForNer         the large language model used for named entity recognition
     */
    protected void addPostProcessingPipelineSteps(ImmutableSortedMap<String, String> additionalConfigs, LargeLanguageModel llmForNer) {
        //base case
    }
}
