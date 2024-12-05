/* Licensed under MIT 2023-2024. */
package edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.code;

import java.util.SortedSet;

import com.fasterxml.jackson.annotation.JsonTypeName;

@JsonTypeName("CodeAssembly")
public final class CodeAssembly extends CodeModule {

    private static final long serialVersionUID = 3082912967900986071L;

    @SuppressWarnings("unused")
    private CodeAssembly() {
        // Jackson
    }

    public CodeAssembly(CodeItemRepository codeItemRepository, String name, SortedSet<? extends CodeItem> content) {
        super(codeItemRepository, name, content);
    }
}
