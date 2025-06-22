package edu.kit.kastel.mcse.ner_for_arch.recognizer;

import dev.langchain4j.model.chat.ChatModel;
import edu.kit.kastel.mcse.ner_for_arch.model.NamedEntity;
import edu.kit.kastel.mcse.ner_for_arch.model.SoftwareArchitectureDocumentation;
import edu.kit.kastel.mcse.ner_for_arch.serialization.NamedEntityParser;
import edu.kit.kastel.mcse.ner_for_arch.util.ChatModelFactory;
import edu.kit.kastel.mcse.ner_for_arch.util.ModelProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Set;

/**
 * The main interface of the library for recognizing named entities in software architecture documentations.
 * <p>The recognizer uses a default prompt and VDL {@link ModelProvider} if not specified otherwise.</p>
 */
public class NamedEntityRecognizer {
    private static final Logger logger = LoggerFactory.getLogger(NamedEntityRecognizer.class);
    private static final String EXAMPLE_PROMPT = loadPromptFromResources("component_recognition_prompt.txt");
    /**
     * The chat model used to process the SAD
     */
    private final ChatModel chatModel;
    /**
     * The prompt that instructs the chat model on how to identify named entities
     */
    private final String prompt;
    /**
     * The software architecture documentation (SAD) to analyze
     */
    private final SoftwareArchitectureDocumentation softwareArchitectureDocumentation;

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
        //call LLM
        logger.info("calling LLM...");
        String answer = chatModel.chat(prompt + "\nText:\n" + softwareArchitectureDocumentation.getText());

        //remove text before and after JSON array (some LLMs create this text)
        int start = answer.indexOf('[');
        int end = answer.lastIndexOf(']');
        if (start != -1 && end != -1 && end > start) {
            answer = answer.substring(start, end + 1);
        } else {
            logger.error("no valid JSON object found in LLM output: {}", answer);
            throw new RuntimeException("no valid JSON object found in LLM output: " + answer);
        }

        //parse JSON array to the set of named entities
        logger.info("parsing LLM response to Java objects...");
        try {
            Set<NamedEntity> result = NamedEntityParser.fromJson(answer, softwareArchitectureDocumentation);
            logger.info("successfully finished and found {} named entities", result.size());
            return result;
        } catch (IOException e) {
            logger.error("error parsing LLM output to named entities");
            throw new RuntimeException(e);
        }
    }

    /**
     * Builder for {@link NamedEntityRecognizer} instances.
     */
    public static class Builder {
        private final Logger logger = LoggerFactory.getLogger(Builder.class);

        private final SoftwareArchitectureDocumentation softwareArchitectureDocumentation;
        private ChatModel chatModel = ChatModelFactory.withProvider(ModelProvider.VDL).build(); // default value
        private String prompt = EXAMPLE_PROMPT;                             // default value

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
            this.chatModel = chatModel;
            return this;
        }


        /**
         * Sets the prompt that will be provided to the chat model.
         * <p>
         *     todo details for what needs to be in the prompt
         * </p>
         * <p>
         * If not specified, a default prompt will be used.
         * </p>
         *
         * @param prompt the prompt text
         * @return this builder
         */
        public Builder prompt(String prompt) {
            if (prompt == null) {
                logger.error("prompt is null");
                throw new IllegalArgumentException("prompt is null");
            }
            if (prompt.isBlank()) {
                logger.error("prompt is blank");
                throw new IllegalArgumentException("prompt is blank");
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
