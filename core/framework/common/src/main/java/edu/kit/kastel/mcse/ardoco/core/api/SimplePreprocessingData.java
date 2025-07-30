package edu.kit.kastel.mcse.ardoco.core.api;

import java.io.Serial;

import edu.kit.kastel.mcse.ardoco.core.api.text.SimpleText;
import edu.kit.kastel.mcse.ardoco.core.data.PipelineStepData;

public class SimplePreprocessingData implements PipelineStepData {
    @Serial
    private static final long serialVersionUID = 7302894013700626736L;

    public static final String ID = SimplePreprocessingData.class.getSimpleName();

    private final SimpleText preprocessedText;

    /**
     * Constructs a new PreprocessingData instance.
     *
     * @param preprocessedText the preprocessed text
     */
    public SimplePreprocessingData(SimpleText preprocessedText) {
        this.preprocessedText = preprocessedText;
    }

    /**
     * Returns the preprocessed text.
     *
     * @return the preprocessed text
     */
    public SimpleText getText() {
        return this.preprocessedText;
    }
}
