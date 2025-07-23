package edu.kit.kastel.mcse.ardoco.ner_for_arch.recognizer;

import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.chat.response.ChatResponse;
import edu.kit.kastel.mcse.ardoco.ner_for_arch.model.NamedEntity;
import edu.kit.kastel.mcse.ardoco.ner_for_arch.model.SoftwareArchitectureDocumentation;
import edu.kit.kastel.mcse.ardoco.ner_for_arch.serialization.NamedEntityParser;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Set;

/**
 * A prompt type that does two calls.
 * The first part generates the desired output content in a free-form format.
 * The second part takes the output from the first part as input and converts it into a structured JSON format that must adhere to the following JSON structure:
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
public class TwoPartPrompt extends Prompt {
    private static final Logger logger = LoggerFactory.getLogger(TwoPartPrompt.class);

    private final String secondText;

    /**
     * Constructs a new {@link TwoPartPrompt} instance.
     *
     * @param firstText  the first part of the prompt
     * @param secondText the second part of the prompt
     * @throws IllegalArgumentException if the first or second part is null or blank
     */
    public TwoPartPrompt(String firstText, String secondText) {
        super(firstText);
        if (secondText == null || secondText.isBlank()) {
            logger.error("Second part of prompt cannot be null or blank for TwoPartPrompt");
            throw new IllegalArgumentException("Second part of prompt cannot be null or blank for TwoPartPrompt");
        }
        this.secondText = secondText;
    }

    /**
     * Gets the second part of the prompt.
     *
     * @return the second part of the prompt
     */
    public String getSecondText() {
        return secondText;
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
    public String process(ChatModel chatModel, SoftwareArchitectureDocumentation sad) {
        logger.info("send prompt one to get components unstructured...");
        UserMessage userMessage1 = new UserMessage(this.text + "\nText:\n" + sad.getText());
        ChatRequest chatRequest1 = ChatRequest.builder().messages(systemMessage, userMessage1).build();
        ChatResponse chatResponse1 = chatModel.chat(chatRequest1);
        String part1Answer = chatResponse1.aiMessage().text();

        logger.info("send prompt two to transform answer to structured JSON array...");
        UserMessage userMessage2 = new UserMessage(secondText + "\nLast answer:\n" + part1Answer);
        ChatRequest chatRequest2 = ChatRequest.builder().messages(systemMessage, userMessage2).build();
        ChatResponse chatResponse2 = chatModel.chat(chatRequest2);
        return chatResponse2.aiMessage().text();
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

    @NotNull
    @Override
    public String toString() {
        return "TwoPartPrompt{" +
                "first=\n'" + text + "'" +
                "\n, second=\n'" + secondText + "'}";
    }

    public static TwoPartPrompt getDefault() {
        String taskPrompt = """
        In the following text, identify all architecturally relevant components that are explicitly named.
        
        For each component, provide:
        - The primary name (as it appears in the text)
        - All alternative names or abbreviations found in the text (case-insensitive match)
        - All complete lines where the component is mentioned.
        
        Rules:
        - Only include actual architecturally relevant components (e.g., modules, services, subsystems, layers)
        - Do not include: interfaces, external libraries, frameworks, or technologies unless they are implemented in this architecture as components
        - Include all indirect references to components as well.
          For example, if a sentence says “Component X handles requests.”, and the following sentence says “It interacts with Component Y.”, then both sentences must be included for Component X, because “It” indirectly refers to Component X.
        
        Return your findings in a clear, unambiguous, structured text format so that a follow-up transformation into JSON is easy.
        """;
        String formattingPrompt = """
        Given the last answer (see below), for each component, return a JSON object containing:
        - "name": the primary name of the component (use the most descriptive name).
        - "type": "COMPONENT"
        - "alternativeNames": a list of alternative or ambiguous names, if applicable.
        - "occurrences": a list of lines where the component appears or is referenced.
        
        Output should be a JSON array (and nothing else!), like:
        [
            {
                "name": "...",
                "type": "COMPONENT",
                "alternativeNames": [...],
                "occurrences": [...]
            },
            ...
        ]
        
        Example:
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
                "alternativeNames": ["DB"],
                "occurrences": ["It forwards valid credentials to the UserDatabase.", "The DB then validates the credentials."]
            }
        ]
        """;
        return new TwoPartPrompt(taskPrompt, formattingPrompt);
    }

}
