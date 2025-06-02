import deprecated.ComponentOccurrence;
import deprecated.ComponentRecognizer;
import dev.langchain4j.model.chat.ChatModel;
import edu.kit.kastel.mcse.ardoco.metrics.ClassificationMetricsCalculator;
import edu.kit.kastel.mcse.ardoco.metrics.result.SingleClassificationResult;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import util.ChatModelBuilder;

import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Integration test that evaluates the component recognition using different LLM models, prompts and parameters.
 */
public class TestComponentRecognition {

    @Test
    @Disabled("Nur bei Bedarf aktivieren")
    public void evaluateComponentRecognitionVDL() {
        System.out.println("Evaluating component recognition with VDL model...");

        ChatModel model = ChatModelBuilder.buildChatModelVDL();

        evaluateAllInstances(model);
    }

    private void evaluateAllInstances(ChatModel model) {
        URL goldstandardsURL = this.getClass().getResource("goldstandards");
        assertNotNull(goldstandardsURL);
        Path goldstandardsPath = assertDoesNotThrow(() -> Paths.get(goldstandardsURL.toURI()));
        Stream<Path> goldstandardsSubPaths = assertDoesNotThrow(() -> Files.list(goldstandardsPath));

        goldstandardsSubPaths.filter(Files::isDirectory).forEach(dir -> {
            //search goldstandard
            Path goldstandardFilePath = assertDoesNotThrow(() ->
                    Files.list(dir)
                            .filter(p -> p.getFileName().toString().contains("_NER.csv"))
                            .findFirst()
                            .orElseThrow(() -> new RuntimeException("No goldstandard file in " + dir))
            );

            //search SAD
            Path sadDirPath = assertDoesNotThrow(() ->
                    Files.list(dir)
                            .filter(Files::isDirectory)
                            .filter(p -> p.getFileName().toString().contains("text_"))
                            .findFirst()
                            .orElseThrow(() -> new RuntimeException("No SAD subdir in " + dir))
            );
            Path sadFilePath = assertDoesNotThrow(() ->
                    Files.list(sadDirPath)
                            .filter(p -> p.getFileName().toString().endsWith("_1SentPerLine.txt"))
                            .findFirst()
                            .orElseThrow(() -> new RuntimeException("No SAD file in " + sadDirPath))
            );

            evaluateSingleInstance(dir, model, sadFilePath, goldstandardFilePath);
        });
    }

    private static void evaluateSingleInstance(Path dir, ChatModel model, Path sadFilePath, Path goldstandardFilePath) {
        System.out.println("\n" + dir.getFileName() + ":");
        Set<ComponentOccurrence> foundComponentOccurrences = ComponentRecognizer.recognizeComponents(model, sadFilePath);
        //System.out.println(foundComponentOccurrences);
        Set<ComponentOccurrence> groundTruth = assertDoesNotThrow(() -> ComponentOccurrence.parse(Files.readString(goldstandardFilePath), true));

        // Use the ArDoCo ClassificationMetricsCalculator to calculate metrics
        ClassificationMetricsCalculator calculator = ClassificationMetricsCalculator.getInstance();
        SingleClassificationResult<ComponentOccurrence> result = calculator.calculateMetrics(foundComponentOccurrences, groundTruth, null);
        System.out.println("Precision=" + result.getPrecision());
        System.out.println("Recall=" + result.getRecall());
        System.out.println("F1-Score=" + result.getF1());
    }


    @Test
    @Disabled("Nur bei Bedarf aktivieren. ACHTUNG KANN TEUER WERDEN!")
    public void evaluateComponentRecognitionOpenAI() {
        System.out.println("Evaluating component recognition with OpenAI model...");

        ChatModel model = ChatModelBuilder.buildChatModelOpenAI();

        evaluateAllInstances(model);
    }

}
