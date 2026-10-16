/* Licensed under MIT 2024-2026. */
package edu.kit.kastel.mcse.ardoco.core.common;

import com.fasterxml.jackson.annotation.JsonAutoDetect;

import tools.jackson.core.StreamReadConstraints;
import tools.jackson.core.json.JsonFactory;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.SerializationFeature;
import tools.jackson.databind.json.JsonMapper;

/**
 * Utility class for creating and configuring Jackson ObjectMappers for JSON serialization and deserialization.
 */
public final class JsonHandling {
    private JsonHandling() {
        throw new IllegalAccessError("Utility class");
    }

    /**
     * Creates and configures a Jackson ObjectMapper for use with ARDoCo models.
     *
     * @return a configured ObjectMapper
     */
    public static ObjectMapper createObjectMapper() {
        return createObjectMapper(JsonFactory.builderWithJackson2Defaults().build());
    }

    /**
     * Creates and configures a Jackson ObjectMapper for use with ARDoCo models that uses the given stream read constraints.
     *
     * @param streamReadConstraints the stream read constraints to use
     * @return a configured ObjectMapper
     */
    public static ObjectMapper createObjectMapper(StreamReadConstraints streamReadConstraints) {
        return createObjectMapper(JsonFactory.builderWithJackson2Defaults().streamReadConstraints(streamReadConstraints).build());
    }

    private static ObjectMapper createObjectMapper(JsonFactory jsonFactory) {
        // Jackson 3 changed several defaults (e.g., alphabetic property sorting, enum handling, escaping of forward slashes,
        // final fields as mutators). Keep the Jackson 2 behavior to retain the serialized format.
        return JsonMapper.builder(jsonFactory)
                .configureForJackson2()
                .disable(SerializationFeature.INDENT_OUTPUT)
                .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                .changeDefaultVisibility(visibilityChecker -> visibilityChecker //
                        .withFieldVisibility(JsonAutoDetect.Visibility.ANY) //
                        .withGetterVisibility(JsonAutoDetect.Visibility.NONE) //
                        .withSetterVisibility(JsonAutoDetect.Visibility.NONE) //
                        .withIsGetterVisibility(JsonAutoDetect.Visibility.NONE) //
                        // Jackson 2 default creator visibility (Jackson 3 defaults to PUBLIC_ONLY / NON_PRIVATE)
                        .withCreatorVisibility(JsonAutoDetect.Visibility.ANY) //
                        .withScalarConstructorVisibility(JsonAutoDetect.Visibility.ANY))
                .build();
    }
}
