package edu.kit.kastel.mcse.ardoco.ner_for_arch.recognizer;

import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.chat.response.ChatResponse;
import edu.kit.kastel.mcse.ardoco.ner_for_arch.model.NamedEntity;
import edu.kit.kastel.mcse.ardoco.ner_for_arch.model.NamedEntityType;
import edu.kit.kastel.mcse.ardoco.ner_for_arch.model.SoftwareArchitectureDocumentation;
import edu.kit.kastel.mcse.ardoco.ner_for_arch.util.ChatModelFactory;
import edu.kit.kastel.mcse.ardoco.ner_for_arch.util.ModelProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Map;
import java.util.Set;

/**
 * The main interface of the library for recognizing named entities in software architecture documentations.
 * <p>The recognizer uses a default prompt and VDL {@link ModelProvider} if not specified otherwise.</p>
 */
public class NamedEntityRecognizer {
    private static final Logger logger = LoggerFactory.getLogger(NamedEntityRecognizer.class);
    private static final String EXAMPLE_PROMPT = loadPromptFromResources("component_recognition_example_prompt.txt");
    /**
     * The chat model used to process the SAD
     */
    private final ChatModel chatModel;
    /**
     * The software architecture documentation (SAD) to analyze
     */
    private final SoftwareArchitectureDocumentation softwareArchitectureDocumentation;
    /**
     * The prompt that instructs the chat model on how to identify named entities
     */
    private final Prompt prompt;

    /**
     * Private constructor used by the Builder to create a NamedEntityRecognizer instance.
     *
     * @param builder the Builder instance containing the configured parameters
     */
    private NamedEntityRecognizer(Builder builder) {
        this.chatModel = builder.chatModel;
        this.prompt = builder.prompt;
        this.softwareArchitectureDocumentation = builder.softwareArchitectureDocumentation;
    }

    private static String loadPromptFromResources(String path) {
        try (InputStream is = NamedEntityRecognizer.class.getClassLoader().getResourceAsStream(path)) {
            if (is == null) {
                logger.error("could not load resource '{}'", path);
                throw new IllegalStateException("Could not find resource: " + path);
            }
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            logger.error("could not load prompt from resources '{}'", path);
            throw new IllegalStateException("Could not load prompt from resources", e);
        }
    }

    /**
     * Recognizes {@link NamedEntity} instances in the given {@link NamedEntityRecognizer#softwareArchitectureDocumentation}.
     * <p>
     * This method first sends the SAD text along with the prompt to the configured chat model and afterward parses the chat models response into a set of {@link NamedEntity} instances.
     * </p>
     *
     * @return a set of recognized named entities
     */
    public Set<NamedEntity> recognize() {
        logger.info("calling LLM...");
        String answer = prompt.process(chatModel, softwareArchitectureDocumentation);

        try {
            return prompt.parseAnswer(answer, softwareArchitectureDocumentation); //if everything works as intended, this does not fail
        } catch (IOException e) {
            logger.warn("initial parsing failed, attempting to reformat LLM output (via LLM)...");
            String repairPrompt = "The following output is invalid. Reformat it so it precisely adheres to the following output format:\n" + prompt.getExpectedOutputFormat() +
                    "\n\nInvalid output to reformat:\n" + answer + "\nThis error occurred when trying to parse it:\n" + e.getMessage();
            UserMessage repairMessage = new UserMessage(repairPrompt);
            SystemMessage systemMessage = new SystemMessage("You are a software engineer and software architect.");
            ChatRequest repairRequest = ChatRequest.builder().messages(systemMessage, repairMessage).build();
            ChatResponse repairResponse = chatModel.chat(repairRequest);
            String repairedAnswer = repairResponse.aiMessage().text();

            logger.info("parsing repaired LLM response...");
            try {
                return prompt.parseAnswer(repairedAnswer, softwareArchitectureDocumentation);
            } catch (IOException e2) {
                logger.error("repair attempt failed");
                throw new RuntimeException("Both original and repair attempts failed", e2);
            }
        }
    }

    /**
     * Recognizes {@link NamedEntity} instances in the given {@link NamedEntityRecognizer#softwareArchitectureDocumentation} using a set of entity names (that are suspected to occur in the SAD) as support.
     * <p>
     * This method first sends the SAD text along with the prompt to the configured chat model and afterward parses the chat models response into a set of {@link NamedEntity} instances.
     * </p>
     *
     * @param possibleEntities a map containing potential named entities where the keys are entity names and their values are entity types (e.g., names that should be recognized with their corresponding types).
     * @return a set of recognized named entities
     */
    public Set<NamedEntity> recognize(Map<NamedEntityType, Set<String>> possibleEntities) {
        prompt.addPossibleEntities(possibleEntities);
        return recognize();
    }


    /**
     * Builder for {@link NamedEntityRecognizer} instances.
     */
    public static class Builder {
        private final Logger logger = LoggerFactory.getLogger(Builder.class);

        private final SoftwareArchitectureDocumentation softwareArchitectureDocumentation;
        private ChatModel chatModel = ChatModelFactory.withProvider(ModelProvider.VDL).build(); // default value
        private Prompt prompt = new StructuredTextOutputPrompt(EXAMPLE_PROMPT);                 // default value

        /**
         * Creates a builder using a path to the file containing the SAD.
         *
         * @param sadFilePath the path to the SAD file
         */
        public Builder(Path sadFilePath) {
            this.softwareArchitectureDocumentation = new SoftwareArchitectureDocumentation(sadFilePath);
        }

        /**
         * Creates a builder using a provided text as the SAD.
         *
         * <p>
         * Note: If this constructor is used, {@link SoftwareArchitectureDocumentation#getFilePath()}{@code == null}
         * </p>
         *
         * @param sadText the SAD as plain text
         */
        public Builder(String sadText) {
            this.softwareArchitectureDocumentation = new SoftwareArchitectureDocumentation(sadText);
        }

        /**
         * Sets the chat model to use.
         *
         * @param chatModel the chat model
         * @return this builder
         */
        public Builder chatModel(ChatModel chatModel) {
            if (chatModel == null) {
                logger.error("chat model must not be null");
                throw new IllegalArgumentException("chat model must not be null");
            }
            this.chatModel = chatModel;
            return this;
        }


        /**
         * Sets the prompt that will be provided to the chat model.
         *
         * <p>
         * If not specified, a default prompt will be used.
         * </p>
         *
         * @param prompt the prompt
         * @return this builder
         */
        public Builder prompt(Prompt prompt) {
            if (prompt == null) {
                logger.error("prompt must not be null");
                throw new IllegalArgumentException("prompt must not be null");
            }
            this.prompt = prompt;
            return this;
        }


        /**
         * Builds the {@link NamedEntityRecognizer} with the configured settings.
         *
         * <p>
         * default values (if not explicitly configured differently):
         * <ul>
         *     <li>{@link NamedEntityRecognizer#chatModel} = {@code defaultVDLChatModel}</li>
         *     <li>{@link NamedEntityRecognizer#prompt} = {@link #EXAMPLE_PROMPT}</li>
         * </ul>
         * </p>
         *
         * @return a new {@link NamedEntityRecognizer}
         */
        public NamedEntityRecognizer build() {
            return new NamedEntityRecognizer(this);
        }
    }
}
