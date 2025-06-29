package edu.kit.kastel.mcse.ner_for_arch.recognizer;

/**
 * Defines the type of a {@link Prompt} instance, indicating the structure and division of its content based on its usage context.
 */
public enum PromptType {
    /**
     * Represents a prompt type designed to generate a structured text output using only one prompt.
     * This type indicates that the prompt output format must adhere to the following structure:
     * <pre>
     * BEGIN-OUTPUT todo
     * &lt;componentName&gt;, &lt;lineNumber&gt;, &lt;referenceType&gt;
     * ...
     * Alternative names:
     * &lt;componentName&gt;: &lt;alternativeName1&gt;, &lt;alternativeName2&gt;, ...
     * ...
     * END-OUTPUT
     * </pre>
     * <p>
     * This prompt type does not support a second part of the prompt and expects the {@link Prompt} object configuration to conform to this single-part structure.
     * </p>
     */
    STRUCTURED_TEXT_OUTPUT_PROMPT,
    /**
     * Represents a prompt type designed to generate JSON-formatted output using only one prompt.
     * This type indicates that the prompt output format must adhere to the following structure:
     * <pre>
     * [
     *   {
     *     "name": "...",
     *     "type": "...",
     *     "alternativeNames": [...],
     *     "occurrences": [
     *       { "line": ..., "referenceType": "..." },
     *       ...
     *     ]
     *   },
     *   ...
     * ]
     * </pre>
     * <p>
     * This prompt type does not support a second part of the prompt and expects the {@link Prompt} object configuration to conform to this single-part structure.
     * </p>
     */
    JSON_OUTPUT_PROMPT,
    /**
     * Represents a prompt type that consists of two distinct parts.
     * <p>
     * The first part generates the desired output content in a free-form format.
     * </p>
     * <p>
     * The second part takes the output from the first part as input and converts it into a structured JSON format, similar to {@link PromptType#JSON_OUTPUT_PROMPT}.
     * </p>
     * <p>
     * This prompt type requires two parts of the prompt and expects the {@link Prompt} object configuration to conform to this two-part structure.
     * </p>
     */
    TWO_PART_PROMPT
}
