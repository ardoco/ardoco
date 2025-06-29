package edu.kit.kastel.mcse.ner_for_arch.recognizer;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Represents a prompt that can consist of one or two parts, depending on the {@link PromptType}.
 *
 * @param first  first part of the prompt
 * @param second second part of the prompt
 * @param type   type of the prompt
 */
public record Prompt(String first, String second, PromptType type) {
    private static final Logger logger = LoggerFactory.getLogger(Prompt.class);

    /**
     * Constructs a new {@link Prompt} instance ensuring all input parameters meet the required conditions.
     *
     * @param first  the first part of the prompt
     * @param second the second part of the prompt
     * @param type   the type of prompt
     * @throws IllegalArgumentException if the first part is null or blank, the second part is blank when provided, or if the provided parameters do not form a valid configuration
     */
    public Prompt {
        if (first == null || first.isBlank()) {
            logger.error("First part of prompt cannot be null or blank");
            throw new IllegalArgumentException("First part of prompt cannot be null or blank");
        }

        if (second != null && second.isBlank()) {
            logger.error("Second part of prompt cannot be blank if provided");
            throw new IllegalArgumentException("Second part of prompt cannot be blank if provided");
        }

        if (!promptTypeIsValid(type, second)) {
            logger.error("Invalid prompt configuration for prompt type: {}", type);
            throw new IllegalArgumentException("Invalid prompt configuration for prompt type: " + type);
        }
    }

    private static boolean promptTypeIsValid(PromptType type, String second) {
        return switch (type) {
            case STRUCTURED_TEXT_OUTPUT_PROMPT, JSON_OUTPUT_PROMPT -> second == null;
            case TWO_PART_PROMPT -> second != null;
        };
    }


    @NotNull
    @Override
    public String toString() {
        return "Prompt{" +
                "first=\n'" + first + "'" +
                "\n, second=\n'" + second + "'" +
                "\n, type=" + type + "}";
    }
}
