package edu.kit.kastel.mcse.ner_for_arch.util;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.ollama.OllamaChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;
import java.util.Map;

/**
 * Utility class for building ChatModel instances using different backends and configurations.
 */
public class DeprecatedChatModelBuilder { //TODO delete (after writing javadoc for the factory) because its depricated
    public static ChatModel buildChatModelVDL() {
        return buildChatModelVDL(0.0, "phi4:latest");//andere models testen evtl.
    }


    /**
     * Builds a langchain4j chat model for the SDQ Virtual Design Lab (VDL) Server, which hosts an ollama instance. (<a href="https://sdq.kastel.kit.edu/wiki/Virtual_Design_Lab_Server">website</a>)
     *
     * @param temperature
     * @param modelName
     * @return the model (ready to use)
     */
    public static ChatModel buildChatModelVDL(double temperature, String modelName) {
        String host = System.getenv("OLLAMA_HOST");
        String user = System.getenv("OLLAMA_USER");
        String password = System.getenv("OLLAMA_PASSWORD");

        ChatModel model = OllamaChatModel.builder()
                .baseUrl(host)
                .customHeaders(Map.of("Authorization", "Basic " + Base64.getEncoder().encodeToString((user + ":" + password).getBytes(StandardCharsets.UTF_8))))
                .modelName(modelName)
                .temperature(temperature)
                .build();
        return model;
    }

    public static ChatModel buildChatModelOpenAI() {
        return buildChatModelOpenAI(0.0, "gpt-4.1-nano"); //most coste efficient models: gpt-4.1-nano https://platform.openai.com/docs/models/gpt-4.1-nano; still pretty cost efficient: gpt-4o-mini https://platform.openai.com/docs/models/gpt-4o-mini
    }

    /**
     * Builds a langchain4j chat model for the SDQ OpenAI organization. (<a href="https://platform.openai.com/docs/overview">website</a>)
     *
     * @param temperature
     * @param modelName
     * @return the model (ready to use)
     */
    public static ChatModel buildChatModelOpenAI(double temperature, String modelName) {
        String apiKey = System.getenv("OPENAI_API_KEY");

        OpenAiChatModel model = OpenAiChatModel.builder()
                .apiKey(apiKey)
                .modelName(modelName)
                .temperature(temperature)
                .timeout(Duration.ofSeconds(120))
                .build();
        return model;
    }

    //possibility to add more models (for example, a local one) here ...
}
