package edu.kit.kastel.mcse.ardoco.ner_for_arch.recognizer;

import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.chat.response.ChatResponse;
import edu.kit.kastel.mcse.ardoco.ner_for_arch.model.NamedEntity;
import edu.kit.kastel.mcse.ardoco.ner_for_arch.model.SoftwareArchitectureDocumentation;
import edu.kit.kastel.mcse.ardoco.ner_for_arch.serialization.NamedEntityParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Set;

/**
 * A prompt type designed to generate JSON-formatted output using only one call.
 * This type indicates that the prompt output format must adhere to the following JSON structure:
 * <pre>
 * [
 *   {
 *     "name": "...",
 *     "type": "...",
 *     "alternativeNames": [...],
 *     "occurrences": [...]
 *   },
 *   ...
 * ]
 * </pre>
 * Example:<br>
 * Text:<br>
 * The AuthenticationService handles login requests.<br>
 * It forwards valid credentials to the UserDatabase.<br>
 * The service logs each attempt.<br>
 * <br>
 * Output:<br>
 * <pre>
 * [
 *     {
 *         "name": "AuthenticationService",
 *         "type": "COMPONENT",
 *         "alternativeNames": ["service"],
 *         "occurrences": ["The AuthenticationService handles login requests.", "It forwards valid credentials to the UserDatabase.", "The service logs each attempt."]
 *     },
 *     {
 *         "name": "UserDatabase",
 *         "type": "COMPONENT",
 *         "alternativeNames": [],
 *         "occurrences": ["It forwards valid credentials to the UserDatabase."]
 *     }
 * ]
 * </pre>
 */
public class JsonOutputPrompt extends Prompt {
    private static final Logger logger = LoggerFactory.getLogger(JsonOutputPrompt.class);

    /**
     * Constructs a new {@link JsonOutputPrompt} instance.
     *
     * @param text the text of the prompt
     * @throws IllegalArgumentException if the text is null or blank
     */
    public JsonOutputPrompt(String text) {
        super(text);
    }

    @Override
    public String process(ChatModel chatModel, SoftwareArchitectureDocumentation sad) {
        UserMessage userMessage = new UserMessage(this.text + "\nText:\n" + sad.getText());
        ChatRequest chatRequest = ChatRequest.builder().messages(systemMessage, userMessage).build();
        ChatResponse chatResponse = chatModel.chat(chatRequest);
        return chatResponse.aiMessage().text();
    }

    @Override
    public Set<NamedEntity> parseAnswer(String answer, SoftwareArchitectureDocumentation sad) throws IOException {
        int start = answer.indexOf('[');
        int end = answer.lastIndexOf(']');
        if (start != -1 && end != -1 && end > start) {
            answer = answer.substring(start, end + 1);
        } else {
            logger.warn("No valid JSON array found.");
            throw new IOException("No valid JSON array found.");
        }
        return NamedEntityParser.fromJson(answer, sad);
    }

    @Override
    public String getExpectedOutputFormat() {
        return """
                [
                    {
                        "name": "...",
                        "type": "COMPONENT",
                        "alternativeNames": [...],
                        "occurrences": [...]
                    },
                    ...
                ]
                
                Example (content is imaginary):
                [
                    {
                        "name": "AuthenticationService",
                        "type": "COMPONENT",
                        "alternativeNames": ["service"],
                        "occurrences": ["The AuthenticationService handles login requests.", "It forwards valid credentials to the UserDatabase.", "The service logs each attempt."]
                    },
                    {
                        "name": "UserDatabase",
                        "type": "COMPONENT",
                        "alternativeNames": [],
                        "occurrences": ["It forwards valid credentials to the UserDatabase."]
                    }
                ]
                """;
    }

    @Override
    public String toString() {
        return "JsonOutputPrompt{" +
                "text='" + text + '\'' +
                '}';
    }
}
