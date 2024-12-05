/* Licensed under MIT 2024. */
package edu.kit.kastel.mcse.ardoco.core.common;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

public final class JsonHandling {
    private JsonHandling() {
        throw new IllegalAccessError("Utility class");
    }

    public static ObjectMapper createObjectMapper() {
        var objectMapper = new ObjectMapper();
        objectMapper.configure(SerializationFeature.INDENT_OUTPUT, false);
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.setVisibility(objectMapper.getSerializationConfig()
                .getDefaultVisibilityChecker() //
                .withFieldVisibility(JsonAutoDetect.Visibility.ANY) //
                .withGetterVisibility(JsonAutoDetect.Visibility.NONE) //
                .withSetterVisibility(JsonAutoDetect.Visibility.NONE) //
                .withIsGetterVisibility(JsonAutoDetect.Visibility.NONE));
        return objectMapper;
    }
}
