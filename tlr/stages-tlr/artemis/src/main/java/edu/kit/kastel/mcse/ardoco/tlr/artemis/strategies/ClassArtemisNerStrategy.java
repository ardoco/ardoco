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
                
                Return the results in a clearly structured, unambiguous plain-text format that enables straightforward conversion to JSON (e.g., using key-value sections per class).
                """;
        String rules = """
                Rules for identifying classes:
                
                1.  Direct mentions:
                    Any line containing the class name, an alias, or an abbreviation is an occurrence of that class.
                
                2.  Pronoun/coreference resolution:
                    If a class is introduced and a following sentence clearly refers to the same class using a pronoun or other unambiguous reference, include both the introducing line and the referring line as occurrences.
                
                    Example:
                    "The class X does this.
                    It is structured as follows."
                
                    Both lines are occurrences of class X.
                
                3.  Retroactive identification:
                    If an entity is described first and a later sentence explicitly identifies that entity as a class, retroactively assign the earlier description to that class.
                
                    Example:
                    "There is an object that does A.
                    ...
                    The B object is responsible for A."
                
                    If it is clear that B is the previously described class, both lines are occurrences of this class B.
                
                4.  Explicit class requirement:
                    Only return classes that are explicitly identified somewhere in the text.
                    A class is considered identified only if the text explicitly mentions a concrete class, object type, or named entity (for example, by a class name, type name, or clearly equivalent identifier).
                    Do not invent or infer new classes solely from their behavior or responsibilities.
                    Behavioral descriptions without an explicit class identification are NOT occurrences.
                
                
                """;
        String searchStrategy = """
                Search Strategy: Two-pass search strategy
                    For every identified class, perform the following steps:
                    1. Locate the explicit introduction or declaration of the class.
                    2. Generate all reasonable lexical variants of the class name, including:
                       - lowercase forms,
                       - singular and plural forms,
                       - space-separated CamelCase forms,
                       - natural-language object phrases,
                       - aliases and abbreviations found in the text.
                    3. Search the entire text again using these variants.
                    4. Include every occurrence that clearly refers to the same class, even if the exact class name is not used.
                    5. Also include occurrences linked through coreference (e.g. pronouns) or later identification of previously unnamed entities.
                
                    Example:
                    Text:
                    "A task queue temporarily stores pending jobs.
                    Jobs remain in the queue until they are processed.
                    ...
                    Each queued job is implemented by the PendingJob class."
                
                    Expected occurrences for the class `PendingJob`:
                    - "A task queue temporarily stores pending jobs."
                    - "Jobs remain in the queue until they are processed."
                    - "Each queued job is implemented by the PendingJob class."#
                
                
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
