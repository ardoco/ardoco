package edu.kit.kastel.mcse.ner_for_arch.recognizer;

import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.chat.response.ChatResponse;
import edu.kit.kastel.mcse.ner_for_arch.model.NamedEntity;
import edu.kit.kastel.mcse.ner_for_arch.model.SoftwareArchitectureDocumentation;
import edu.kit.kastel.mcse.ner_for_arch.serialization.NamedEntityParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Set;


/**
 * A prompt type designed to generate a structured text output using only one call.
 * This type indicates that the prompt output format must adhere to the following structure:
 * <pre>
 * BEGIN-OUTPUT
 * &lt;entityType1&gt; entities recognized:
 * &lt;entityName&gt;, &lt;line&gt;
 * ...
 * Alternative names:
 * &lt;entityName&gt;: &lt;alternativeName1&gt;, &lt;alternativeName2&gt;, ...
 * ...
 *
 * &lt;entityType2&gt; entities recognized:
 * ...
 * END-OUTPUT
 * </pre>
 *
 * Example:<br>
 *
 * Text:<br>
 * The AuthenticationService handles login requests.<br>
 * It forwards valid credentials to the UserDatabase.<br>
 * The service logs each attempt.<br>
 * <br>
 * Output:<br>
 * <pre>
 * BEGIN-OUTPUT
 * COMPONENT entities recognized:
 * AuthenticationService, 'The AuthenticationService handles login requests.'
 * AuthenticationService, 'It forwards valid credentials to the UserDatabase.'
 * UserDatabase, 'It forwards valid credentials to the UserDatabase.'
 * AuthenticationService, 'The service logs each attempt.'
 *
 * Alternative names:
 * AuthenticationService: service
 * UserDatabase: None
 * END-OUTPUT
 * </pre>
 */
public class StructuredTextOutputPrompt extends Prompt {
    private static final Logger logger = LoggerFactory.getLogger(StructuredTextOutputPrompt.class);

    /**
     * Constructs a new {@link StructuredTextOutputPrompt} instance.
     *
     * @param text the text of the prompt
     * @throws IllegalArgumentException if the text is null or blank
     */
    public StructuredTextOutputPrompt(String text) {
        super(text);
    }

    @Override
    public String getExpectedOutputFormat() {
        return """
                BEGIN-OUTPUT
                COMPONENT entities recognized:
                <componentName>, '<line>'
                ...
                Alternative names:
                <componentName>: <alternativeName1>, <alternativeName2>, ...
                ...
                END-OUTPUT
                
                Example (content is imaginary):
                BEGIN-OUTPUT
                COMPONENT entities recognized:
                AuthenticationService, 'The AuthenticationService handles login requests.'
                AuthenticationService, 'It forwards valid credentials to the UserDatabase.'
                UserDatabase, 'It forwards valid credentials to the UserDatabase.'
                AuthenticationService, 'The service logs each attempt.'
                
                Alternative names:
                AuthenticationService: service
                UserDatabase: None
                END-OUTPUT
                """;
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
        int start = answer.indexOf("BEGIN-OUTPUT");
        int end = answer.lastIndexOf("END-OUTPUT");
        if (start != -1 && end != -1 && end > start) {
            start += "BEGIN-OUTPUT".length();
            answer = answer.substring(start, end);
        } else {
            logger.warn("No valid structured text output found. Output must begin with 'BEGIN-OUTPUT' and end with 'END-OUTPUT'.");
            throw new IOException("No valid structured text output found. Output must begin with 'BEGIN-OUTPUT' and end with 'END-OUTPUT'.");
        }
        return NamedEntityParser.fromString(answer, sad);
    }

    @Override
    public String toString() {
        return "StructuredTextOutputPrompt{" +
                "text='" + text + '\'' +
                '}';
    }
}