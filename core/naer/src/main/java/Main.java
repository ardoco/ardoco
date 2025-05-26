import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.ollama.OllamaChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import edu.kit.kastel.mcse.ardoco.metrics.ClassificationMetricsCalculator;
import edu.kit.kastel.mcse.ardoco.metrics.result.SingleClassificationResult;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.Map;
import java.util.Set;

public class Main {
    public static void main(String[] args) {
        String sadFilepath = "src/main/resources/goldstandards/jabref/text_2021/jabref_1SentPerLine.txt"; //todo könnte man auch aus args auslesen damit man das programm mit nem scipt starten kann usw
        String goldstandardFilepath = "src/main/resources/goldstandards/jabref/goldstandard_NER.csv";

        ChatModel model = buildChatModelVDL(); //todo auch als arg möglich + iwan dann evtl local classifier

        Set<ComponentOccurrence> foundComponentOccurrences = ComponentRecognizer.recognizeComponents(model, sadFilepath);

        Set<ComponentOccurrence> groundTruth = null;
        try {
            groundTruth = ComponentOccurrence.parse(Files.readString(Paths.get(goldstandardFilepath)));
        } catch (IOException e) {
            System.err.println("Error reading goldstandard file: " + goldstandardFilepath);
            System.exit(1);
        }

        // Use the ArDoCo ClassificationMetricsCalculator to calculate metrics
        ClassificationMetricsCalculator calculator = ClassificationMetricsCalculator.getInstance();
        SingleClassificationResult<ComponentOccurrence> result = calculator.calculateMetrics(foundComponentOccurrences, groundTruth, null);
        System.out.println("Precision=" + result.getPrecision());
        System.out.println("Recall=" + result.getRecall());
        System.out.println("F1-Score=" + result.getF1());

    }

    private static ChatModel buildChatModelVDL() {
        return buildChatModelVDL(0.0, "phi4:latest");//andere models testen evtl.
    }

    /**
     * Builds a langchain4j chat model for the SDQ Virtual Design Lab (VDL) Server, which hosts an ollama instance. (<a href="https://sdq.kastel.kit.edu/wiki/Virtual_Design_Lab_Server">website</a>)
     *
     * @param temperature
     * @param modelName
     * @return the model (ready to use)
     */
    private static ChatModel buildChatModelVDL(double temperature, String modelName) {
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

    private static ChatModel buildChatModelOpenAI() {
        return buildChatModelOpenAI(0.0, "gpt-4.1-nano"); //most coste efficient models: gpt-4.1-nano https://platform.openai.com/docs/models/gpt-4.1-nano; still pretty cost efficient: gpt-4o-mini https://platform.openai.com/docs/models/gpt-4o-mini
    }

    /**
     * Builds a langchain4j chat model for the SDQ OpenAI organization. (<a href="https://platform.openai.com/docs/overview">website</a>)
     *
     * @param temperature
     * @param modelName
     * @return the model (ready to use)
     */
    private static ChatModel buildChatModelOpenAI(double temperature, String modelName) {
        String apiKey = System.getenv("OPENAI_API_KEY");

        OpenAiChatModel model = OpenAiChatModel.builder()
                .apiKey(apiKey)
                .modelName(modelName)
                .temperature(temperature)
                .build();
        return model;
    }


}
