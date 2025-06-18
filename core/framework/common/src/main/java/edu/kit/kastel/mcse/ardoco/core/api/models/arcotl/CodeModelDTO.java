package edu.kit.kastel.mcse.ardoco.core.api.models.arcotl;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.code.CodeItemRepository;

public record CodeModelDTO(@JsonProperty CodeItemRepository codeItemRepository, @JsonProperty List<String> content) {

    @Override
    public CodeItemRepository codeItemRepository() {
        codeItemRepository.init();
        return codeItemRepository;
    }
}
