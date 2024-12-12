package edu.kit.kastel.mcse.ardoco.secdreqan;

import org.eclipse.collections.api.list.ImmutableList;

import edu.kit.kastel.mcse.ardoco.core.api.output.ArDoCoResult;
import edu.kit.kastel.mcse.ardoco.metrics.result.SingleClassificationResult;

public class RequirementTLREvaluationObject {

    private String id;

    private ArDoCoResult arDoCoResult;

    private ImmutableList<String> goldStandard;
    private SingleClassificationResult<String> evaluationResults;

    public RequirementTLREvaluationObject(String id, ImmutableList<String> goldStandard){
        this.id = id;
        this.goldStandard = goldStandard;
    }

    public void addArDoCoResult(ArDoCoResult arDoCoResult){
        this.arDoCoResult = arDoCoResult;
    }


    public void addEvalResults(SingleClassificationResult<String> evaluationResults) {
        this.evaluationResults = evaluationResults;
    }

    public String getId() {
        return id;
    }

    public ArDoCoResult getArDoCoResult() {
        return arDoCoResult;
    }

    public ImmutableList<String> getGoldStandard() {
        return goldStandard;
    }

    public SingleClassificationResult<String> getEvaluationResults(){return evaluationResults;}

}
