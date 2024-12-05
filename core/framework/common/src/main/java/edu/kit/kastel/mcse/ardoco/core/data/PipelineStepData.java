/* Licensed under MIT 2022-2024. */
package edu.kit.kastel.mcse.ardoco.core.data;

import java.io.Serializable;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.kit.kastel.mcse.ardoco.core.common.JsonHandling;

/**
 * This abstract class defines data that is used for the pipeline steps.
 */
public interface PipelineStepData extends Serializable {

    Logger logger = LoggerFactory.getLogger(PipelineStepData.class);

    /**
     * Converts this data into a given {@link PipelineStepData} class and returns the converted class, packed in an
     * {@link Optional}. If conversion is impossible, returns an empty {@link Optional}.
     *
     * @param clazz class this should be converted to
     * @param <T>   {@link PipelineStepData} type
     * @return Optional containing the converted class or that is empty, if conversion failed.
     */
    default <T extends PipelineStepData> Optional<T> asPipelineStepData(Class<T> clazz) {
        if (!clazz.isAssignableFrom(this.getClass())) {
            return Optional.empty();
        }
        return Optional.of(clazz.cast(this));
    }

    default String serialize() {
        var oom = JsonHandling.createObjectMapper();
        try {
            return oom.writeValueAsString(this);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return null;
        }
    }

    default PipelineStepData deserialize(String data) {
        var oom = JsonHandling.createObjectMapper();
        try {
            return oom.readValue(data, this.getClass());
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return null;
        }
    }
}
