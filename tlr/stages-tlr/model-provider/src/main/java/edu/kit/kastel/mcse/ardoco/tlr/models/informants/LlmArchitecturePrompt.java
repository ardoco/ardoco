/* Licensed under MIT 2024-2025. */
package edu.kit.kastel.mcse.ardoco.tlr.models.informants;

import java.util.List;

public enum LlmArchitecturePrompt {
    EXTRACT_FROM_ARCHITECTURE(
            """
                    Your task is to identify the high-level components based on a software architecture documentation. In a first step, you shall elaborate on the following documentation:

                    %s
                    """,
            """
                    Now provide a list that only covers the component names. Omit common prefixes and suffixes in the names in camel case.
                    Output format:
                    - Name1
                    - Name2
                    """), //
    EXTRACT_FROM_CODE(
            """
                    You get the {FEATURES} of a software project. Your task is to summarize the {FEATURES} w.r.t. the high-level architecture of the system. Try to identify possible components.

                    {FEATURES}:

                    %s
                    """,
            """
                    Now provide a list that only covers the component names. Omit common prefixes and suffixes in the names in camel case.
                    Output format:
                    - Name1
                    - Name2
                    """), //
    AGGREGATION("""
            You get a list of possible component names. Your task is to aggregate the list and remove duplicates.
            Omit common prefixes and suffixes in the names in camel case.
            Output format:
            - Name1
            - Name2

            Possible component names:

            %s
            """);

    private final List<String> templates;

    LlmArchitecturePrompt(String... templates) {
        this.templates = List.of(templates);
    }

    public List<String> getTemplates() {
        if (this == EXTRACT_FROM_CODE)
            throw new IllegalArgumentException("This method is not supported for this enum value");
        return templates;
    }

    public List<String> getTemplates(Features features) {
        return templates.stream().map(it -> it.replace("{FEATURES}", features.toString())).toList();
    }

    public enum Features {
        PACKAGES, PACKAGES_AND_THEIR_CLASSES;

        @Override
        public String toString() {
            return super.toString().charAt(0) + super.toString().toLowerCase().substring(1).replace("_", " ");
        }
    }
}
