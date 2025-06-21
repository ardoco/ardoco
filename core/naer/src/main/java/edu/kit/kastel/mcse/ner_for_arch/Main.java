package edu.kit.kastel.mcse.ner_for_arch;

import edu.kit.kastel.mcse.ner_for_arch.model.NamedEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import edu.kit.kastel.mcse.ner_for_arch.recognizer.NamedEntityRecognizer;

import java.nio.file.Path;
import java.util.Set;

public class Main {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    /**
     * only used for small tests and development purposes
     */
    public static void main(String[] args) {

        NamedEntityRecognizer namedEntityRecognizer = new NamedEntityRecognizer.Builder(Path.of("src/test/resources/evaluation_resources/jabref/text_2021/jabref_1SentPerLine.txt")).build();
        Set<NamedEntity> components = namedEntityRecognizer.recognize();

        System.out.println(components);

        /*// Use the ArDoCo ClassificationMetricsCalculator to calculate metrics
        ClassificationMetricsCalculator calculator = ClassificationMetricsCalculator.getInstance();
        SingleClassificationResult<deprecated.ComponentOccurrence> result = calculator.calculateMetrics(foundComponentOccurrences, groundTruth, null);
        System.out.println("Precision=" + result.getPrecision());
        System.out.println("Recall=" + result.getRecall());
        System.out.println("F1-Score=" + result.getF1());*/
    }


}
