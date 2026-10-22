/* Licensed under MIT 2026. */
package edu.kit.kastel.mcse.ardoco.id;

import edu.kit.kastel.mcse.ardoco.core.api.models.Metamodel;
import edu.kit.kastel.mcse.ardoco.core.data.DataRepository;
import edu.kit.kastel.mcse.ardoco.naer.model.NamedEntityType;
import edu.kit.kastel.mcse.ardoco.naer.recognizer.Prompt;
import edu.kit.kastel.mcse.ardoco.naer.recognizer.TwoPartPrompt;
import edu.kit.kastel.mcse.ardoco.tlr.artemis.strategies.ArtemisNerStrategy;

public class ProgrammingLanguageNerStrategy implements ArtemisNerStrategy {

    @Override
    public Prompt createPrompt(DataRepository dataRepository) {
        String taskPrompt = """
                Identify all programming languages that are explicitly mentioned in the following text.

                For each identified programming language, provide:
                - The primary name (as it appears in the text)
                - All alternative names or abbreviations used for the same language in the text (case-insensitive)
                - All full lines where the programming language is mentioned (directly or via clear context)

                Rules for identifying programming languages:

                1.  Explicit mentions only:
                    Identify a programming language only when its name, abbreviation, alias, or an unambiguous language-specific reference is explicitly present in the text.
                    Do not resolve pronouns, anaphora, or other coreferences. Do not infer a programming language from surrounding context.

                2.  Web languages:
                    Treat HTML, CSS, and similar web languages as programming languages for this task.
                    Identify them when they are explicitly mentioned by name or by a recognized abbreviation/alias.

                    HTML file references count as occurrences only when they refer to project files, file paths, or filenames.
                    Do not identify HTML from URLs, hyperlinks, or other external links merely because the URL contains ".html" or the term "html".
                    For example, "index.html" or "src/pages/index.html" should be treated as an occurrence of HTML, while "https://www.example.com/page.html" should not.

                3.  Query and database languages:
                    Treat SQL and other query/database languages as programming languages for this task.
                    Identify them when they are explicitly mentioned by name or by a recognized abbreviation/alias.

                4.  File extensions and filenames:
                    Identify a programming language when its standard file extension, filename convention, or language-specific path reference clearly identifies that language.
                    Treat such references as occurrences of the corresponding programming language, even when the language name itself is not written explicitly.
                    For example, ".py" should be treated as an occurrence of Python, and ".js" as an occurrence of JavaScript.
                    Do not identify a language from generic or ambiguous file extensions that cannot be reliably associated with a specific programming language.

                5.  No invented information:
                    Report only programming languages, aliases, and occurrence lines supported by the supplied text.

                6.  Non-programming language file formats / data-file formats:
                    Do not identify data formats, markup formats, configuration formats, or other non-programming file types as programming languages solely because their file extensions or filenames appear.
                    For example, ".json", ".csv", ".xml", and ".yaml" should not be identified as programming languages unless the text explicitly identifies them as a programming language.

                Return the results in a clearly structured, unambiguous plain-text format that enables straightforward conversion to JSON (e.g., using key-value sections per file).
                """;
        String formattingPrompt = """
                Given the last answer (see below), for each programming language, return a JSON object containing:

                - "name": the primary name of the programming language.
                - "type": "PROGRAMMING_LANGUAGE"
                - "alternativeNames": a list of alternative or ambiguous names, if applicable.
                - "occurrences": a list of lines where the file appears or is referenced.


                Output should be a JSON array (and nothing else!), like:
                [
                    {
                        "name": "...",
                        "type": "PROGRAMMING_LANGUAGE",
                        "alternativeNames": [...],
                        "occurrences": [...]
                    },
                    ...
                ]

                Example:
                [
                    {
                        "name": "java",
                        "type": "PROGRAMMING_LANGUAGE",
                        "alternativeNames": [],
                        "occurrences": ["The framework logic is written in java."]
                    },
                    {
                        "name": "python",
                        "type": "PROGRAMMING_LANGUAGE",
                        "alternativeNames": [...],
                        "occurrences": ["The python helper scripts were used to do this.", "They can be found in the 'src/helpers/' folder."]
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
        return NamedEntityType.PROGRAMMING_LANGUAGE;
    }
}
