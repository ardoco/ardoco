package edu.kit.kastel.mcse.ner_for_arch;

import edu.kit.kastel.mcse.ner_for_arch.model.NamedEntity;
import edu.kit.kastel.mcse.ner_for_arch.recognizer.NamedEntityRecognizer;
import edu.kit.kastel.mcse.ner_for_arch.recognizer.PromptType;
import kotlin.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.util.Set;

public class Main {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    /**
     * only used for small tests and development purposes
     */
    public static void main(String[] args) {
        String fst = """
                In the following text, identify all architecturally relevant components that are explicitly named.
                
                For each such component:
                1. Provide:
                - The primary name (as it appears in the text)
                - All alternative names or abbreviations found in the text (case-insensitive match)
                
                2. For each appearance:
                - The line number (start counting from 1)
                - Whether the appearance is:
                    DIRECT = The name or any alternative name of the component appears literally in the text (case-insensitive)
                    or
                    INDIRECT = The component is referred to indirectly, e.g., by pronouns ("it", "this component", "such module")
                
                Rules:
                - Only include actual architecturally relevant components (e.g., modules, services, subsystems, layers)
                - Do not include: interfaces, external libraries, frameworks, or technologies unless they are implemented in this architecture as components
                
                Return your findings in a clear, unambiguous, structured text format so that a follow-up transformation into JSON is easy.
                """;
        String snd = """
                Given the last answer (see below), transform it into a valid JSON array.
                For each component, output a JSON object containing:
                {
                  "name": "<primary name>",
                  "type": "COMPONENT",
                  "alternativeNames": ["<alt1>", "<alt2>", ...],
                  "occurrences": [
                    { "line": <line number>, "referenceType": "<DIRECT|INDIRECT>" },
                    ...
                  ]
                }
                
                Make sure:
                - The JSON array is syntactically correct.
                - All alternative names are inside an array (even if empty).
                - All occurrences are listed with correct line numbers and "DIRECT" or "INDIRECT" as "referenceType".
                - No extra text - output only the JSON array.
                
                Example for a single component:
                [
                  {
                    "name": "Database",
                    "type": "COMPONENT",
                    "alternativeNames": ["UserDatabase", "DB"],
                    "occurrences": [
                      { "line": 1, "referenceType": "DIRECT" },
                      { "line": 3, "referenceType": "INDIRECT" },
                      { "line": 8, "referenceType": "DIRECT" },
                      { "line": 11, "referenceType": "INDIRECT" }
                    ]
                  }
                ]
                """;
        //NamedEntityRecognizer namedEntityRecognizer = new NamedEntityRecognizer.Builder(Path.of("src/test/resources/evaluation_resources/jabref/text_2021/jabref_1SentPerLine.txt")).prompt(new Pair<>(fst, snd)).promptType(PromptType.TWO_PART_PROMPT).build();
        NamedEntityRecognizer namedEntityRecognizer = new NamedEntityRecognizer.Builder(Path.of("src/test/resources/evaluation_resources/jabref/text_2021/jabref_1SentPerLine.txt")).build();
        //NamedEntityRecognizer namedEntityRecognizer = new NamedEntityRecognizer.Builder(Path.of("src/test/resources/evaluation_resources/jabref/text_2021/jabref_1SentPerLine.txt")).chatModel(ChatModelFactory.withProvider(ModelProvider.OPEN_AI).build()).build();
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
