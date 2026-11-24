/* Licensed under MIT 2025-2026. */
package edu.kit.kastel.mcse.ardoco.naer.recognizer;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.model.chat.ChatModel;
import edu.kit.kastel.mcse.ardoco.naer.model.NamedEntity;
import edu.kit.kastel.mcse.ardoco.naer.model.NamedEntityType;
import edu.kit.kastel.mcse.ardoco.naer.model.SoftwareArchitectureDocumentation;

/**
 * Abstract base class representing a prompt for named entity recognition.
 */
public abstract class Prompt {
    private static final Logger logger = LoggerFactory.getLogger(Prompt.class);
    protected final SystemMessage systemMessage = new SystemMessage("You are a software engineer and software architect.");
    protected final String text;

    /**
     * Constructs a new {@link Prompt} instance.
     *
     * @param text the text of the prompt
     * @throws IllegalArgumentException if the text is null or blank
     */
    protected Prompt(String text) {
        if (text == null || text.isBlank()) {
            logger.error("First part of prompt cannot be null or blank");
            throw new IllegalArgumentException("First part of prompt cannot be null or blank");
        }
        this.text = text;
    }

    /**
     * Gets the text of the prompt.
     *
     * @return the text of the prompt
     */
    public String getText() {
        return text;
    }

    /**
     * Gets the text of the prompt, extended by a list of possible named entities that could be mentioned in the SAD, grouped by their types.
     * The prompt itself is not modified.
     *
     * @param possibleEntities a map containing named entity types as keys and sets of entity names as values. Each type represents a category (e.g., COMPONENT,
     *                         INTERFACE, ...), and each set contains the names of entities belonging to that type. Must not be null or contain null
     *                         keys/values.
     * @return the text of the prompt, or the text with the list of possible entities appended if the map is not empty
     */
    protected String getText(Map<NamedEntityType, Set<String>> possibleEntities) {
        if (possibleEntities.isEmpty()) {
            return text;
        }

        StringBuilder sb = new StringBuilder();

        for (Map.Entry<NamedEntityType, Set<String>> entry : possibleEntities.entrySet()) {
            NamedEntityType type = entry.getKey();
            Set<String> names = entry.getValue();
            if (names.isEmpty()) {
                continue;
            }

            sb.append(type.toString().toLowerCase()).append(" entities: ");
            sb.append(String.join(", ", names));
            sb.append("\n");
        }

        String possibleEntitiesString = sb.toString();

        return text + "\n\nAs support, here is a list of entities that could be mentioned in the text:\n" + possibleEntitiesString + "\n";
    }

    /**
     * Processes the prompt using the given chat model and SAD.
     *
     * @param chatModel the chat model to use
     * @param sad       the software architecture documentation to be analyzed
     * @return the response from the chat model
     */
    public String process(ChatModel chatModel, SoftwareArchitectureDocumentation sad) {
        return process(chatModel, sad, Map.of());
    }

    /**
     * Processes the prompt using the given chat model and SAD, with a list of possible named entities as support (see {@link #getText(Map)}).
     *
     * @param chatModel        the chat model to use
     * @param sad              the software architecture documentation to be analyzed
     * @param possibleEntities possible named entities grouped by their types; may be empty
     * @return the response from the chat model
     */
    public abstract String process(ChatModel chatModel, SoftwareArchitectureDocumentation sad, Map<NamedEntityType, Set<String>> possibleEntities);

    /**
     * Parses the answer from the chat model into a set of named entities.
     *
     * @param answer the answer from the chat model
     * @param sad    the software architecture documentation
     * @return a set of named entities
     * @throws IOException if the answer cannot be parsed
     */
    public abstract Set<NamedEntity> parseAnswer(String answer, SoftwareArchitectureDocumentation sad) throws IOException;

    /**
     * Returns the expected output format of the prompt.
     *
     * @return a string describing the expected format of the output
     */
    public abstract String getExpectedOutputFormat();
}
