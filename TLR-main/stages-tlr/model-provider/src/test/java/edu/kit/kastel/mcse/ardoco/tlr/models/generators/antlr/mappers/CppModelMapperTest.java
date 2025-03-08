package edu.kit.kastel.mcse.ardoco.tlr.models.generators.antlr.mappers;

import java.io.IOException;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.CodeModel;
import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.code.CodeItemRepository;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.cpp.CppElementManager;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.extraction.cpp.CppExtractor;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.mapping.cpp.CppModelMapper;

public class CppModelMapperTest {

    @Test
    void testCppModelMapper() throws IOException {
        CodeItemRepository repository = new CodeItemRepository();
        CppExtractor extractor = new CppExtractor(repository, "src/test/resources/cpp/interface/edu/");
        extractor.extractModel();
        CppElementManager manager = extractor.getElementManager();
        manager.addComments(extractor.getComments());

        CppModelMapper mapper = new CppModelMapper(repository, manager);
        mapper.mapToCodeModel();
        CodeModel codeModel = mapper.getCodeModel();

        // Assertions
        Assertions.assertNotNull(mapper);
        Assertions.assertNotNull(codeModel);
        Assertions.assertEquals(3, codeModel.getEndpoints().size());
    }

}
