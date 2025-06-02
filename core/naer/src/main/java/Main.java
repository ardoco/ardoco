import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.ChatModelBuilder;

import java.nio.file.Path;
import java.util.Set;

public class Main {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {

        //TODO read config file, then text and start the process
        logger.info("Reading config file...");
        //Nur temporär so (bis man es aus dem file auslesen kann)
        Path sadFilePath = Path.of("src/test/resources/goldstandards/jabref/text_2021/jabref_1SentPerLine.txt");
        String prompt = """
                You are an experienced software engineer with expertise in software architecture analysis.
                Given a text, describing a software architecture (one sentence per line), identify all software architecture components mentioned in each sentence.
                For each component, output its name, its alternative names (if the name is ambiguous) and its occurrences (= line number and reference type(=direct(d) or co-reference(c))) in the following format:
                C:ComponentName; alternativeName1,alternativeName2,...
                o:LineNumberOfOccurrence1,ReferenceType1(d/c);LineNumberOfOccurrence2,ReferenceType2;LineNumberOfOccurrence3,ReferenceType3,...
                
                Instructions:
                Use the exact casing of the component as it appears in the text.
                Use line numbers to indicate where each component is mentioned (starting at 1).
                If a component appears in multiple lines, list it multiple times (once per line).
                If a sentence contains multiple components, include a separate entry for each.
                Normalize similar component names if they clearly refer to the same concept (e.g., treat Database and User Database as the same component when appropriate) and add all names to the alternative names list.
                Use the most descriptive name.
                Only return the list of components (as described above; separated by new line characters), nothing else.
                Double check your answer with respect to the text (line per line) to reduce hallucinations and increase accuracy
                
                Example (for the output for one component (using random data)):
                C:Database;UserDatabase,DB
                o:1,d;3,c;8,d;11,c""";
        String prompt2 = """
    You are an experienced software engineer with expertise in software architecture analysis.
    Given a text describing a software architecture (one sentence per line), identify all software architecture components mentioned in each sentence.

    For each component, output a JSON object containing:
    - "name": the primary name of the component (use the most descriptive name).
    - "alternativeNames": a list of alternative or ambiguous names, if applicable.
    - "occurrences": a list of objects each with:
        - "line": the line number of the occurrence (starting from 1),
        - "referenceType": "d" for direct, "c" for co-reference.

    Instructions:
    - Use the exact casing of the component as it appears in the text.
    - Normalize similar component names if they clearly refer to the same concept (e.g., treat Database and User Database as the same component).
    - Only include components that are relevant architectural elements.
    - If a component appears in multiple lines, list all occurrences in the same array.
    - If a sentence contains multiple components, include each as a separate JSON object.
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

        //TODO [Frage] parameter einbauen um den output so zu strukturieren oder line by line so wie davor (um zu testen ob das eine Auswirkung hat)

        Set<NamedEntity> components = NamedEntityRecognizer.recognize(sadFilePath, ChatModelBuilder.buildChatModelVDL(), prompt);
        System.out.println(components);
        //TODO was bauen um die goldstandards einzulesen und als entities zu parsen (coreference hier einf ignorieren) und dann versuchen metrics zu berechnen



        /*// Use the ArDoCo ClassificationMetricsCalculator to calculate metrics
        ClassificationMetricsCalculator calculator = ClassificationMetricsCalculator.getInstance();
        SingleClassificationResult<ComponentOccurrence> result = calculator.calculateMetrics(foundComponentOccurrences, groundTruth, null);
        System.out.println("Precision=" + result.getPrecision());
        System.out.println("Recall=" + result.getRecall());
        System.out.println("F1-Score=" + result.getF1());*/
    }


}
