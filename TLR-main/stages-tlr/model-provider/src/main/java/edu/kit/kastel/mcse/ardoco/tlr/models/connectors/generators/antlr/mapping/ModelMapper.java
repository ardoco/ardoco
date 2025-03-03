package edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.mapping;

import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.CodeModel;

public interface ModelMapper {
    void mapToCodeModel();

    CodeModel getCodeModel();
}
