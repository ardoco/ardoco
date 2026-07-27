package edu.kit.kastel.mcse.ardoco.tlr.connectiongenerator.ner.strategies;

import edu.kit.kastel.mcse.ardoco.core.api.models.Metamodel;
import edu.kit.kastel.mcse.ardoco.core.data.DataRepository;
import edu.kit.kastel.mcse.ardoco.naer.recognizer.Prompt;
import edu.kit.kastel.mcse.ardoco.naer.recognizer.TwoPartPrompt;

/**
 * NER strategy for identifying source code classes in text.
 * <p>
 * This strategy is used by the class-decision ArTEMiS approach to recover trace links between text and source code classes.
 * </p>
 */
public class ClassNerStrategy implements NerStrategy {

    @Override
    public Prompt getPrompt(DataRepository dataRepository) {
        String taskPrompt = """
                Identify all classes that are explicitly named in the following text.
                
                For each identified class, provide:
                - The primary name (as it appears in the text)
                - All alternative names or abbreviations used for the same class in the text (case-insensitive)
                - All full lines where the class is mentioned (directly or via clear context)
                
                Return the results in a clearly structured, unambiguous plain-text format that enables straightforward conversion to JSON (e.g., using key-value sections per class).
                """;
        String formattingPrompt = """
                Given the last answer (see below), for each class, return a JSON object containing:
                - "name": the primary name of the class.
                - "type": "CLASS"
                - "alternativeNames": a list of alternative or ambiguous names, if applicable.
                - "occurrences": a list of lines where the class appears or is referenced.
                
                Output should be a JSON array (and nothing else!), like:
                [
                    {
                        "name": "...",
                        "type": "CLASS",
                        "alternativeNames": [...],
                        "occurrences": [...]
                    },
                    ...
                ]
                
                Example:
                [
                    {
                        "name": "Recognizer",
                        "type": "CLASS",
                        "alternativeNames": [],
                        "occurrences": ["The Recognizer class contains the recognition logic."]
                    },
                    {
                        "name": "PropertyExtractor",
                        "type": "CLASS",
                        "alternativeNames": ["PE"],
                        "occurrences": ["The PropertyExtractor extracts important properties and returns them.", "This data is then passed on to the PE."]
                    }
                ]
                """;
        return new TwoPartPrompt(taskPrompt, formattingPrompt);
    }

    @Override
    public Metamodel getMetamodel() {
        return Metamodel.CODE_WITH_COMPILATION_UNITS;
    }

}
