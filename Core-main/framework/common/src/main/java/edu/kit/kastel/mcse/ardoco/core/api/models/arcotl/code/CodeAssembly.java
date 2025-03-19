/* Licensed under MIT 2023-2024. */
package edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.code;

import java.util.SortedSet;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;

@JsonTypeName("CodeAssembly")
public final class CodeAssembly extends CodeModule {

    @JsonProperty
    private ProgrammingLanguage language;

    @SuppressWarnings("unused")
    private CodeAssembly() {
        // Jackson
    }

    public CodeAssembly(CodeItemRepository codeItemRepository, String name, SortedSet<? extends CodeItem> content) {
        super(codeItemRepository, name, content);
    }

    public CodeAssembly(CodeItemRepository codeItemRepository, String name, SortedSet<? extends CodeItem> content, ProgrammingLanguage language) {
        super(codeItemRepository, name, content);
        this.language = language;
    }
}
