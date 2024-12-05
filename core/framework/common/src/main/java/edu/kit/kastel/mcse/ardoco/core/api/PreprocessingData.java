/* Licensed under MIT 2022-2024. */
package edu.kit.kastel.mcse.ardoco.core.api;

import edu.kit.kastel.mcse.ardoco.core.api.text.Text;
import edu.kit.kastel.mcse.ardoco.core.data.PipelineStepData;

/**
 * This class serves as container for different data after preprocessing to store as {@link PipelineStepData}. Right now, this includes the preprocessed
 * {@link Text} only.
 */
public class PreprocessingData implements PipelineStepData {
    private static final long serialVersionUID = 8103545017098419675L;

    public static final String ID = "PreprocessingData";

    private final Text preprocessedText;

    public PreprocessingData(Text preprocessedText) {
        this.preprocessedText = preprocessedText;
    }

    public Text getText() {
        return this.preprocessedText;
    }

}
