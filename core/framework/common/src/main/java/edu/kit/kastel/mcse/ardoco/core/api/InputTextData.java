/* Licensed under MIT 2023-2024. */
package edu.kit.kastel.mcse.ardoco.core.api;

import edu.kit.kastel.mcse.ardoco.core.data.PipelineStepData;

public class InputTextData implements PipelineStepData {

    private static final long serialVersionUID = -5404851121533249349L;

    public static final String ID = "InputTextData";

    private String text;

    public InputTextData(String text) {
        this.text = text;
    }

    public String getText() {
        return this.text;
    }
}
