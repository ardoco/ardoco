import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.ChatModelBuilder;

import java.nio.file.Path;
import java.util.Set;

public class Main {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        //TODO read json config file, then text and start the process
        logger.info("Reading config file...");
        //Nur TEMPORÄR so (bis man es aus dem file auslesen kann)
        Path sadFilePath = Path.of("src/test/resources/goldstandards/jabref/text_2021/jabref_1SentPerLine.txt");
        String prompt = """
                You are an experienced software engineer with expertise in software architecture analysis.
                Given a text describing a software architecture (one sentence per line), identify all software architecture components mentioned in each sentence.
                
                For each component, output a JSON object containing:
                - "name": the primary name of the component (use the most descriptive name).
                - "type": "COMPONENT"
                - "alternativeNames": a list of alternative or ambiguous names, if applicable.
                - "occurrences": a list of objects each with:
                    - "line": the line number of the occurrence (starting from 1),
                    - "referenceType": "DIRECT" or "CO_REFERENCE".
                
                Instructions:
                - Use the exact casing of the component as it appears in the text.
                - Normalize similar component names if they clearly refer to the same concept (e.g., treat Database and User Database as the same component).
                - Only include components that are relevant architectural elements.
                - If a component appears in multiple lines, list all occurrences.
                - Only return a JSON array of component objects, nothing else.
                
                Example (for a single component):
                {
                    "name": "Database",
                    "type": "COMPONENT"
                    "alternativeNames": ["UserDatabase", "DB"],
                    "occurrences": [
                        {"line": 1, "referenceType": "DIRECT"},
                        {"line": 3, "referenceType": "CO_REFERENCE"},
                        {"line": 8, "referenceType": "DIRECT"},
                        {"line": 11, "referenceType": "CO_REFERENCE"}
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

        //TODO [Frage] parameter einbauen um den output so zu strukturieren oder line by line so wie davor (um zu testen ob das eine Auswirkung auf die Antwortqualität hat)

        Set<NamedEntity> components = NamedEntityRecognizer.recognize(sadFilePath, ChatModelBuilder.buildChatModelVDL(), prompt);
        System.out.println(components);

        //TODO goldstandards einlesen + zu entities parsen [Frage](coreference hier einfach erstmal ignorieren oder goldstandards an jetziges json format anpassen)
        // dann versuchen metrics zu berechnen

        /*// Use the ArDoCo ClassificationMetricsCalculator to calculate metrics
        ClassificationMetricsCalculator calculator = ClassificationMetricsCalculator.getInstance();
        SingleClassificationResult<deprecated.ComponentOccurrence> result = calculator.calculateMetrics(foundComponentOccurrences, groundTruth, null);
        System.out.println("Precision=" + result.getPrecision());
        System.out.println("Recall=" + result.getRecall());
        System.out.println("F1-Score=" + result.getF1());*/
    }


}
