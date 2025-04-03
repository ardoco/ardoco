package edu.kit.kastel.mcse.ardoco.tlr.models.generators;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.CodeModel;
import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.code.CodeItemRepository;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.extraction.cpp.CppExtractor;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.extraction.java.JavaExtractor;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.extraction.python3.Python3Extractor;

public class GithubProjectTest {

    @Test
    public void testCppProject() {
        String directory = "src/test/resources/testprojects/cpp/simplerenderengine/";
        CodeItemRepository repository = new CodeItemRepository();
        CppExtractor extractor = new CppExtractor(repository, directory);
        CodeModel codeModel = extractor.extractModel();

        // Assertions
        Assertions.assertNotNull(extractor.getElementExtractor().getElements().getAllElements().size() != 0);
        Assertions.assertNotNull(codeModel);
    }

    @Test
    public void testJavaProject() {
        String directory = "src/test/resources/testprojects/java/simplehotelmanagement/";
        CodeItemRepository repository = new CodeItemRepository();
        JavaExtractor extractor = new JavaExtractor(repository, directory);
        CodeModel codeModel = extractor.extractModel();

        // Assertions
        Assertions.assertNotNull(extractor.getElementExtractor().getElements());
        Assertions.assertNotNull(codeModel);
    }
    
    @Test
    public void testPython3Project() {
        String directory = "src/test/resources/testprojects/python3/simpledjangologin/";
        CodeItemRepository repository = new CodeItemRepository();
        Python3Extractor extractor = new Python3Extractor(repository, directory);
        CodeModel codeModel = extractor.extractModel();

        // Assertions
        Assertions.assertNotNull(extractor.getElementExtractor().getElements());
        Assertions.assertNotNull(codeModel);
    }

    @Test
    public void testJavaArDoCoBenchmarkProject() {
        String directory = "src/test/resources/testprojects/ardoco_benchmarks/jabref/";
        CodeItemRepository repository = new CodeItemRepository();
        JavaExtractor extractor = new JavaExtractor(repository, directory);
        CodeModel codeModel = extractor.extractModel();

        // Assertions
        Assertions.assertNotNull(extractor.getElementExtractor().getElements());
        Assertions.assertNotNull(codeModel);
    }
}
