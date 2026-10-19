package edu.kit.kastel.mcse.ardoco.id;

import edu.kit.kastel.mcse.ardoco.core.api.models.Metamodel;
import edu.kit.kastel.mcse.ardoco.core.data.DataRepository;
import edu.kit.kastel.mcse.ardoco.naer.model.NamedEntityType;
import edu.kit.kastel.mcse.ardoco.naer.recognizer.Prompt;
import edu.kit.kastel.mcse.ardoco.naer.recognizer.TwoPartPrompt;
import edu.kit.kastel.mcse.ardoco.tlr.artemis.strategies.ArtemisNerStrategy;

public class ArchitecturalStylePatternNerStrategy implements ArtemisNerStrategy {

    @Override
    public Prompt createPrompt(DataRepository dataRepository) {
        String taskPrompt = """
                Identify all architectural styles, architectural design patterns and software design patterns that are explicitly mentioned in the following text.
                
                For each identified style or pattern, provide:
                - The primary name as it appears in the text
                - All alternative names, abbreviations, or aliases used for the same style or pattern in the text
                - All full lines where the style or pattern is mentioned directly or via an unambiguous alias
                
                Rules:
                1.  Occurrence granularity:
                    An occurrence is a full documentation line where the style or pattern is mentioned.
                    If a line mentions multiple styles or patterns, report an occurrence for each style or pattern.
                
                2.  Recognized patterns only:
                    Identify a term only if it is a recognized architectural style, architectural design pattern, or software design pattern.
                    A technical concept, implementation technique, infrastructure component, or database technology is not a pattern merely because it describes how a system works.
                
                3.  Pattern-specific evidence:
                    The text must explicitly name a recognized pattern/style or describe it through an unambiguous, specific reference.
                
                Return the results in a clearly structured, unambiguous plain-text format that enables straightforward conversion to JSON.
                """;

        String formattingPrompt = """
                Given the last answer, return a JSON array and nothing else.
                
                For each architectural style or pattern, return one JSON object with:
                
                - "name": the primary name of the architectural style or pattern.
                - "type": "ARCHITECTURAL_PATTERN"
                - "alternativeNames": a list of alternative names, abbreviations, or aliases, if applicable.
                - "occurrences": a list of full lines where the style or pattern appears.
                
                Example:
                [
                    {
                        "name": "MVC",
                        "type": "ARCHITECTURAL_PATTERN",
                        "alternativeNames": ["Model-View-Controller"],
                        "occurrences": [
                            "The UI, controller, and logic packages follow the MVC pattern."
                        ]
                    },
                    {
                        "name": "Template Method",
                        "type": "ARCHITECTURAL_PATTERN",
                        "alternativeNames": [],
                        "occurrences": [
                            "The request handling workflow uses the Template Method pattern."
                        ]
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
        return NamedEntityType.ARCHITECTURAL_PATTERN;
    }
}
