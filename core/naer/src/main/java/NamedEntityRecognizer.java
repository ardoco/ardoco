import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import dev.langchain4j.model.chat.ChatModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.ChatModelBuilder;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;
import java.util.Set;

/**
 * todo: überall schön javadoc und erklären wie es zu verwenden ist und dass das hier main interface of the library is
 */
public class NamedEntityRecognizer {
    private static final String EXAMPLE_PROMPT = """
            You are an experienced software engineer with expertise in software architecture analysis.
            Given a text describing a software architecture (one sentence per line), identify all software architecture components mentioned in each sentence.
            
            For each component, output a JSON object containing:
            - "name": the primary name of the component (use the most descriptive name).
            - "type": "COMPONENT"
            - "alternativeNames": a list of alternative or ambiguous names, if applicable.
            - "occurrences": a list of objects each with:
                - "line": the line number of the occurrence (starting from 1),
                - "referenceType": "DIRECT" or "INDIRECT".
            
            Instructions:
            - Use the exact casing of the component as it appears in the text.
            - Normalize similar component names if they clearly refer to the same concept (e.g., treat Database and User Database as the same component).
            - Only include components that are relevant architectural elements.
            - If a component appears in multiple lines, list all occurrences.
            - A reference where the name or one of the alternative names is in the sentence is called DIRECT. Otherwise it is called INDIRECT.
            - Only return a JSON array of component objects, nothing else.
            
            Example (for a single component):
            {
                "name": "Database",
                "type": "COMPONENT"
                "alternativeNames": ["UserDatabase", "DB"],
                "occurrences": [
                    {"line": 1, "referenceType": "DIRECT"},
                    {"line": 3, "referenceType": "INDIRECT"},
                    {"line": 8, "referenceType": "DIRECT"},
                    {"line": 11, "referenceType": "INDIRECT"}
                ]
            }
            
            Output should be a JSON array, like:
            [
                {
                    "name": "...",
                    "type": "COMPONENT",
                    "alternativeNames": [...],
                    "occurrences": [
                        {"line": ..., "referenceType": "..."},
                        ...
                    ]
                },
                ...
            ]
            
            """;
    private final Logger logger = LoggerFactory.getLogger(NamedEntityRecognizer.class);
    private final ChatModel chatModel;
    private final String prompt;
    private final SoftwareArchitectureDocumentation softwareArchitectureDocumentation;

    private NamedEntityRecognizer(Builder builder) {
        this.chatModel = builder.chatModel;
        this.prompt = builder.prompt;
        this.softwareArchitectureDocumentation = builder.softwareArchitectureDocumentation;
    }

    public Set<NamedEntity> recognize() {
        //call LLM
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
        try {
            return parseNamedEntities(answer, softwareArchitectureDocumentation);
        } catch (IOException e) {
            logger.error("error parsing LLM output to named entities");
            throw new RuntimeException(e);
        }
    }

    public Set<NamedEntity> parseNamedEntities(String json, SoftwareArchitectureDocumentation sad) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        SimpleModule module = new SimpleModule();
        module.addDeserializer(Set.class, new NamedEntityDeserializer(sad));
        mapper.registerModule(module);
        return mapper.readValue(json, new TypeReference<>() {
        });
    }

    /**
     * Builder for {@link NamedEntityRecognizer} instances.
     */
    public static class Builder {
        private final Logger logger = LoggerFactory.getLogger(Builder.class);

        private final SoftwareArchitectureDocumentation softwareArchitectureDocumentation;
        private ChatModel chatModel = ChatModelBuilder.buildChatModelVDL(); // default value
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
         * Note: If this constructor is used, {@link SoftwareArchitectureDocumentation#getFilePath()}{@code == }{@link Optional#empty()}
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
         *
         * @param prompt the prompt text, must not be {@code null} or blank and todo (genaues format spezifizieren was im prompt stehen muss und was nicht) -> evtl so bauen, dass es einen festen prompt für den output gibt und man nur den rest ändert(?)
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
         * default values (if not explicitly configured differently): <br>
         * {@link NamedEntityRecognizer#chatModel} = {@link ChatModelBuilder#buildChatModelVDL()} <br>
         * {@link NamedEntityRecognizer#prompt} = {@value #EXAMPLE_PROMPT}
         * </p>
         *
         * @return a new {@code NamedEntityRecognizer}
         */
        public NamedEntityRecognizer build() {
            return new NamedEntityRecognizer(this);
        }
    }

}
