import dev.langchain4j.model.chat.ChatModel;
import edu.kit.kastel.mcse.ardoco.metrics.ClassificationMetricsCalculator;
import edu.kit.kastel.mcse.ardoco.metrics.result.SingleClassificationResult;
import util.ChatModelBuilder;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;

public class Main {
    public static void main(String[] args) {
        System.out.println("Example run for jabref...");

        String sadFilepath = "src/main/resources/goldstandards/jabref/text_2021/jabref_1SentPerLine.txt";
        String goldstandardFilepath = "src/main/resources/goldstandards/jabref/goldstandard_NER.csv";

        ChatModel model = ChatModelBuilder.buildChatModelVDL();

        Set<ComponentOccurrence> foundComponentOccurrences = ComponentRecognizer.recognizeComponents(model, Path.of(sadFilepath));

        Set<ComponentOccurrence> groundTruth = null;
        try {
            groundTruth = ComponentOccurrence.parse(Files.readString(Paths.get(goldstandardFilepath)), true);
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


}
