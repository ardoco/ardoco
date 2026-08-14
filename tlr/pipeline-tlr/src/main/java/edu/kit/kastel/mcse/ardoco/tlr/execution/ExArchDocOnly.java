/* Licensed under MIT 2025-2026. */
package edu.kit.kastel.mcse.ardoco.tlr.execution;

import java.io.File;

import org.eclipse.collections.api.map.sorted.ImmutableSortedMap;

import edu.kit.kastel.mcse.ardoco.core.common.util.CommonUtilities;
import edu.kit.kastel.mcse.ardoco.core.common.util.DataRepositoryHelper;
import edu.kit.kastel.mcse.ardoco.core.execution.Ardoco;
import edu.kit.kastel.mcse.ardoco.core.execution.runner.ArdocoRunner;
import edu.kit.kastel.mcse.ardoco.tlr.models.agents.LlmArchitectureProviderAgent;
import edu.kit.kastel.mcse.ardoco.tlr.models.informants.LargeLanguageModel;
import edu.kit.kastel.mcse.ardoco.tlr.models.informants.LlmArchitecturePrompt;

/**
 * ExArchDocOnly is a documentation-only variant of ExArch. It uses an LLM to extract component names solely from the
 * architecture documentation, without requiring a code model. The resulting component list forms a minimal "Simple
 * Software Architecture Model" (SSAM) and is stored in the DataRepository for downstream use or export.
 */
public class ExArchDocOnly extends ArdocoRunner {

    public ExArchDocOnly(String projectName) {
        super(projectName);
    }

    public void setUp(File inputText, ImmutableSortedMap<String, String> additionalConfigs, File outputDir, LargeLanguageModel largeLanguageModel,
            LlmArchitecturePrompt documentationExtractionPrompt) {
        definePipeline(inputText, additionalConfigs, largeLanguageModel, documentationExtractionPrompt);
        setOutputDirectory(outputDir);
        isSetUp = true;
    }

    private void definePipeline(File inputText, ImmutableSortedMap<String, String> additionalConfigs, LargeLanguageModel largeLanguageModel,
            LlmArchitecturePrompt documentationExtractionPrompt) {
        Ardoco arDoCo = this.getArdoco();
        var dataRepository = arDoCo.getDataRepository();

        var text = CommonUtilities.readInputText(inputText);
        if (text.isBlank()) {
            throw new IllegalArgumentException("Cannot deal with empty input text. Maybe there was an error reading the file.");
        }
        DataRepositoryHelper.putInputText(dataRepository, text);

        LlmArchitectureProviderAgent llmArchitectureProviderAgent = new LlmArchitectureProviderAgent(dataRepository, largeLanguageModel,
                documentationExtractionPrompt, null, LlmArchitecturePrompt.Features.PACKAGES, null);
        arDoCo.addPipelineStep(llmArchitectureProviderAgent);
    }
}
