package edu.kit.kastel.mcse.ardoco.tlr.artemis.strategies;

import edu.kit.kastel.mcse.ardoco.core.api.models.Metamodel;
import edu.kit.kastel.mcse.ardoco.core.data.DataRepository;
import edu.kit.kastel.mcse.ardoco.naer.model.NamedEntityType;
import edu.kit.kastel.mcse.ardoco.naer.recognizer.Prompt;
import edu.kit.kastel.mcse.ardoco.naer.recognizer.TwoPartPrompt;

public class ClassArtemisNerStrategy implements ArtemisNerStrategy {
    @Override
    public Prompt createPrompt(DataRepository dataRepository) {
        String taskPrompt = """
                Identify all classes that are explicitly named in the following text.
                
                For each identified class, provide:
                - The primary name (as it appears in the text)
                - All alternative names or abbreviations used for the same class in the text (case-insensitive)
                - All full lines where the class is mentioned (directly or via clear context)
                
                Rules for identifying classes:
                
                1.  Pronoun and coreference resolution:
                    Resolve pronouns and other unambiguous references when they clearly refer to a previously introduced class.
                    Include both the introducing line and the referring line as occurrences.
                
                    Example:
                    "The class X does this.
                    It is structured as follows."
                
                    Both lines must be listed as occurrences of class X.
                
                2.  Constructor-style class references:
                    If a class is written in constructor-like form: ClassName(ClassArg)
                    Identify both ClassName and ClassArg as class occurrences (if it conforms to rule 3.).
                
                3.  Exclude standard-library classes
                
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

    @Override
    public NamedEntityType getNamedEntityType() {
        return NamedEntityType.CLASS;
    }
}
