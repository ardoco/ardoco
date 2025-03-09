package edu.kit.kastel.mcse.ardoco.tlr.models.generators.antlr.extraction.python3;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.Type;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.VariableElement;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.extraction.python3.Python3ElementExtractor;

public class Python3VariableExtractorTest {
    String sourcePath = "src/test/resources/python/interface/edu/";

    @Test
    void testPyAClassVariableExtractor() throws IOException {
        String filePath = sourcePath + "APyClass.py";
        List<VariableElement> variables = extractVariablesFromFile(filePath);
        Assertions.assertEquals(10, variables.size());

        // Test the first variable
        Assertions.assertEquals("class_variable", variables.get(0).getName());
        Assertions.assertEquals("str", variables.get(0).getDataType());
        Assertions.assertEquals("AClass", variables.get(0).getParent().getName());
        Assertions.assertEquals(Type.CLASS, variables.get(0).getParent().getType());

        // Test the second variable
        Assertions.assertEquals("self.name", variables.get(1).getName());
        Assertions.assertEquals("any", variables.get(1).getDataType());
        Assertions.assertEquals("__init__", variables.get(1).getParent().getName());
        Assertions.assertEquals(Type.FUNCTION, variables.get(1).getParent().getType());

        // Test the third variable
        Assertions.assertEquals("self.instance_variable", variables.get(2).getName());
        Assertions.assertEquals("str", variables.get(2).getDataType());
        Assertions.assertEquals("__init__", variables.get(2).getParent().getName());
        Assertions.assertEquals(Type.FUNCTION, variables.get(2).getParent().getType());

        // Test the fourth variable
        Assertions.assertEquals("class_variable", variables.get(3).getName());
        Assertions.assertEquals("str", variables.get(3).getDataType());
        Assertions.assertEquals("InnerClass1", variables.get(3).getParent().getName());
        Assertions.assertEquals(Type.CLASS, variables.get(3).getParent().getType());

        // Test the fifth variable
        Assertions.assertEquals("self.value", variables.get(4).getName());
        Assertions.assertEquals("any", variables.get(4).getDataType());
        Assertions.assertEquals("__init__", variables.get(4).getParent().getName());
        Assertions.assertEquals(Type.FUNCTION, variables.get(4).getParent().getType());

        // Test the sixth variable
        Assertions.assertEquals("self.instance_variable", variables.get(5).getName());
        Assertions.assertEquals("str", variables.get(5).getDataType());
        Assertions.assertEquals("__init__", variables.get(5).getParent().getName());
        Assertions.assertEquals(Type.FUNCTION, variables.get(5).getParent().getType());

        // Test the seventh variable
        Assertions.assertEquals("class_variable", variables.get(6).getName());
        Assertions.assertEquals("str", variables.get(6).getDataType());
        Assertions.assertEquals("InnerClass2", variables.get(6).getParent().getName());
        Assertions.assertEquals(Type.CLASS, variables.get(6).getParent().getType());

        // Test the eighth variable
        Assertions.assertEquals("self.value", variables.get(7).getName());
        Assertions.assertEquals("any", variables.get(7).getDataType());
        Assertions.assertEquals("__init__", variables.get(7).getParent().getName());
        Assertions.assertEquals(Type.FUNCTION, variables.get(7).getParent().getType());

        // Test the ninth variable
        Assertions.assertEquals("self.instance_variable", variables.get(8).getName());
        Assertions.assertEquals("str", variables.get(8).getDataType());
        Assertions.assertEquals("__init__", variables.get(8).getParent().getName());
        Assertions.assertEquals(Type.FUNCTION, variables.get(8).getParent().getType());

        // Test the tenth variable
        Assertions.assertEquals("class_variable", variables.get(9).getName());
        Assertions.assertEquals("str", variables.get(9).getDataType());
        Assertions.assertEquals("BClass", variables.get(9).getParent().getName());
        Assertions.assertEquals(Type.CLASS, variables.get(9).getParent().getType());
    }

    private List<VariableElement> extractVariablesFromFile(String filePath) throws IOException {
        Python3ElementExtractor extractor = new Python3ElementExtractor();
        Path path = Path.of(filePath);
        extractor.extract(path);
        return extractor.getElements().getVariables();
    }

}
