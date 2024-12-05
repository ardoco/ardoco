/* Licensed under MIT 2022-2023. */
package edu.kit.kastel.mcse.ardoco.core.data.impl;

import edu.kit.kastel.mcse.ardoco.core.data.PipelineStepData;

/**
 * Example {@link PipelineStepData}
 */
public class ResultData implements PipelineStepData {
    private static final long serialVersionUID = 9183617106768927240L;
    private String result = null;

    public String getResult() {
        return this.result;
    }

    public void setResult(String result) {
        this.result = result;
    }
}
