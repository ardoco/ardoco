package edu.kit.kastel.mcse.ardoco.secdreqan;

import java.io.File;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import edu.kit.kastel.mcse.ardoco.metrics.result.SingleClassificationResult;

import org.eclipse.collections.api.collection.ImmutableCollection;
import org.eclipse.collections.api.list.ImmutableList;
import org.junit.jupiter.api.Assertions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.kit.kastel.mcse.ardoco.core.api.models.ArchitectureModelType;
import edu.kit.kastel.mcse.ardoco.core.api.output.ArDoCoResult;
import edu.kit.kastel.mcse.ardoco.core.common.util.TraceLinkUtilities;
import edu.kit.kastel.mcse.ardoco.core.execution.ConfigurationHelper;
import edu.kit.kastel.mcse.ardoco.core.tests.TestUtil;
import edu.kit.kastel.mcse.ardoco.core.tests.eval.results.EvaluationResults;
import edu.kit.kastel.mcse.ardoco.core.tests.eval.results.ExpectedResults;
import edu.kit.kastel.mcse.ardoco.metrics.ClassificationMetricsCalculator;

public class SecDReqAnEvaluation {

    protected static final Logger logger = LoggerFactory.getLogger(SecDReqAnEvaluation.class);
    private MultiGoldStandardProject project = MultiGoldStandardProject.EVEREST;
    protected static final String OUTPUT = "target/testout-secdreqan-eval";


    protected final void runTraceLinkEvaluation(MultiGoldStandardProject project) {

        var additionalConfigs= ConfigurationHelper.loadAdditionalConfigs(project.getAdditionalConfigurationsFile());

        String name = project.getAlias();
        File inputModel = project.getModelFile();
        List<File> inputFiles = project.getRequirements();

        var runner = new SecDReqAnMultiRunner(name);
        Map<String, ArDoCoResult> resultMap = runner.varyTextkeepSAM(inputFiles, inputModel, ArchitectureModelType.PCM,additionalConfigs, OUTPUT);
        List<RequirementTLREvaluationObject> evaluationObjects = project.getTlRGoldStandards();

        this.addResultsToEvaluationObjects(evaluationObjects, resultMap);

        for(var evalObject: evaluationObjects){
            this.calculateSingleEvaluationResults(evalObject);
        }

        EvaluationResults<String> evaluationOverallResults = accumulateEvaluationResultsOverAllRequirementTLRs(evaluationObjects);


        ExpectedResults expectedOverallResults = project.getExpectedTraceLinkResults();
        TestUtil.logExtendedResultsWithExpected(SecDReqAnEvaluation.logger, this, project.getAlias(), evaluationOverallResults, expectedOverallResults);
        this.compareResults(evaluationOverallResults, expectedOverallResults);
    }

    private EvaluationResults<String> accumulateEvaluationResultsOverAllRequirementTLRs(List<RequirementTLREvaluationObject> evaluationObjects) {

        List<SingleClassificationResult<String>> evaluationResults = evaluationObjects.stream().map(e -> e.getEvaluationResults()).collect(Collectors.toList());

        // TODO: Aggregate Results
        //<String>  = ResultCalculatorUtil.calculateMacroAverageResults(Lists.immutable.ofAll(evaluationResults));


        return overallResult;
    }

    private void addResultsToEvaluationObjects(List<RequirementTLREvaluationObject> evaluationObjects, Map<String, ArDoCoResult> resultMap) {

        for(var evaluationObject : evaluationObjects){
            String id = evaluationObject.getId();
            if (! resultMap.containsKey(id)){
                throw new IllegalStateException("Requirement ids of results and goldstandard do not match.");
            }
            evaluationObject.addArDoCoResult(resultMap.get(id));
            resultMap.remove(id);
        }
        if(!resultMap.isEmpty()){
            throw new IllegalStateException("Requirement ids of results and goldstandard do not match.");
        }
    }

    protected void calculateSingleEvaluationResults(RequirementTLREvaluationObject evalObject) {
        ArDoCoResult arDoCoResult = evalObject.getArDoCoResult();
        ImmutableCollection<String> goldStandard = evalObject.getGoldStandard();
        ImmutableList<String> results = TraceLinkUtilities.getSadSamTraceLinksAsStringList(arDoCoResult.getAllTraceLinks());
        Assertions.assertFalse(results.isEmpty());

        Set<String> distinctTraceLinks = new LinkedHashSet<>(results.castToCollection());
        Set<String> distinctGoldStandard = new LinkedHashSet<>(goldStandard.castToCollection());
        int confusionMatrixSum = this.getConfusionMatrixSum(arDoCoResult);

        var calculator = ClassificationMetricsCalculator.getInstance();
        var classification = calculator.calculateMetrics(distinctTraceLinks, distinctGoldStandard, confusionMatrixSum);
        evalObject.addEvalResults(classification);
    }

    protected int getConfusionMatrixSum(ArDoCoResult arDoCoResult) {
        int sentences = arDoCoResult.getText().getSentences().size();
        int modelElements = 0;
        for (var model : arDoCoResult.getModelIds()) {
            modelElements += arDoCoResult.getModelState(model).getInstances().size();
        }

        return sentences * modelElements;
    }

    protected void compareResults(EvaluationResults<String> results, ExpectedResults expectedResults) {
        Assertions.assertAll(//
                () -> Assertions.assertTrue(results.precision() >= expectedResults.precision(), "Precision " + results
                        .precision() + " is below the expected minimum value " + expectedResults.precision()), //
                () -> Assertions.assertTrue(results.recall() >= expectedResults.recall(), "Recall " + results
                        .recall() + " is below the expected minimum value " + expectedResults.recall()), //
                () -> Assertions.assertTrue(results.f1() >= expectedResults.f1(), "F1 " + results
                        .f1() + " is below the expected minimum value " + expectedResults.f1()), () -> Assertions.assertTrue(results
                        .accuracy() >= expectedResults.accuracy(), "Accuracy " + results
                        .accuracy() + " is below the expected minimum value " + expectedResults.accuracy()), //
                () -> Assertions.assertTrue(results.phiCoefficient() >= expectedResults.phiCoefficient(), "Phi coefficient " + results
                        .phiCoefficient() + " is below the expected minimum value " + expectedResults.phiCoefficient()));
    }

}
