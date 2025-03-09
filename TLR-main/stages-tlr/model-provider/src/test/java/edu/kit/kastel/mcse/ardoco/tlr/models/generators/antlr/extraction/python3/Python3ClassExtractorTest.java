package edu.kit.kastel.mcse.ardoco.tlr.models.generators.antlr.extraction.python3;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.Type;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.ClassElement;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.extraction.python3.Python3ElementExtractor;

public class Python3ClassExtractorTest {
    String sourcePath = "src/test/resources/python/interface/edu/";

    @Test
    void testPyAClassPython3ExtractorTest() throws IOException {
        String filePath = sourcePath + "APyClass.py";
        List<ClassElement> classes = extractClassElementsFromFile(filePath);

        Assertions.assertEquals(4, classes.size());

                Assertions.assertEquals("InnerClass1", classes.get(0).getName());
                Assertions.assertEquals(0, classes.get(0).getInherits().size());
                Assertions.assertEquals(0, classes.get(0).getInherits().size());
                Assertions.assertEquals("AClass", classes.get(0).getParent().getName());
                Assertions.assertEquals(Type.CLASS, classes.get(0).getParent().getType());


        Assertions.assertEquals("InnerClass2", classes.get(1).getName());
        Assertions.assertEquals(0, classes.get(1).getInherits().size());
        Assertions.assertEquals("AClass", classes.get(1).getParent().getName());
        Assertions.assertEquals(Type.CLASS, classes.get(1).getParent().getType());

        Assertions.assertEquals("AClass", classes.get(2).getName());
        Assertions.assertEquals(0, classes.get(2).getInherits().size());
        Assertions.assertEquals("APyClass", classes.get(2).getParent().getName());
        Assertions.assertEquals(Type.MODULE, classes.get(2).getParent().getType());




        // Test the fourth class
        Assertions.assertEquals("BClass", classes.get(3).getName());
        Assertions.assertEquals(0, classes.get(1).getInherits().size());
        Assertions.assertEquals("APyClass", classes.get(3).getParent().getName());
        Assertions.assertEquals(Type.MODULE, classes.get(3).getParent().getType());
    }

    @Test
    void testPyEnumPython3ExtractorTest() throws IOException {
        String filePath = sourcePath + "APyEnum.py";
        List<ClassElement> classes = extractClassElementsFromFile(filePath);

        Assertions.assertEquals(1, classes.size());

        // Test the first class
        Assertions.assertEquals("APyEnum", classes.get(0).getName());
        Assertions.assertEquals(1, classes.get(0).getInherits().size());
        Assertions.assertEquals("Enum", classes.get(0).getInherits().get(0));
        Assertions.assertEquals("APyEnum", classes.get(0).getParent().getName());
        Assertions.assertEquals(Type.MODULE, classes.get(0).getParent().getType());
    }

    @Test
    void testPyMetaclassPython3ExtractorTest() throws IOException {
        String filePath = sourcePath + "APyMetaclass.py";
        List<ClassElement> classes = extractClassElementsFromFile(filePath);

        Assertions.assertEquals(1, classes.size());

        // Test the first class
        Assertions.assertEquals("APyMetaclass", classes.get(0).getName());
        Assertions.assertEquals(1, classes.get(0).getInherits().size());
        Assertions.assertEquals("type", classes.get(0).getInherits().get(0));
        Assertions.assertEquals("APyMetaclass", classes.get(0).getParent().getName());
        Assertions.assertEquals(Type.MODULE, classes.get(0).getParent().getType());
    }

    @Test
    void testPyDataClassPython3ExtractorTest() throws IOException {
        String filePath = sourcePath + "APyDataClass.py";
        List<ClassElement> classes = extractClassElementsFromFile(filePath);

        Assertions.assertEquals(1, classes.size());

        // Test the first class
        Assertions.assertEquals("APyDataclass", classes.get(0).getName());
        Assertions.assertEquals(0, classes.get(0).getInherits().size());
        Assertions.assertEquals("APyDataClass", classes.get(0).getParent().getName());
        Assertions.assertEquals(Type.MODULE, classes.get(0).getParent().getType());
    }

    @Test
    void testPyModulePython3ExtractorTest() throws IOException {
        String filePath = sourcePath + "APyModule.py";
        List<ClassElement> classes = extractClassElementsFromFile(filePath);
        Assertions.assertEquals(0, classes.size());
    }

    private List<ClassElement> extractClassElementsFromFile(String filePath) throws IOException {
        Python3ElementExtractor extractor = new Python3ElementExtractor();
        Path path = Path.of(filePath);
        extractor.extract(path);
        return extractor.getElements().getClasses();
    }
}
