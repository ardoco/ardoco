/* Licensed under MIT 2023-2024. */
package edu.kit.kastel.mcse.ardoco.tlr.tests.integration;

import static edu.kit.kastel.mcse.ardoco.tlr.tests.integration.TraceLinkEvaluationIT.OUTPUT;

import java.io.File;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import org.eclipse.collections.api.collection.ImmutableCollection;
import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ImmutableList;

import edu.kit.kastel.mcse.ardoco.core.api.models.CodeModelType;
import edu.kit.kastel.mcse.ardoco.core.api.models.ModelStates;
import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.Model;
import edu.kit.kastel.mcse.ardoco.core.api.output.ArDoCoResult;
import edu.kit.kastel.mcse.ardoco.core.api.text.Text;
import edu.kit.kastel.mcse.ardoco.core.common.util.DataRepositoryHelper;
import edu.kit.kastel.mcse.ardoco.core.common.util.TraceLinkUtilities;
import edu.kit.kastel.mcse.ardoco.core.data.DataRepository;
import edu.kit.kastel.mcse.ardoco.core.execution.runner.ArDoCoRunner;
import edu.kit.kastel.mcse.ardoco.core.tests.eval.CodeProject;
import edu.kit.kastel.mcse.ardoco.core.tests.eval.results.EvaluationResults;
import edu.kit.kastel.mcse.ardoco.core.tests.eval.results.ExpectedResults;
import edu.kit.kastel.mcse.ardoco.metrics.ClassificationMetricsCalculator;
import edu.kit.kastel.mcse.ardoco.tlr.execution.ArDoCoForSadSamViaLlmCodeTraceabilityLinkRecovery;
import edu.kit.kastel.mcse.ardoco.tlr.models.informants.LLMArchitecturePrompt;
import edu.kit.kastel.mcse.ardoco.tlr.models.informants.LargeLanguageModel;

class SadSamViaLlmCodeTraceabilityLinkRecoveryEvaluation extends TraceabilityLinkRecoveryEvaluation<CodeProject> {
    private final boolean acmFile;
    private final LargeLanguageModel largeLanguageModel;
    private final LLMArchitecturePrompt documentationExtractionPrompt;
    private final LLMArchitecturePrompt codeExtractionPrompt;
    private final LLMArchitecturePrompt aggregationPrompt;
    private final LLMArchitecturePrompt.Features codeFeatures;

    public SadSamViaLlmCodeTraceabilityLinkRecoveryEvaluation(boolean acmFile, LargeLanguageModel largeLanguageModel,
            LLMArchitecturePrompt documentationExtractionPrompt, LLMArchitecturePrompt codeExtractionPrompt, LLMArchitecturePrompt.Features codeFeatures,
            LLMArchitecturePrompt aggregationPrompt) {
        super();
        this.acmFile = acmFile;
        this.largeLanguageModel = largeLanguageModel;
        this.documentationExtractionPrompt = documentationExtractionPrompt;
        this.codeExtractionPrompt = codeExtractionPrompt;
        this.codeFeatures = codeFeatures;
        this.aggregationPrompt = aggregationPrompt;
    }

    @Override
    protected EvaluationResults<String> calculateEvaluationResults(ArDoCoResult arDoCoResult, ImmutableCollection<String> goldStandard) {
        // Disable asserts here ..
        ImmutableList<String> results = createTraceLinkStringList(arDoCoResult);

        Set<String> distinctTraceLinks = new LinkedHashSet<>(results.castToCollection());
        Set<String> distinctGoldStandard = new LinkedHashSet<>(goldStandard.castToCollection());
        int confusionMatrixSum = getConfusionMatrixSum(arDoCoResult);

        var calculator = ClassificationMetricsCalculator.getInstance();
        var classification = calculator.calculateMetrics(distinctTraceLinks, distinctGoldStandard, confusionMatrixSum);
        return new EvaluationResults<>(classification);
    }

    @Override
    protected void compareResults(EvaluationResults<String> results, ExpectedResults expectedResults) {
        // Disable Asserts. We want to see all results.
    }

    @Override
    protected boolean resultHasRequiredData(ArDoCoResult arDoCoResult) {
        var traceLinks = arDoCoResult.getSadCodeTraceLinks();
        return !traceLinks.isEmpty();
    }

    @Override
    protected ArDoCoRunner getAndSetupRunner(CodeProject codeProject) {
        String name = codeProject.name().toLowerCase();
        File textInput = codeProject.getTextFile();
        File inputCode = getInputCode(codeProject, acmFile);
        SortedMap<String, String> additionalConfigsMap = new TreeMap<>();
        File outputDir = new File(OUTPUT);

        var runner = new ArDoCoForSadSamViaLlmCodeTraceabilityLinkRecovery(name);
        runner.setUp(textInput, inputCode, additionalConfigsMap, outputDir, largeLanguageModel, documentationExtractionPrompt, codeExtractionPrompt,
                codeFeatures, aggregationPrompt);
        return runner;
    }

    @Override
    protected ImmutableList<String> createTraceLinkStringList(ArDoCoResult arDoCoResult) {
        var traceLinks = arDoCoResult.getSadCodeTraceLinks();

        return TraceLinkUtilities.getSadCodeTraceLinksAsStringList(Lists.immutable.ofAll(traceLinks));
    }

    @Override
    protected ImmutableList<String> getGoldStandard(CodeProject codeProject) {
        return codeProject.getSadCodeGoldStandard();
    }

    @Override
    protected ImmutableList<String> enrollGoldStandard(ImmutableList<String> goldStandard, ArDoCoResult result) {
        return enrollGoldStandardForCode(goldStandard, result);
    }

    @Override
    protected ExpectedResults getExpectedResults(CodeProject codeProject) {
        return codeProject.getExpectedResultsForSadSamCode();
    }

    @Override
    protected int getConfusionMatrixSum(ArDoCoResult arDoCoResult) {
        DataRepository dataRepository = arDoCoResult.dataRepository();

        Text text = DataRepositoryHelper.getAnnotatedText(dataRepository);
        int sentences = text.getSentences().size();

        ModelStates modelStatesData = DataRepositoryHelper.getModelStatesData(dataRepository);
        Model codeModel = modelStatesData.getModel(CodeModelType.CODE_MODEL.getModelId());
        var codeModelEndpoints = codeModel.getEndpoints().size();

        return sentences * codeModelEndpoints;
    }
}
