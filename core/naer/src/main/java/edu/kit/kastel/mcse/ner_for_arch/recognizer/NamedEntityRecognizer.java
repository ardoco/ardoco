package edu.kit.kastel.mcse.ner_for_arch.recognizer;

import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.chat.response.ChatResponse;
import edu.kit.kastel.mcse.ner_for_arch.model.NamedEntity;
import edu.kit.kastel.mcse.ner_for_arch.model.SoftwareArchitectureDocumentation;
import edu.kit.kastel.mcse.ner_for_arch.serialization.NamedEntityParser;
import edu.kit.kastel.mcse.ner_for_arch.util.ChatModelFactory;
import edu.kit.kastel.mcse.ner_for_arch.util.ModelProvider;
import kotlin.Pair;
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
    private static final String EXAMPLE_PROMPT = loadPromptFromResources("component_recognition_example_prompt.txt");
    /**
     * The chat model used to process the SAD
     */
    private final ChatModel chatModel;
    /**
     * todo
     */
    private final PromptType promptType;
    /**
     * The prompt that instructs the chat model on how to identify named entities todo erklären wie das funktioniert mit dem pair und welche prompt arten es gibt usw
     */
    private final Pair<String, String> prompt;
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
        this.promptType = builder.promptType;
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
        //String answer = chatModel.chat(prompt + "\nText:\n" + softwareArchitectureDocumentation.getText()); //old
        SystemMessage systemMessage = new SystemMessage("You are a software engineer and software architect.");
        String answer = switch (promptType) {
            case STRUCTURED_TEXT_OUTPUT_PROMPT, JSON_OUTPUT_PROMPT -> {
                String actualPrompt = prompt.getFirst();
                UserMessage userMessage = new UserMessage(actualPrompt + "\nText:\n" + softwareArchitectureDocumentation.getText());
                ChatRequest chatRequest = ChatRequest.builder().messages(systemMessage, userMessage).build();
                ChatResponse chatResponse = chatModel.chat(chatRequest);
                yield chatResponse.aiMessage().text();
            }
            case TWO_PART_PROMPT -> {
                //part one: "get components unstructured"
                UserMessage userMessage1 = new UserMessage(prompt.getFirst() + "\nText:\n" + softwareArchitectureDocumentation.getText());

                ChatRequest chatRequest1 = ChatRequest.builder().messages(systemMessage, userMessage1).build();

                ChatResponse chatResponse1 = chatModel.chat(chatRequest1);
                String part1Answer = chatResponse1.aiMessage().text();
                System.out.println("part1Answer: \n" + part1Answer + "\n-----------------");

                //part two: "transform to structured JSON output"
                UserMessage userMessage2 = new UserMessage(prompt.getSecond() + "\nLast answer:\n" + part1Answer);

                ChatRequest chatRequest2 = ChatRequest.builder().messages(systemMessage, userMessage2).build();

                ChatResponse chatResponse2 = chatModel.chat(chatRequest2);
                yield chatResponse2.aiMessage().text();
            }
        };


        logger.info("parsing LLM response to Java objects...");
        try {
            Set<NamedEntity> result = switch (promptType) {
                case STRUCTURED_TEXT_OUTPUT_PROMPT -> {
                    answer = answer.replaceAll("^```plaintext\\n?", "").replaceAll("\\n?```$", ""); //(für STRUCTURED_TEXT_OUTPUT_PROMPT) ->fix:siehe todo siehe drunter

                    yield NamedEntityParser.fromString(answer, softwareArchitectureDocumentation);
                }
                case JSON_OUTPUT_PROMPT, TWO_PART_PROMPT -> {
                    //remove text before and after JSON array (some LLMs create this text) todo eig wegmachen und prompt verbessern damit das nicht passiert... Oder prompts so bauen dass richtiges result innerhalb von start...end steht und alles drum herum wird ignoriert
                    int start = answer.indexOf('[');
                    int end = answer.lastIndexOf(']');
                    if (start != -1 && end != -1 && end > start) {
                        answer = answer.substring(start, end + 1);
                    } else {
                        logger.error("no valid JSON object found in LLM output: {}", answer);
                        throw new RuntimeException("no valid JSON object found in LLM output: " + answer);
                    }

                    yield NamedEntityParser.fromJson(answer, softwareArchitectureDocumentation);
                }
            };
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
        private Pair<String, String> prompt = new Pair<>(EXAMPLE_PROMPT, null);                 // default value //TODO FRAGE: so ok oder besser als eigene (record) class?
        private PromptType promptType = PromptType.STRUCTURED_TEXT_OUTPUT_PROMPT;               // default value

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


        /*
         * Sets the prompt that will be provided to the chat model.
         * <p>
         *     todo details for what needs to be in the prompt + JAVADOC aktualisieren wegen pair + überladen damit man auch mit string aufrufen kann
         * </p>
         * <p>
         * If not specified, a default prompt will be used.
         * </p>
         *
         * @param prompt the prompt text
         * @return this builder
         */
        public Builder prompt(Pair<String, String> prompt) {
            if (prompt == null) {
                return this; //todo erklärung warum= "damit man das mit den paramaterized tests benutzen kann" - oder ist das zu schlechter stil wegen unerwartetem verghalten und ich muss das halt in der parameterized test klasse handlen?
                //logger.error("prompt is null");
                //throw new IllegalArgumentException("prompt is null");
            }
            if (prompt.getFirst().isBlank()) {
                logger.error("prompt is blank");
                throw new IllegalArgumentException("prompt is blank");
            }
            if (prompt.getSecond() != null && prompt.getSecond().isBlank()) {
                logger.error("prompt second part is blank");
                throw new IllegalArgumentException("prompt second part is blank");
            }

            this.prompt = prompt;
            return this;
        }

        /**
         * todo
         *
         * @param promptType
         * @return
         */
        public Builder promptType(PromptType promptType) {
            if (promptType == null) {
                return this; //todo konsisten zu der prompt methode machen
                //logger.error("promptType is null");
                //throw new IllegalArgumentException("promptType is null");
            }
            this.promptType = promptType; //TODO iwi noch assuren, dass das pair und der prompt type immer konsistent gesetzt sind!
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
