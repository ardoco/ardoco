package edu.kit.kastel.mcse.ardoco.tlr.artemis.strategies;

import edu.kit.kastel.mcse.ardoco.core.api.models.Metamodel;
import edu.kit.kastel.mcse.ardoco.core.data.DataRepository;
import edu.kit.kastel.mcse.ardoco.naer.model.NamedEntityType;
import edu.kit.kastel.mcse.ardoco.naer.recognizer.Prompt;
import edu.kit.kastel.mcse.ardoco.naer.recognizer.TwoPartPrompt;

public class DatafileArtemisNerStrategy implements ArtemisNerStrategy {
    @Override
    public Prompt createPrompt(DataRepository dataRepository) {
        String taskPrompt = """
                Identify all data files that are explicitly named in the following text.
                
                For each identified file, provide:
                - The primary name (as it appears in the text)
                - All alternative names or abbreviations used for the same file in the text (case-insensitive)
                - All full lines where the file is mentioned (directly or via clear context)
                
                Rules for identifying data files:
                
                1.  Data files are non-executable files that contain information essential to the operation, configuration, processing, or functionality of a system or application.
                    They provide meaningful data that can be stored, transferred, processed, or shared between systems or subsystems.
                    Files that only serve a visual, illustrative, or documentation purpose (for example, architecture_overview.png) are not considered data files.
                
                2.  Pronoun and coreference resolution:
                    Resolve pronouns and other unambiguous references when they clearly refer to a previously introduced data file.
                    Include both the introducing line and the referring line as occurrences.
                
                    Example:
                    "The configuration.xml file specifies how the system behaves.
                    It is structured as follows."
                
                    Both lines must be listed as occurrences of datafile configuration.xml.
                
                Return the results in a clearly structured, unambiguous plain-text format that enables straightforward conversion to JSON (e.g., using key-value sections per file).
                """;
        String formattingPrompt = """
                Given the last answer (see below), for each file, return a JSON object containing:
                - "name": the primary name of the file.
                - "type": "DATAFILE"
                - "alternativeNames": a list of alternative or ambiguous names, if applicable.
                - "occurrences": a list of lines where the file appears or is referenced.
                
                Output should be a JSON array (and nothing else!), like:
                [
                    {
                        "name": "...",
                        "type": "DATAFILE",
                        "alternativeNames": [...],
                        "occurrences": [...]
                    },
                    ...
                ]
                
                Example:
                [
                    {
                        "name": "db.config",
                        "type": "DATAFILE",
                        "alternativeNames": [],
                        "occurrences": ["The database properties are configured using the db.config file."]
                    },
                    {
                        "name": "main_conf.yaml",
                        "type": "DATAFILE",
                        "alternativeNames": ["main configuration file"],
                        "occurrences": ["One can configure the system using the main_conf.yaml file.", "This can also be configured using the main configuration file."]
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
        return NamedEntityType.DATAFILE;
    }
}
