package edu.kit.kastel.mcse.ner_for_arch.recognizer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Custom deserializer for the {@link Prompt} class hierarchy.
 * This deserializer creates the appropriate concrete subclass based on the "type" field in the JSON.
 */
public class PromptDeserializer extends StdDeserializer<Prompt> {
    private static final Logger logger = LoggerFactory.getLogger(PromptDeserializer.class);

    public PromptDeserializer() {
        this(null);
    }

    public PromptDeserializer(Class<?> vc) {
        super(vc);
    }

    @Override
    public Prompt deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException {
        JsonNode node = jp.getCodec().readTree(jp);

        String first = node.get("first").asText();
        JsonNode secondNode = node.get("second");
        String second = secondNode != null && !secondNode.isNull() ? secondNode.asText() : null;
        String typeStr = node.get("type").asText();

        try {
            return switch (typeStr) {
                case "STRUCTURED_TEXT_OUTPUT_PROMPT" -> new StructuredTextOutputPrompt(first);
                case "JSON_OUTPUT_PROMPT" -> new JsonOutputPrompt(first);
                case "TWO_PART_PROMPT" -> {
                    if (second == null || second.isBlank()) {
                        logger.error("Second part of prompt cannot be null or blank for TWO_PART_PROMPT");
                        throw new IllegalArgumentException("Second part of prompt cannot be null or blank for TWO_PART_PROMPT");
                    }
                    yield new TwoPartPrompt(first, second);
                }
                default -> throw new IllegalStateException("Unexpected prompt type value: " + typeStr);
            };
        } catch (IllegalArgumentException e) {
            logger.error("Could not create prompt of type {}", typeStr, e);
            throw new IOException("Could not create prompt of type " + typeStr, e);
        }
    }
}