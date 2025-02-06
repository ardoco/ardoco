package edu.kit.kastel.mcse.ardoco.tlr.models.generators.antlr.mappers;
import java.io.IOException;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.CodeModel;
import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.code.CodeItemRepository;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.java.JavaExtractor;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.mappers.JavaModelMapper;


public class JavaModelMapperTest {

    @Test
    void testJavaModelMapper() throws IOException {
        CodeItemRepository repository = new CodeItemRepository();
        JavaExtractor extractor = new JavaExtractor(repository, "src/test/resources/interface/edu/");
        extractor.execute();

        JavaModelMapper mapper = new JavaModelMapper(repository, extractor.getVariables(), extractor.getControls(), extractor.getClasses(), extractor.getInterfaces(), extractor.getCompilationUnits(), extractor.getPackages(), extractor.getComments());
        mapper.mapToCodeModel();
        CodeModel codeModel = mapper.getCodeModel();
        // Assertions
        Assertions.assertNotNull(mapper);
        Assertions.assertNotNull(codeModel);
        Assertions.assertEquals(7, codeModel.getEndpoints().size());

        // More Detailed Assertions
        Assertions.assertEquals(codeModel.getAllPackages().size(), 3);

    }

}
